package com.logilink.company;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.logilink.company.global.client")
@EnableCaching
public class CompanyApplication {
	public static void main(String[] args) {
		SpringApplication.run(CompanyApplication.class, args);
	}
}
