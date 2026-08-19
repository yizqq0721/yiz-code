package com.yizqq.yizcode;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yizqq.yizcode.mapper")
public class YizCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(YizCodeApplication.class, args);
    }

}
