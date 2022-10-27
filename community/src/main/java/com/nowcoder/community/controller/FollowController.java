package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Resource
    private FollowService followService;

    @Resource
    private UserService userService;

    @Resource
    private HostHolder hostHolder;

    @Resource
    private EventProducer eventProducer;

    @RequestMapping(path = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(user.getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(200, "已关注");
    }

    @RequestMapping(path = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(200, "已取关");
    }

    @RequestMapping(path = "/followees/{userId}", method = RequestMethod.GET)
    public String getFollowee(Page page, Model model, @PathVariable int userId) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));
        List<Map<String, Object>> followeeVoList = followService.findFollowees(userId, page);
        User loginUser = hostHolder.getUser();
        if (followeeVoList != null) {
            for (Map<String, Object> followeeVo : followeeVoList) {
                User target = (User) followeeVo.get("user");
                boolean hasFollowed = loginUser == null ? false : followService.hasFollowed(loginUser.getId(), ENTITY_TYPE_USER, target.getId());
                followeeVo.put("hasFollowed", hasFollowed);
            }
        }
        model.addAttribute("followeeVoList", followeeVoList);
        return "/site/followee";
    }

    @RequestMapping(path = "/followers/{userId}", method = RequestMethod.GET)
    public String getFollower(Page page, Model model, @PathVariable int userId) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(userId, ENTITY_TYPE_USER));
        List<Map<String, Object>> followerVoList = followService.findFollowers(userId, page);
        User loginUser = hostHolder.getUser();
        if (followerVoList != null) {
            for (Map<String, Object> followerVo : followerVoList) {
                User target = (User) followerVo.get("user");
                boolean hasFollowed = loginUser == null ? false : followService.hasFollowed(loginUser.getId(), ENTITY_TYPE_USER, target.getId());
                followerVo.put("hasFollowed", hasFollowed);
            }
        }
        model.addAttribute("followerVoList", followerVoList);
        return "/site/follower";
    }
}
