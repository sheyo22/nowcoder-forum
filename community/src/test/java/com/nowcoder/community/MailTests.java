package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;

@SpringBootTest
public class MailTests {

    @Resource
    private MailClient mailClient;

    @Resource
    private TemplateEngine templateEngine;

    @Test
    public void testTextMail() {
        mailClient.sendMail("202021014774@mail.scut.edu.cn", "TEST", "hello world");
    }

    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "xueyou");
        String content = templateEngine.process("/mail/activation", context);
        System.out.println(content);
        mailClient.sendMail("202021014774@mail.scut.edu.cn", "激活码", content);
    }
}
