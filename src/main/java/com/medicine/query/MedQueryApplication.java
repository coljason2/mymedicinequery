package com.medicine.query;

import com.medicine.query.common.SslUtil;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MedQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MedQueryApplication.class, args);
        SslUtil.disableSslVerification();
    }

}
