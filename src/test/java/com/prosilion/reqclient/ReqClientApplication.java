package com.prosilion.reqclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@PropertySource("classpath:application.properties")
public class ReqClientApplication extends SpringBootServletInitializer {

  /**
   * spring-boot WAR hook
   */
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    return builder.sources(ReqClientApplication.class);
  }

  /**
   * spring-boot executable JAR hook
   */
  public static void main(String[] args) {
    SpringApplication
//        .from(AfterimageApplication::main)
//        .with(SuperconductorRelaysConfig.class)
//        .run(args);
        .run(ReqClientApplication.class, args);
  }
}
