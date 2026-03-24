package com.simon.lawrag;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.simon.lawrag.mapper")
@EnableAsync
public class LawRagApplication {

    public static void main(String[] args) {
        SpringApplication.run(LawRagApplication.class, args);
    }
}
