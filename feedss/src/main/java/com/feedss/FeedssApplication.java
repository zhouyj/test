package com.feedss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication()
@EnableAsync
public class FeedssApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeedssApplication.class, args);
	}
}
