package com.example.production;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.production")
public class ProductionApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProductionApplication.class, args);
	}
}
