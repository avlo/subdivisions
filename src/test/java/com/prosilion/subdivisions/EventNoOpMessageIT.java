package com.prosilion.subdivisions;

import com.prosilion.subdivisions.config.SuperconductorRelayConfig;
import com.prosilion.subdivisions.event.StandardEventPublisher;
import com.prosilion.subdivisions.util.Factory;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.api.NIP01;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@Testcontainers
@SpringBootTest(classes = SuperconductorRelayConfig.class)
@PropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
class EventNoOpMessageIT {

  private final StandardEventPublisher standardEventPublisher;

  private final static Identity identity = Factory.createNewIdentity();

  @Autowired
  public EventNoOpMessageIT(
      ComposeContainer superconductorContainer,
      @NonNull @Value("${superconductor.relay.uri}") String relayUri) throws ExecutionException, InterruptedException {
    String serviceHost = superconductorContainer.getServiceHost("superconductor-subdivisions", 5555);

    log.debug("00000000000000000");
    log.debug("00000000000000000");
    log.debug("serviceHost: {}", serviceHost);
    log.debug("00000000000000000");
    log.debug("00000000000000000");

    this.standardEventPublisher = new StandardEventPublisher(relayUri);
  }

  @Test
  void testEventNoOpMessage() throws IOException {
    String content = Factory.lorumIpsum(getClass());

    GenericEvent genericEvent = new NIP01<>(identity).createTextNoteEvent(content).sign().getEvent();

    log.debug("setup() send event:\n  {}", genericEvent);

    OkMessage okMessage = this.standardEventPublisher.sendEvent(new EventMessage(genericEvent));

    assertEquals(true, okMessage.getFlag());
    assertEquals("success: request processed", okMessage.getMessage());
  }
}
