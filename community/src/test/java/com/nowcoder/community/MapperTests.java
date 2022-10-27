package com.nowcoder.community;

import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.Message;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@SpringBootTest
public class MapperTests {

    @Resource
    private MessageMapper messageMapper;

    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("123123");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
    }

    @Test
    public void testMessageMapper() {
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for(Message message : list){
            System.out.println(message);
        }
        System.out.println(messageMapper.selectConversationCount(111));
        list = messageMapper.selectLetters("111_112", 0, 10);
        for(Message message : list){
            System.out.println(message);
        }
        System.out.println(messageMapper.selectLetterCount("111_112"));
        System.out.println(messageMapper.selectUnreadLetterCount(131,"111_131"));
    }
}
