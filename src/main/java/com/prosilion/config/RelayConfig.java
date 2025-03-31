package com.prosilion.config;

import java.io.IOException;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
//@TestPropertySources(
@PropertySource("classpath:afterimage-relays.properties")
//)
public class RelayConfig {

  @Bean
  public Map<String, String> afterImageRelays() throws IOException {
    ResourceBundle relaysBundle = ResourceBundle.getBundle("afterimage-relays");
    return relaysBundle.keySet().stream()
        .collect(Collectors.toMap(key -> key, relaysBundle::getString));
  }
}
