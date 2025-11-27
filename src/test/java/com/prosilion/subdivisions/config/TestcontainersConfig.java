package com.prosilion.subdivisions.config;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@Testcontainers
public class TestcontainersConfig {
  @Bean
  @ServiceConnection
  public ComposeContainer composeContainerLocalDev() {
    return new ComposeContainer(
        new File("src/test/resources/subdivisions-docker-compose/superconductor-docker-compose-local_ws.yml"))
        .waitingFor("superconductor-db", Wait.forHealthcheck())
        .waitingFor("superconductor-subdivisions", Wait.forHealthcheck())
        .withRemoveVolumes(true);
  }
}
