package com.prosilion.subdivisions.config;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@Slf4j
@TestConfiguration
public class SuperconductorRelayConfig {
  @Bean
  public Map<String, String> superconductorRelays() {
    ResourceBundle relaysBundle = ResourceBundle.getBundle("superconductor-relays");
    Map<String, String> collect = relaysBundle.keySet().stream()
        .collect(Collectors.toMap(key -> key, relaysBundle::getString));
    log.debug("loaded SuperconductorRelayConfig relays = {}", collect);
    return collect;
  }

  @Bean
  public String superconductorRelayUrl(@Value("${superconductor.relay.url}") String relayUrl) {
    return relayUrl;
  }
}
