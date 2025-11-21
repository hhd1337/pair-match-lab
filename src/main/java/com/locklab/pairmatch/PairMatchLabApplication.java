package com.locklab.pairmatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PairMatchLabApplication {

    public static void main(String[] args) {
        SpringApplication.run(PairMatchLabApplication.class, args);
    }

}
