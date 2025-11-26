package com.prosilion.subdivisions.config;

import io.github.tobi.laa.spring.boot.embedded.redis.standalone.EmbeddedRedisStandalone;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@Testcontainers
@EmbeddedRedisStandalone
public class TestcontainersConfig {
  @Bean
  @ServiceConnection
  public ComposeContainer composeContainerLocalDev() {
    try (ComposeContainer superconductorContainer =
             new ComposeContainer(
                 new File("src/test/resources/superconductor-docker-compose-local_ws.yml"))) {
      superconductorContainer
          .waitingFor("superconductor-db", Wait.forHealthcheck())
          .waitingFor("superconductor-subdivisions", Wait.forHealthcheck())
          .withRemoveVolumes(true);
      log.debug("{} loaded superconductorContainer started", getClass().getSimpleName());
      return superconductorContainer;
    }
  }
}
