package com.vuthevy1209.springmail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@org.springframework.scheduling.annotation.EnableScheduling
public class SpringMailApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringMailApplication.class, args);
	}

}
