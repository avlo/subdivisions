package com.prosilion.subdivisions;

import com.prosilion.subdivisions.event.EventPublisher;
import com.prosilion.subdivisions.util.Factory;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PublicKey;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
class EventNoOpMessageTest {
  private final EventPublisher eventPublisher;

  private final String authorPubKey;
  private final String eventId;

  @Autowired
  public EventNoOpMessageTest(@Value("${afterimage.relay.uri}") String relayUri) throws ExecutionException, InterruptedException {
    this.eventPublisher = new EventPublisher(relayUri);
    this.eventId = Factory.generateRandomHex64String();
    this.authorPubKey = Factory.generateRandomHex64String();
  }

  @Test
  void testEventNoOpMessage() throws IOException {
    String content = Factory.lorumIpsum(getClass());

    GenericEvent genericEvent = new GenericEvent(eventId);
    genericEvent.setContent(content);
    genericEvent.setPubKey(new PublicKey(authorPubKey));
    genericEvent.setCreatedAt(1717357053050L);
    genericEvent.setKind(1);

    log.debug("setup() send event:\n  {}", genericEvent);

    OkMessage okMessage = this.eventPublisher.sendEvent(new EventMessage(genericEvent));
    final String noOpResponse = "afterimage is a nostr-reputation authority relay.  it does not accept events, only requests";

    assertEquals(false, okMessage.getFlag());
    assertEquals(noOpResponse, okMessage.getMessage());
  }
}
