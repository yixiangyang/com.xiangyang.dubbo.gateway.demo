package com.xiangyang.application;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
@EnableDubbo(scanBasePackages = {"com.xiangyang.controller"})
@SpringBootApplication(scanBasePackages = {"com.xiangyang.controller","com.xiangyang.application"})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
