package com.carbonbuddy;

import com.carbonbuddy.config.AppProperties;
import com.carbonbuddy.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, AppProperties.class})
public class CarbonBuddyApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarbonBuddyApplication.class, args);
    }
}
