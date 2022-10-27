package com.nowcoder.community;

import com.nowcoder.community.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class SensitiveTest {
    @Resource
    private SensitiveFilter sensitiveFilter;

    @Test
    public void testSensitiveFilter() {
        String test = "这里可以赌※博，可以吸※毒，有毒※品出售，可以吸※烟";
        test = sensitiveFilter.filter(test);
        System.out.println(test);
    }
}
