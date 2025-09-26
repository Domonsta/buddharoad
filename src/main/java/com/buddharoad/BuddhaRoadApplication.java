package com.buddharoad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.buddharoad.controller", "com.buddharoad.service", "com.buddharoad.repository", "com.buddharoad.config", "com.buddharoad.security", "com.buddharoad.domain"}) // ⭐ 명시적으로 스캔할 패키지들을 지정
public class BuddhaRoadApplication {

    public static void main(String[] args) {
        SpringApplication.run(BuddhaRoadApplication.class, args);
    }

}
