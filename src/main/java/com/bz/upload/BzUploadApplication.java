package com.bz.upload;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.bz.dao.mapper")
@EnableCaching
public class BzUploadApplication {
 
	public static void main(String[] args) {
		SpringApplication.run(BzUploadApplication.class, args);
	}
}
