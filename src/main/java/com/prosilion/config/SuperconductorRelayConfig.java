package com.prosilion.config;

import com.prosilion.nostrclient.AggregateSuperconductorRelaysByName;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
//@TestPropertySources(
@PropertySource("classpath:superconductor-relays.properties")
//)
public class SuperconductorRelayConfig {

  @Bean
  public Map<String, String> superconductorRelays() {
    ResourceBundle relaysBundle = ResourceBundle.getBundle("superconductor-relays");
    return relaysBundle.keySet().stream()
        .collect(Collectors.toMap(key -> key, relaysBundle::getString));
  }

  @Bean
  public AggregateSuperconductorRelaysByName superconductorRelaysAggregate(Map<String, String> superconductorRelays) {
    return new AggregateSuperconductorRelaysByName(superconductorRelays);
  }
}
