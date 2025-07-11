package com.snh.odiga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class OdigaApplication {

	public static void main(String[] args) {
		SpringApplication.run(OdigaApplication.class, args);
	}

}
