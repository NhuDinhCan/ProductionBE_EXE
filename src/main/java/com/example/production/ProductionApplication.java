package com.example.production;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example.production")
@EnableScheduling
public class ProductionApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProductionApplication.class, args);
	}
}
