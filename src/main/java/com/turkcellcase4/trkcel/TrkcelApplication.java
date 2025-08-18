package com.turkcellcase4.trkcel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.turkcellcase4.trkcel")
@EntityScan(basePackages = "com.turkcellcase4.trkcel")
public class TrkcelApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrkcelApplication.class, args);
	}

}
