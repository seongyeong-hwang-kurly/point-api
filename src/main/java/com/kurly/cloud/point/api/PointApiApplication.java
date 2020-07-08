package com.kurly.cloud.point.api;

import com.kurly.cloud.api.common.annotation.EnableKurlyJWTAuth;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableKurlyJWTAuth
@SpringBootApplication
public class PointApiApplication {
  public static void main(String[] args) {
    SpringApplication.run(PointApiApplication.class, args);
  }
}
