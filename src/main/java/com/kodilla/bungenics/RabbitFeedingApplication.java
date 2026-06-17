package com.kodilla.bungenics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RabbitFeedingApplication {

    public static void main(String[] args) {
        SpringApplication.run(RabbitFeedingApplication.class, args);
    }


}
