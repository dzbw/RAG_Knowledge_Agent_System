package com.java1234;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Java1234 RAG 企业知识库问答系统 — 启动类。
 * 扫描 MyBatis Mapper 接口。
 */
@SpringBootApplication
@MapperScan("com.java1234.mapper")
public class ServerApplication {

    /**
     * 应用入口。
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

}
