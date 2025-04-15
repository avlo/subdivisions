package com.prosilion.subdivisions;

import com.prosilion.subdivisions.config.SuperconductorRelayConfig;
import com.prosilion.subdivisions.event.EventPublisher;
import com.prosilion.subdivisions.util.Factory;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PublicKey;
import nostr.event.message.OkMessage;
import nostr.id.Identity;
import org.apache.commons.lang3.stream.Streams;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ActiveProfiles("test")
@SpringJUnitConfig(SuperconductorRelayConfig.class)
public class MultiRelayTest {
  private final Map<String, String> superconductorRelays;

  @Autowired
  public MultiRelayTest(Map<String, String> superconductorRelays) {
    this.superconductorRelays = superconductorRelays;
    superconductorRelays.forEach((k, v) -> log.info("\n\nrelayName:\n  {}\nrelayUri\n  {}\n", k, v));
  }

  @Test
  void multiRelayTest() throws IOException {
    List<EventPublisher> eventPublishers = Streams.failableStream(superconductorRelays.values()).map(EventPublisher::new).stream().toList();
    for (EventPublisher eventPublisher : eventPublishers) {
      String eventId = Factory.generateRandomHex64String();
      Identity newIdentity = Factory.createNewIdentity();
      PublicKey authorPubKey = newIdentity.getPublicKey();

      String content = Factory.lorumIpsum(getClass());
      String globalEventJson =
          "[\"EVENT\",{" +
              "\"id\":\"" + eventId +
              "\",\"kind\":1,\"content\":\"" + content +
              "\",\"pubkey\":\"" + authorPubKey.toHexString() +
              "\",\"created_at\":1717357053050" +
              ",tags:[]" +
              ",sig:\"86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546\"}]";
      log.info("multiRelayTest() send event:\n  {}", globalEventJson);

      OkMessage okMessage = eventPublisher.sendEvent(globalEventJson);
      assertTrue(okMessage.getFlag());
      assertEquals(eventId, okMessage.getEventId());
      assertEquals("success: request processed", okMessage.getMessage());
    }
  }
}
