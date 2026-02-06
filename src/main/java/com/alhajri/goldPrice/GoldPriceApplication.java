package com.alhajri.goldPrice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class GoldPriceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoldPriceApplication.class, args);
	}

}
