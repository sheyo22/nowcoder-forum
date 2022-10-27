package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("com.nowcoder.community.dao")
class CommunityApplicationTests{
    private ApplicationContext applicationContext;
    @Resource
    private DiscussPostMapper discussPostMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserService userService;

    @Resource
    private DiscussPostService discussPostService;
    /*@Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }*/

    @Test
    void contextLoads() {
    }

    @Test
    public void testSelectPosts(){
        List<DiscussPost> testList = discussPostMapper.selectDiscussPosts(0,0,10);
        for(DiscussPost post : testList){
            System.out.println(post);
        }
        System.out.println(discussPostMapper.selectDiscussPostRows(0));
    }

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(0);
        System.out.println(user);
        user = userMapper.selectByEmail("nowcoder116@sina.com");
        System.out.println(user);
        user = userMapper.selectByName("SYSTEM");
        System.out.println(user);
    }

    @Test
    public void testController(){
        List<DiscussPost> list = discussPostService.findDiscussPosts(0,0,10);
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        if(list!=null){
            for(DiscussPost discussPost:list){
                Map<String,Object> map = new HashMap<>();
                User user = userService.findUserById(discussPost.getUserId());
                map.put("post",discussPost);
                map.put("user",user);
                discussPosts.add(map);
            }
        }
        for(Map map : discussPosts){
            System.out.println(map.get("post"));
            System.out.println(map.get("user"));
        }
    }

    @Test
    public void testGetMD5(){
        String password = "123456";
        String salt = "33785";
        System.out.println(CommunityUtil.md5(password+salt));
    }

}
