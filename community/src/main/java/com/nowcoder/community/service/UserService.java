package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Resource
    private UserMapper userMapper;

    @Resource
    private MailClient mailClient;

    @Resource
    private TemplateEngine templateEngine;

    @Resource
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        User user = getCache(id);
        if (user != null) return user;
        user = initCache(id);
        return user;
    }

    public User findUserByUsername(String username) {
        return userMapper.selectByName(username);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        //处理传入空值
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        //验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "账号已存在");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "邮箱已存在");
            return map;
        }
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        context.setVariable("url", domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode());
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);
        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            cleanCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空");
            return map;
        }
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空");
            return map;
        }
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "该账户不存在");
            return map;
        }
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "该账户未激活");
            return map;
        }
        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "密码不正确");
            return map;
        }
        LoginTicket ticket = new LoginTicket();
        ticket.setUserId(user.getId());
        ticket.setTicket(CommunityUtil.generateUUID());
        ticket.setStatus(0);
        ticket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000L));
        String ticketKey = RedisKeyUtil.getUserTicketKey(ticket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, ticket);
        map.put("ticket", ticket.getTicket());
        return map;
    }

    public Map<String, Object> sendForgetCode(String email) {
        Map<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(email)) {
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            map.put("emailMsg", "邮箱填写错误");
            return map;
        }
        String codeKey = RedisKeyUtil.getForgetCodeKey(email);
        String code = CommunityUtil.generateUUID().substring(0, 6).toLowerCase();
        redisTemplate.opsForValue().set(codeKey, code, 300, TimeUnit.SECONDS);
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        context.setVariable("code", code);
        String content = templateEngine.process("/mail/forget", context);
        mailClient.sendMail(email, "找回密码", content);
        return map;
    }

    public Map<String, Object> forgetPassword(String email, String code,String password) {
        Map<String,Object> map = new HashMap<>();
        String codeKey = RedisKeyUtil.getForgetCodeKey(email);
        String correct = (String) redisTemplate.opsForValue().get(codeKey);
        if(correct==null){
            map.put("emailMsg","请确认邮箱正确并重新发送验证码");
            return map;
        }
        if(!correct.equalsIgnoreCase(code.trim())){
            map.put("codeMsg","验证码输入错误");
            return map;
        }
        password = password.trim();
        if(password.length()<6){
            map.put("passwordMsg","密码长度不能小于6位");
            return map;
        }
        User user = userMapper.selectByEmail(email);
        userMapper.updatePassword(user.getId(), CommunityUtil.md5(password + user.getSalt()));
        cleanCache(user.getId());
        return map;
    }

    public void logout(String ticket) {
        String ticketKey = RedisKeyUtil.getUserTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket) {
        String ticketKey = RedisKeyUtil.getUserTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    public int updateHeader(int userId, String headerUrl) {
        int re = userMapper.updateHeader(userId, headerUrl);
        cleanCache(userId);
        return re;
    }

    public int updatePassword(User user, String oldPassword, String newPassword) {
        oldPassword = CommunityUtil.md5(oldPassword + user.getSalt());
        if (!oldPassword.equals(user.getPassword())) {
            return 0;
        }
        int re = userMapper.updatePassword(user.getId(), CommunityUtil.md5(newPassword + user.getSalt()));
        cleanCache(user.getId());
        return re;
    }

    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    private void cleanCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }
}
