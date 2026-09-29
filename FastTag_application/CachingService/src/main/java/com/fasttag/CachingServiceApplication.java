package com.fasttag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CachingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CachingServiceApplication.class, args);
    }
}
