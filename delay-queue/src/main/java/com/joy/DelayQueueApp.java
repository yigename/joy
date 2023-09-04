package com.joy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author Joy
 */
@SpringBootApplication
@EnableCaching
public class DelayQueueApp {
    public static void main(String[] args) {
        SpringApplication.run(DelayQueueApp.class, args);
    }
}
