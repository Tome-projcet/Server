package com.Tome.tome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
@EnableJpaAuditing
public class TomeApplication {

	public static void main(String[] args) {
		SpringApplication.run(TomeApplication.class, args);
	}

}
