package com.prosilion.subdivisions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.subdivisions.client.standard.StandardEventPublisher;
import com.prosilion.subdivisions.client.standard.StandardRelaySubscriptionsManager;
import com.prosilion.subdivisions.config.SuperconductorRelayConfig;
import com.prosilion.subdivisions.util.Factory;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PublicKey;
import nostr.event.BaseMessage;
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
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.prosilion.subdivisions.NostrRelayReactiveClientTest.getGenericEvents;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(SuperconductorRelayConfig.class)
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
class EventThenReqTest<T extends BaseMessage> {
  private final StandardRelaySubscriptionsManager<T> standardRelaySubscriptionsManager;

  private final PublicKey authorPubKey;
  private final String eventId;

  @Autowired
  public EventThenReqTest(@Value("${superconductor.relay.uri}") String relayUri) throws ExecutionException, InterruptedException, IOException {
    final StandardEventPublisher standardEventPublisher = new StandardEventPublisher(relayUri);
    this.standardRelaySubscriptionsManager = new StandardRelaySubscriptionsManager<>(relayUri);
    this.eventId = Factory.generateRandomHex64String();
    this.authorPubKey = Factory.createNewIdentity().getPublicKey();

    String content = Factory.lorumIpsum(getClass());
    String globalEventJson =
        "[\"EVENT\",{" +
            "\"id\":\"" + eventId +
            "\",\"kind\":1,\"content\":\"" + content +
            "\",\"pubkey\":\"" + authorPubKey.toHexString() +
            "\",\"created_at\":1717357053050" +
            ",\"tags\":[]" +
            ",\"sig\":\"86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546\"}]";
    log.debug("setup() send event:\n  {}", globalEventJson);

    OkMessage okMessage = standardEventPublisher.sendEvent(globalEventJson);
    assertTrue(okMessage.getFlag());
    assertEquals(eventId, okMessage.getEventId());
    assertEquals("success: request processed", okMessage.getMessage());
  }

  @Test
  void testReqFilteredByEventAndAuthor() throws JsonProcessingException {
    String subscriberId = Factory.generateRandomHex64String();

    List<T> returnedBaseMessages = standardRelaySubscriptionsManager
        .sendRequestReturnEvents(
            new ReqMessage(subscriberId,
                new Filters(
                    new AuthorFilter<>(authorPubKey))));

    List<GenericEvent> returnedEvents = getGenericEvents(returnedBaseMessages);

    log.debug("returnedBaseMessages testReqFilteredByEventAndAuthor():");
    assertTrue(returnedBaseMessages.stream()
        .filter(EoseMessage.class::isInstance)
        .map(EoseMessage.class::cast)
        .findAny().isPresent());

    assertEquals(1, returnedEvents.size());
    assertTrue(returnedEvents.toString().contains(eventId));
    assertTrue(returnedEvents.toString().contains(authorPubKey.toHexString()));
    assertFalse(returnedEvents.toString().contains(eventId + "X"));
    assertFalse(returnedEvents.toString().contains(subscriberId + "X"));
  }

  @Test
  void testReqNonMatchingEvent() throws JsonProcessingException {
    String subscriberId = Factory.generateRandomHex64String();
    String aNonExistentEventId = Factory.generateRandomHex64String();
    GenericEvent event = new GenericEvent(aNonExistentEventId);

    List<T> returnedBaseMessages = standardRelaySubscriptionsManager
        .sendRequestReturnEvents(
            new ReqMessage(subscriberId,
                new Filters(
                    new EventFilter<>(event))));

    List<GenericEvent> returnedEvents = getGenericEvents(returnedBaseMessages);

    log.debug("returnedBaseMessages testReqFilteredByEventAndAuthor():");
    assertTrue(returnedBaseMessages.stream()
        .filter(EoseMessage.class::isInstance)
        .map(EoseMessage.class::cast)
        .findAny().isPresent());

    assertTrue(returnedEvents.isEmpty());
  }

  @Test
  void testCulledSubscriberId() throws JsonProcessingException {
    String subscriberId = Factory.generateRandomHex64String();
    String aNonExistentEventId = Factory.generateRandomHex64String();
    GenericEvent event = new GenericEvent(aNonExistentEventId);

    assertThrows(IllegalArgumentException.class, () -> new ReqMessage(subscriberId + "123456",
        new Filters(
            new EventFilter<>(event))));

    ReqMessage reqMessage = new ReqMessage(subscriberId,
        new Filters(
            new EventFilter<>(event)));

    List<T> returnedBaseMessages = standardRelaySubscriptionsManager.sendRequestReturnEvents(reqMessage);
    List<GenericEvent> returnedEvents = getGenericEvents(returnedBaseMessages);

    log.debug("returnedBaseMessages testReqFilteredByEventAndAuthor():");
    assertTrue(returnedBaseMessages.stream()
        .filter(EoseMessage.class::isInstance)
        .map(EoseMessage.class::cast)
        .findAny().isPresent());

    assertTrue(returnedEvents.isEmpty());
  }
}
