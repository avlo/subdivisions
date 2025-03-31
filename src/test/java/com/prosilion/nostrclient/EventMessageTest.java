package com.prosilion.nostrclient;

import com.prosilion.nostrclient.util.Factory;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import nostr.event.message.OkMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
class EventMessageTest {
  private final RelayEventClient relayEventClient;

  private final String authorPubKey;
  private final String eventId;

  @Autowired
  public EventMessageTest(@Value("${superconductor.relay.uri}") String relayUri) throws ExecutionException, InterruptedException {
    this.relayEventClient = new RelayEventClient(relayUri);
    this.eventId = Factory.generateRandomHex64String();
    this.authorPubKey = Factory.generateRandomHex64String();
  }

  @Test
  void testEventNoOpMessage() throws IOException {
    String content = Factory.lorumIpsum(getClass());
    String globalEventJson =
        "[\"EVENT\",{" +
            "\"id\":\"" + eventId +
            "\",\"kind\":1,\"content\":\"" + content +
            "\",\"pubkey\":\"" + authorPubKey +
            "\",\"created_at\":1717357053050" +
            ",tags:[]" +
            ",sig:\"86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546\"}]";
    log.debug("setup() send event:\n  {}", globalEventJson);

    OkMessage okMessage = this.relayEventClient.createEvent(globalEventJson);
    assertTrue(okMessage.getFlag());
    assertEquals(eventId, okMessage.getEventId());
    assertEquals("success: request processed", okMessage.getMessage());
  }
}
