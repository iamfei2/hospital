package com.hospit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.hospit.mapper")
@EnableAsync
@EnableScheduling
public class  HospitApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitApplication.class, args);
    }

}
