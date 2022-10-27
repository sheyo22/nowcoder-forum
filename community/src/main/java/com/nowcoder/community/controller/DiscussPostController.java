package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
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
import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Resource
    private HostHolder hostHolder;

    @Resource
    private DiscussPostService discussPostService;

    @Resource
    private UserService userService;

    @Resource
    private CommentService commentService;

    @Resource
    private LikeService likeService;

    @LoginRequired
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(DiscussPost discussPost) {
        User user = hostHolder.getUser();
        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(new Date());
        discussPostService.addDiscussPost(discussPost);
        return CommunityUtil.getJSONString(200, "发布成功");
    }

    @RequestMapping(path = "/detail/{postId}", method = RequestMethod.GET)
    public String getDiscussPostDetail(@PathVariable int postId, Model model, Page page) {
        DiscussPost post = discussPostService.getDiscussPostDetail(postId);
        if (post == null) {
            throw new RuntimeException("该帖已经无法浏览");
        }
        //帖子信息
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("post", post);
        model.addAttribute("user", user);
        model.addAttribute("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId));
        User curUser = hostHolder.getUser();
        int postLikeStatus = curUser == null ? 0 : likeService.findEntityLikeStatus(curUser.getId(), ENTITY_TYPE_POST, postId);
        model.addAttribute("likeStatus", postLikeStatus);
        //评论
        page.setLimit(5);
        page.setPath("/discuss/detail/" + postId);
        page.setRows(post.getCommentCount());
        List<Comment> commentList = commentService.findCommentsByEntity(
                ENTITY_TYPE_POST, postId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                Map<String, Object> commentVo = new HashMap<>();
                commentVo.put("comment", comment);
                commentVo.put("user", userService.findUserById(comment.getUserId()));
                commentVo.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId()));
                int commentLikeStatus = curUser == null ? 0 : likeService.findEntityLikeStatus(curUser.getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", commentLikeStatus);
                List<Comment> replyList = commentService.findCommentsByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                for (Comment reply : replyList) {
                    Map<String, Object> replyVo = new HashMap<>();
                    replyVo.put("reply", reply);
                    replyVo.put("user", userService.findUserById(reply.getUserId()));
                    User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                    replyVo.put("target", target);
                    replyVo.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId()));
                    int replyLikeStatus = curUser == null ? 0 : likeService.findEntityLikeStatus(curUser.getId(), ENTITY_TYPE_COMMENT, comment.getId());
                    replyVo.put("likeStatus", replyLikeStatus);
                    replyVoList.add(replyVo);
                }
                commentVo.put("replies", replyVoList);
                int replyCount = commentService.findCommentCountByEntity(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);
                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }
}
