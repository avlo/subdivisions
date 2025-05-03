package com.prosilion.subdivisions;

import com.prosilion.subdivisions.config.SuperconductorRelayConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LocalDevApplication {

//  @Override
//  protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
//    return builder.sources(LocalDevApplication.class);
//  }

  public static void main(String[] args) {
    SpringApplication
        .from(LocalDevApplication::main)
        .with(SuperconductorRelayConfig.class)
        .run(args);
  }
}
