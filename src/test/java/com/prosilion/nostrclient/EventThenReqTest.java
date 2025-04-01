package com.prosilion.nostrclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostrclient.util.EventPublisher;
import com.prosilion.nostrclient.util.Factory;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import nostr.base.Command;
import nostr.base.PublicKey;
import nostr.event.filter.AuthorFilter;
import nostr.event.filter.EventFilter;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EoseMessage;
import nostr.event.message.OkMessage;
import nostr.event.message.ReqMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
class EventThenReqTest {
  private final RelaySubscriptionsManager relaySubscriptionsManager;

  private final PublicKey authorPubKey;
  private final String eventId;

  @Autowired
  public EventThenReqTest(@Value("${superconductor.relay.uri}") String relayUri) throws ExecutionException, InterruptedException, IOException {
    final EventPublisher eventPublisher = new EventPublisher(relayUri);
    this.relaySubscriptionsManager = new RelaySubscriptionsManager(relayUri);
    this.eventId = Factory.generateRandomHex64String();
    this.authorPubKey = Factory.createNewIdentity().getPublicKey();

    String content = Factory.lorumIpsum(getClass());
    String globalEventJson =
        "[\"EVENT\",{" +
            "\"id\":\"" + eventId +
            "\",\"kind\":1,\"content\":\"" + content +
            "\",\"pubkey\":\"" + authorPubKey.toHexString() +
            "\",\"created_at\":1717357053050" +
            ",tags:[]" +
            ",sig:\"86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546\"}]";
    log.debug("setup() send event:\n  {}", globalEventJson);

    OkMessage okMessage = eventPublisher.createEvent(globalEventJson);
    assertTrue(okMessage.getFlag());
    assertEquals(eventId, okMessage.getEventId());
    assertEquals("success: request processed", okMessage.getMessage());
  }

  @Test
  void testReqFilteredByEventAndAuthor() throws JsonProcessingException {
    String subscriberId = Factory.generateRandomHex64String();

    Map<Command, String> returnedJsonMap = relaySubscriptionsManager.sendRequest(
        new ReqMessage(subscriberId,
            new Filters(
                new AuthorFilter<>(authorPubKey))));

    log.debug("returnedJsonMap testReqFilteredByEventAndAuthor():");
    log.debug("  {}", returnedJsonMap);
    assertTrue(returnedJsonMap.get(Command.EVENT).contains(eventId));
    assertTrue(returnedJsonMap.get(Command.EVENT).contains(authorPubKey.toHexString()));
    assertTrue(returnedJsonMap.get(Command.EOSE).contains(subscriberId));

    assertFalse(returnedJsonMap.get(Command.EVENT).contains(eventId + "X"));
    assertFalse(returnedJsonMap.get(Command.EOSE).contains(subscriberId + "X"));
  }

  @Test
  void testReqNonMatchingEvent() throws JsonProcessingException {
    String subscriberId = Factory.generateRandomHex64String();
    String aNonExistentEventId = Factory.generateRandomHex64String();
    GenericEvent event = new GenericEvent(aNonExistentEventId);

    Map<Command, String> returnedJsonMap = relaySubscriptionsManager.sendRequest(
        new ReqMessage(subscriberId,
            new Filters(
                new EventFilter<>(event))));

    log.info("returnedJsonMap testReqNonMatchingEvent():");
    log.info("  {}", returnedJsonMap);
    assertFalse(returnedJsonMap.containsKey(Command.EVENT));
    assertFalse(returnedJsonMap.get(Command.EOSE).isEmpty());
  }

  @Test
  void testCulledSubscriberId() throws JsonProcessingException {
    String subscriberId = Factory.generateRandomHex64String();
    String aNonExistentEventId = Factory.generateRandomHex64String();
    GenericEvent event = new GenericEvent(aNonExistentEventId);

    assertThrows(IllegalArgumentException.class, () -> new ReqMessage(subscriberId + "123456",
        new Filters(
            new EventFilter<>(event))));

    Map<Command, String> returnedJsonMap = relaySubscriptionsManager.sendRequest(
        new ReqMessage(subscriberId,
            new Filters(
                new EventFilter<>(event))));

    log.info("returnedJsonMap testCulledSubscriberId():");
    log.info("  {}", returnedJsonMap);
    assertFalse(returnedJsonMap.containsKey(Command.EVENT));
    assertEquals(new EoseMessage(returnedJsonMap.get(Command.EOSE)).getSubscriptionId(), subscriberId);
  }
}
