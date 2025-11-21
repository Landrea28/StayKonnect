package com.staykonnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StayKonnectApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(StayKonnectApplication.class, args);
    }
}