package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Resource
    private UserService userService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private FollowService followService;

    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("msg", "还没有选择图片!");
            return "/site/setting";
        }
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("msg", "文件的格式不正确");
            return "/site/setting";
        }
        fileName = CommunityUtil.generateUUID() + "." + suffix;
        File dest = new File(uploadPath + "/" + fileName);
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常", e);
        }
        User user = hostHolder.getUser();
        userService.updateHeader(user.getId(), domain + contextPath + "/user/header/" + fileName);
        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(HttpServletResponse response, @PathVariable String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
        String filePath = uploadPath + "/" + fileName;
        response.setContentType("image/" + suffix);
        try (FileInputStream fis = new FileInputStream(filePath); OutputStream os = response.getOutputStream()) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败" + e.getMessage());
            throw new RuntimeException("读取头像失败", e);
        }
    }

    @LoginRequired
    @RequestMapping(path = "/password", method = RequestMethod.POST)
    public String setPassword(String oldPassword, String newPassword, Model model, @CookieValue("ticket") String ticket) {
        if (StringUtils.isBlank(newPassword)) {
            model.addAttribute("newPasswordMsg", "密码不能为空");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        int res = userService.updatePassword(user, oldPassword, newPassword);
        if (res == 0) {
            model.addAttribute("oldPasswordMsg", "原密码错误，请重新输入");
            return "/site/setting";
        }
        userService.logout(ticket);
        model.addAttribute("msg", "修改密码成功,请重新登录");
        model.addAttribute("target", "/login");
        return "/site/operate-result";
    }

    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getUserProfile(Model model, @PathVariable int userId) {
        User user = userService.findUserById(userId);
        model.addAttribute("user", user);

        String userLikeKey = RedisKeyUtil.getUserLikeKey(user.getId());
        Integer userLikeCount = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        model.addAttribute("userLikeCount", userLikeCount == null ? 0 : userLikeCount);

        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);

        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        User loginUser = hostHolder.getUser();
        if (loginUser != null) {
            boolean hasFollowed = followService.hasFollowed(loginUser.getId(), ENTITY_TYPE_USER, userId);
            model.addAttribute("hasFollowed", hasFollowed);
            model.addAttribute("loginUser", hostHolder.getUser());
        } else {
            model.addAttribute("hasFollowed", false);
        }
        return "/site/profile";
    }
}
