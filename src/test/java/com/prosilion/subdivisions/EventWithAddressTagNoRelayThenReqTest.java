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
import nostr.event.filter.AddressTagFilter;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import nostr.event.message.ReqMessage;
import nostr.event.tag.AddressTag;
import nostr.event.tag.IdentifierTag;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(SuperconductorRelayConfig.class)
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
class EventWithAddressTagNoRelayThenReqTest {
  private final StandardRelaySubscriptionsManager standardRelaySubscriptionsManager;

  private final PublicKey authorPubKey = Factory.createNewIdentity().getPublicKey();
  private final String eventId = Factory.generateRandomHex64String();
  private final PublicKey addressTagPubKey = Factory.createNewIdentity().getPublicKey();
  private final String uuid = Factory.generateRandomHex64String();

  @Autowired
  public EventWithAddressTagNoRelayThenReqTest(@Value("${superconductor.relay.uri}") String relayUri) throws ExecutionException, InterruptedException, IOException {
    final StandardEventPublisher standardEventPublisher = new StandardEventPublisher(relayUri);
    this.standardRelaySubscriptionsManager = new StandardRelaySubscriptionsManager(relayUri);

    String content = Factory.lorumIpsum(getClass());
    String globalEventJson =
        "[\"EVENT\",{" +
            "\"id\":\"" + eventId +
            "\",\"kind\":1,\"content\":\"" + content +
            "\",\"pubkey\":\"" + authorPubKey.toHexString() +
            "\",\"created_at\":1717357053050" +
            ",\"tags\":[" +
            "[\"a\",\"1:" + addressTagPubKey.toHexString() + ":" + uuid + "\"]" +
            "]," +
            "\"sig\":\"86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546\"}]";

    log.debug("setup() send event:\n  {}", globalEventJson);
    OkMessage okMessage = standardEventPublisher.sendEvent(
        new BaseMessageDecoder<EventMessage>().decode(globalEventJson));
    assertTrue(okMessage.getFlag());
    assertEquals(eventId, okMessage.getEventId());
    assertEquals("success: request processed", okMessage.getMessage());
  }

  @Test
  void testReqFilteredByAddressTagNoRelay() throws JsonProcessingException {
    String subscriberId = Factory.generateRandomHex64String();

    AddressTag addressTag = new AddressTag();
    addressTag.setKind(1);
    addressTag.setPublicKey(addressTagPubKey);
    addressTag.setIdentifierTag(new IdentifierTag(uuid));

    ReqMessage reqMessage = new ReqMessage(subscriberId, new Filters(new AddressTagFilter<>(addressTag)));
    List<BaseMessage> returnedBaseMessages = standardRelaySubscriptionsManager.send(reqMessage);
    List<GenericEvent> returnedEvents = getGenericEvents(returnedBaseMessages);

    log.debug("returnedEvents testReqFilteredByAddressTag():");
    log.debug("  {}", returnedEvents);
    assertEquals(1, returnedEvents.size());
    assertTrue(returnedEvents.toString().contains(eventId));
    assertTrue(returnedEvents.toString().contains(authorPubKey.toHexString()));
    assertFalse(returnedEvents.toString().contains(eventId + "X"));
  }

//  @Test
//  void testReqNonMatchingEvent() throws JsonProcessingException {
//    String subscriberId = Factory.generateRandomHex64String();
//    String aNonExistentEventId = Factory.generateRandomHex64String();
//    GenericEvent event = new GenericEvent(aNonExistentEventId);
//
//    Map<Command, List<String>> returnedJsonMap = standardRelaySubscriptionsManager.sendRequestReturnCommandResultsMap(
//        new ReqMessage(subscriberId,
//            new Filters(
//                new EventFilter<>(event))));
//
//    log.info("returnedJsonMap testReqNonMatchingEvent():");
//    log.info("  {}", returnedJsonMap);
//    assertFalse(returnedJsonMap.containsKey(Command.EVENT));
//    assertFalse(returnedJsonMap.get(Command.EOSE).isEmpty());
//  }

//  @Test
//  void testCulledSubscriberId() throws JsonProcessingException {
//    String subscriberId = Factory.generateRandomHex64String();
//    String aNonExistentEventId = Factory.generateRandomHex64String();
//    GenericEvent event = new GenericEvent(aNonExistentEventId);
//
//    assertThrows(IllegalArgumentException.class, () -> new ReqMessage(subscriberId + "123456",
//        new Filters(
//            new EventFilter<>(event))));
//
//    Map<Command, List<String>> returnedJsonMap = standardRelaySubscriptionsManager.sendRequestReturnCommandResultsMap(
//        new ReqMessage(subscriberId,
//            new Filters(
//                new EventFilter<>(event))));
//
//    log.info("returnedJsonMap testCulledSubscriberId():");
//    log.info("  {}", returnedJsonMap);
//    assertFalse(returnedJsonMap.containsKey(Command.EVENT));
//    assertEquals(new EoseMessage(returnedJsonMap.get(Command.EOSE).getFirst()).getSubscriptionId(), subscriberId);
//  }
}
