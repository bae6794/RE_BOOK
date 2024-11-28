package com.re_book;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ReBookApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReBookApplication.class, args);
    }

}
