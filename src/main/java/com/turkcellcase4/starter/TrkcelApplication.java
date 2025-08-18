package com.turkcellcase4.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.turkcellcase4")
@EnableJpaRepositories(basePackages = "com.turkcellcase4")
@EntityScan(basePackages = "com.turkcellcase4")
@EnableJpaAuditing
public class TrkcelApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrkcelApplication.class, args);
	}
}
