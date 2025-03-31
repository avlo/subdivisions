package com.prosilion.nostrclient;

import com.prosilion.nostrclient.util.Factory;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.Command;
import nostr.event.message.OkMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(SpringExtension.class)
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
class EventThenReqTest {
  private final RelaySubscriptionsManager relaySubscriptionsManager;

  private final String authorPubKey;
  private final String eventId;

  @Autowired
  public EventThenReqTest(@Value("${superconductor.relay.uri}") String relayUri) throws ExecutionException, InterruptedException, IOException {
    final RelayEventPublisher relayEventPublisher = new RelayEventPublisher(relayUri);
    this.relaySubscriptionsManager = new RelaySubscriptionsManager(relayUri);
    this.eventId = Factory.generateRandomHex64String();
    this.authorPubKey = Factory.generateRandomHex64String();

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

    OkMessage okMessage = relayEventPublisher.createEvent(globalEventJson);
    assertTrue(okMessage.getFlag());
    assertEquals(eventId, okMessage.getEventId());
    assertEquals("success: request processed", okMessage.getMessage());
  }

  @Test
  void testReqFilteredByEventAndAuthor() {
    String subscriberId = Factory.generateRandomHex64String();

    Map<Command, String> returnedJsonMap = relaySubscriptionsManager.sendRequest(
        subscriberId,
        createReqJson(subscriberId, authorPubKey)
    );
    log.debug("returnedJsonMap testReqFilteredByEventAndAuthor():");
    log.debug("  {}", returnedJsonMap);
    assertTrue(returnedJsonMap.get(Command.EVENT).contains(eventId));
    assertTrue(returnedJsonMap.get(Command.EVENT).contains(authorPubKey));
    assertTrue(returnedJsonMap.get(Command.EOSE).contains(subscriberId));
  }

  private String createReqJson(@NonNull String uuid, @NonNull String authorPubkey) {
    return "[\"REQ\",\"" + uuid + "\",{\"ids\":[\"" + eventId + "\"],\"authors\":[\"" + authorPubkey + "\"]}]";
  }

  @Test
  void testReqNonMatchingEvent() {
    String subscriberId = Factory.generateRandomHex64String();
    String aNonExistentEventId = "bbbd79f81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984";

    String nonMatchEventReqJson = createNonMatchEventReqJson(subscriberId, aNonExistentEventId);
    Map<Command, String> returnedJsonMap = relaySubscriptionsManager.sendRequest(
        subscriberId,
        nonMatchEventReqJson
    );
    log.debug("returnedJsonMap testReqNonMatchingEvent():");
    log.debug("  {}", returnedJsonMap);
    assertFalse(returnedJsonMap.containsKey(Command.EVENT));
    assertFalse(returnedJsonMap.get(Command.EOSE).isEmpty());
  }

  private String createNonMatchEventReqJson(@NonNull String subscriberId, @NonNull String nonMatchingEventId) {
    return "[\"REQ\",\"" + subscriberId + "\",{\"ids\":[\"" + nonMatchingEventId + "\"]}]";
  }
}
