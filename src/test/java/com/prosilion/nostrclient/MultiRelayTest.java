package com.prosilion.nostrclient;


import com.prosilion.nostrclient.config.SuperconductorRelayConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import nostr.config.RelayConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
//@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
@SpringJUnitConfig(SuperconductorRelayConfig.class)
public class MultiRelayTest {
  private final Map<String, String> superconductorRelays;

  @Autowired
  public MultiRelayTest(Map<String, String> superconductorRelays) {
    this.superconductorRelays = superconductorRelays;
    superconductorRelays.forEach((k, v) -> log.info("{}={}", k, v));
  }
  
  @Test
  void doThaDo() {
    assertEquals(1,1); 
  }
}
