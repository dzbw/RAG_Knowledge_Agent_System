package com.java1234;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 上下文加载测试（默认禁用：依赖本机 MySQL / Redis / Ollama）。
 */
@SpringBootTest
@Disabled("需要本地 MySQL、Redis Stack、Ollama，默认跳过")
class ServerApplicationTests {

    @Test
    void contextLoads() {
        // intentionally empty
    }

}
