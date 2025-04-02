package com.prosilion.subdivisions.config;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:afterimage-relays.properties")
public class AfterimageRelayConfig {
  @Bean
  public Map<String, String> afterimageRelays() {
    ResourceBundle relaysBundle = ResourceBundle.getBundle("afterimage-relays");
    return relaysBundle.keySet().stream()
        .collect(Collectors.toMap(key -> key, relaysBundle::getString));
  }
}
