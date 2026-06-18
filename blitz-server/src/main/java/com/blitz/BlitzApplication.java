package com.blitz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.blitz")
public class BlitzApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlitzApplication.class, args);
    }
}
