package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;

import java.util.List;

public interface MessageMapper {

    //查询会话列表
    List<Message> selectConversations(int userId, int offset, int limit);

    //查询会话数量
    int selectConversationCount(int userId);

    //查询一个会话所包含的私信
    List<Message> selectLetters(String conversationId, int offset, int limit);

    //查询一个会话包含的私信数量
    int selectLetterCount(String conversationId);

    //查询一个会话未读的私信数量
    int selectUnreadLetterCount(int userId, String conversationId);

    int insertMessage(Message message);

    int updateStatus(List<Integer> ids,int status);

    //查询某主题下最新的通知
    Message selectLatestNotice(int userId,String topic);

    //查询某主题所包含的通知的数量
    int selectNoticeCount(int userId,String topic);

    //查询未读的通知的数量
    int selectUnreadNoticeCount(int userId,String topic);

    //查询某主题包含的通知列表
    List<Message> selectNotices(int userId,String topic,int offset, int limit);
}

