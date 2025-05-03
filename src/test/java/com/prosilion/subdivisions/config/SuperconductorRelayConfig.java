package com.prosilion.subdivisions.config;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@Slf4j
@TestConfiguration
public class SuperconductorRelayConfig {

  public SuperconductorRelayConfig() {
    System.out.println("SuperconductorRelayConfig");
  }

  @Lazy
  @Bean
//  @RestartScope
  @ServiceConnection
  public ComposeContainer superconductorContainer() {
    try (
        ComposeContainer composeContainer = new ComposeContainer(
            new File("src/test/resources/superconductor-docker-compose-dev_ws_old.yml"))
            .withExposedService("superconductor", 5555, Wait.forHealthcheck())) {
      return composeContainer;
    }
  }

//  @Bean
//  public RequestConsolidator superconductorRelaysAggregate(Map<String, String> superconductorRelays) {
//    return new RequestConsolidator(superconductorRelays);
//  }
}
