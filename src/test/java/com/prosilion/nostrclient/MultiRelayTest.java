package com.prosilion.nostrclient;

import com.prosilion.nostrclient.config.SuperconductorRelayConfig;
import com.prosilion.nostrclient.util.EventPublisher;
import com.prosilion.nostrclient.util.Factory;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import nostr.api.factory.impl.NIP01Impl;
import nostr.base.PublicKey;
import nostr.event.impl.TextNoteEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.message.OkMessage;
import nostr.event.tag.VoteTag;
import nostr.id.Identity;
import org.apache.commons.lang3.stream.Streams;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

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

      OkMessage okMessage = eventPublisher.createEvent(globalEventJson);
      assertTrue(okMessage.getFlag());
      assertEquals(eventId, okMessage.getEventId());
      assertEquals("success: request processed", okMessage.getMessage());
    }
  }

  @Test
  void textNoteEventMultiRelayTest() throws IOException {
    List<EventPublisher> eventPublishers = Streams.failableStream(superconductorRelays.values()).map(EventPublisher::new).stream().toList();
    for (EventPublisher eventPublisher : eventPublishers) {
      Identity newIdentity = Factory.createNewIdentity();
      String content = Factory.lorumIpsum(getClass());
      VoteTag voteTag = new VoteTag(1);

      TextNoteEvent textNoteEvent = new NIP01Impl.TextNoteEventFactory(newIdentity, List.of(voteTag), content).create();
      textNoteEvent.setKind(2112);

      String eventId = textNoteEvent.getId();
      String eventJson = new BaseEventEncoder<>(textNoteEvent).encode();

      log.info("textNoteEventMultiRelayTest() send event:\n  {}", eventJson);

      OkMessage okMessage = eventPublisher.createEvent(textNoteEvent);
      assertTrue(okMessage.getFlag());
      assertEquals(eventId, okMessage.getEventId());
      assertEquals("success: request processed", okMessage.getMessage());
    }
  }
}
