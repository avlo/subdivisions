package com.prosilion.subdivisions.config;

import java.io.File;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
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
    log.debug("SuperconductorRelayConfig instantiated");
  }

  @Bean
  public Map<String, String> superconductorRelays() {
    ResourceBundle relaysBundle = ResourceBundle.getBundle("superconductor-relays");
    Map<String, String> collect = relaysBundle.keySet().stream()
        .collect(Collectors.toMap(key -> key, relaysBundle::getString));
    log.debug("loaded SuperconductorRelayConfig relays = {}", collect);
    return collect;
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
      log.debug("loaded SuperconductorRelayConfig @ServiceConnection @Bean: {}", composeContainer.toString());
      return composeContainer;
    }
  }

//  @Bean
//  public StandardRequestConsolidator superconductorRelaysAggregate(Map<String, String> superconductorRelays) {
//    return new StandardRequestConsolidator(superconductorRelays);
//  }
}
