package com.kurly.cloud.point.api;

import com.kurly.cloud.api.common.annotation.EnableKurlyJWTAuth;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@EnableKurlyJWTAuth
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class PointApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(PointApiApplication.class, args);
  }
}
