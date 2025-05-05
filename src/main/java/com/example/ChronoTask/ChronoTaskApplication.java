package com.example.ChronoTask;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ChronoTaskApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChronoTaskApplication.class, args);
	}

}
