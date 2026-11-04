package com.medicine.query;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class MedQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedQueryApplication.class, args);
    }

}
