package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
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
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import java.util.*;

@Controller
public class MessageController implements CommunityConstant {

    @Resource
    private MessageService messageService;

    @Resource
    private UserService userService;

    @Resource
    private HostHolder hostHolder;

    @LoginRequired
    @RequestMapping(path = "/message/list", method = RequestMethod.GET)
    public String getMessageList(Page page, Model model) {
        User user = hostHolder.getUser();
        page.setPath("/message/list");
        page.setRows(messageService.findConversationCount(user.getId()));
        List<Message> messageList = messageService.findConversations(user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> messageVoList = new ArrayList<>();
        if (messageList != null) {
            for (Message message : messageList) {
                Map<String, Object> messageVo = new HashMap<>();
                messageVo.put("message", message);
                User target = message.getToId() == user.getId() ?
                        userService.findUserById(message.getFromId()) : userService.findUserById(message.getToId());
                messageVo.put("target", target);
                messageVo.put("count", messageService.findLetterCount(message.getConversationId()));
                messageVo.put("unreadCount", messageService.findUnreadLetterCount(user.getId(), message.getConversationId()));
                messageVoList.add(messageVo);
            }
        }
        model.addAttribute("letterUnreadCount", messageService.findUnreadLetterCount(user.getId(), null));
        model.addAttribute("noticeUnreadCount", messageService.findNoticeUnreadCount(user.getId(), null));
        model.addAttribute("conversations", messageVoList);
        return "/site/letter";
    }

    @RequestMapping(path = "/message/detail/{conversationId}", method = RequestMethod.GET)
    public String getMessageDetail(@PathVariable String conversationId, Model model, Page page) {
        page.setPath("/message/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));
        List<Message> messageList = messageService.findLetters(conversationId, page.getOffset(), page.getLimit());
        List<Map<String, Object>> messageVoList = new ArrayList<>();
        if (messageList != null) {
            for (Message message : messageList) {
                Map<String, Object> map = new HashMap<>();
                map.put("message", message);
                User user = userService.findUserById(message.getFromId());
                map.put("fromUser", user);
                messageVoList.add(map);
            }
        }
        model.addAttribute("letters", messageVoList);
        model.addAttribute("target", getLetterTarget(conversationId));
        List<Integer> ids = getUpdateIds(messageList);
        if (!ids.isEmpty()) messageService.readMessage(ids, 1);
        return "/site/letter-detail";
    }

    @RequestMapping(path = "/message/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendMessage(String toName, String content) {
        Message message = new Message();
        message.setContent(content);
        User target = userService.findUserByUsername(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(400, "目标用户不存在");
        }
        message.setToId(target.getId());
        User user = hostHolder.getUser();
        message.setFromId(user.getId());
        message.setCreateTime(new Date());
        int toId = message.getToId();
        int fromId = message.getFromId();
        if (toId > fromId) {
            message.setConversationId(fromId + "_" + toId);
        } else {
            message.setConversationId(toId + "_" + fromId);
        }
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(200, "发送私信成功");
    }

    @RequestMapping(path = "/message/delete", method = RequestMethod.POST)
    @ResponseBody
    public String deleteMsg(List<Integer> ids) {
        messageService.updateMsgStatus(ids, 1);
        return CommunityUtil.getJSONString(200, "删除成功");
    }

    @RequestMapping(path = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        Message message = messageService.findLatestNotice(user.getId(), TOPIC_COMMENT);
        Map<String, Object> messageVO = new HashMap<>();
        if (message != null) {
            messageVO.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("count", count);
            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_COMMENT);
            messageVO.put("unread", unread);
        }
        model.addAttribute("commentNotice", messageVO);

        message = messageService.findLatestNotice(user.getId(), TOPIC_LIKE);
        messageVO = new HashMap<>();
        if (message != null) {
            messageVO.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            messageVO.put("postId", data.get("postId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_LIKE);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_LIKE);
            messageVO.put("unread", unread);
        }
        model.addAttribute("likeNotice", messageVO);

        message = messageService.findLatestNotice(user.getId(), TOPIC_FOLLOW);
        messageVO = new HashMap<>();
        if (message != null) {
            messageVO.put("message", message);
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));

            int count = messageService.findNoticeCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("count", count);

            int unread = messageService.findNoticeUnreadCount(user.getId(), TOPIC_FOLLOW);
            messageVO.put("unread", unread);
        }
        model.addAttribute("followNotice", messageVO);

        int letterUnreadCount = messageService.findUnreadLetterCount(user.getId(), null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        return "/site/notice";
    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable String topic, Page page, Model model) {
        User user = hostHolder.getUser();
        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));
        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                map.put("notice", notice);
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                map.put("fromUser", userService.findUserById(notice.getFromId()));
                noticeVoList.add(map);
            }
        }
        model.addAttribute("noticeVo",noticeVoList);
        List<Integer> ids = getUpdateIds(noticeList);
        if (!ids.isEmpty()) messageService.readMessage(ids, 1);
        return "/site/notice-detail";
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.valueOf(ids[0]);
        int id1 = Integer.valueOf(ids[1]);
        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    private List<Integer> getUpdateIds(List<Message> messageList) {
        List<Integer> updateIds = new ArrayList<>();
        if (messageList != null) {
            for (Message message : messageList) {
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    updateIds.add(message.getId());
                }
            }
        }
        return updateIds;
    }
}
