package com.copywriting.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class CopywritingAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(CopywritingAssistantApplication.class, args);
    }
}
