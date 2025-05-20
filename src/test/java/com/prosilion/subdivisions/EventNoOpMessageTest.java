package com.prosilion.subdivisions;

import com.prosilion.subdivisions.config.SuperconductorRelayConfig;
import com.prosilion.subdivisions.client.standard.StandardEventPublisher;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(SuperconductorRelayConfig.class)
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
class EventNoOpMessageTest {

  private final StandardEventPublisher standardEventPublisher;

  private final static Identity identity = Factory.createNewIdentity();

  @Autowired
  public EventNoOpMessageTest(
      @NonNull @Value("${superconductor.relay.uri}") String relayUri) throws ExecutionException, InterruptedException {
    this.standardEventPublisher = new StandardEventPublisher(relayUri);
  }

  @Test
  void testEventNoOpMessage() throws IOException {
    String content = Factory.lorumIpsum(getClass());

    GenericEvent genericEvent = new NIP01(identity).createTextNoteEvent(content).sign().getEvent();

    log.debug("setup() send event:\n  {}", genericEvent);

    OkMessage okMessage = this.standardEventPublisher.sendEvent(new EventMessage(genericEvent));

    assertEquals(true, okMessage.getFlag());
    assertEquals("success: request processed", okMessage.getMessage());
  }
}

