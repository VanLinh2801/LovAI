package com.lovai.lovaiapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LovAiBackendApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(LovAiBackendApiApplication.class, args);
	}

}
