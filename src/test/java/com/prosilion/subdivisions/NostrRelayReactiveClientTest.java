package com.prosilion.subdivisions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.NostrException;
import com.prosilion.nostr.event.EventIF;
import com.prosilion.nostr.event.GenericEventId;
import com.prosilion.nostr.event.TextNoteEvent;
import com.prosilion.nostr.filter.Filters;
import com.prosilion.nostr.filter.event.AuthorFilter;
import com.prosilion.nostr.filter.event.EventFilter;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.EventMessage;
import com.prosilion.nostr.message.OkMessage;
import com.prosilion.nostr.message.ReqMessage;
import com.prosilion.nostr.user.Identity;
import com.prosilion.subdivisions.client.reactive.ReactiveNostrRelayClient;
import com.prosilion.subdivisions.config.SuperconductorRelayConfig;
import com.prosilion.subdivisions.util.Factory;
import com.prosilion.subdivisions.util.TestSubscriber;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(SuperconductorRelayConfig.class)
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
class NostrRelayReactiveClientTest {
  private final String relayUri;

  public NostrRelayReactiveClientTest(@Value("${superconductor.relay.uri}") String relayUri) {
    this.relayUri = relayUri;
  }

  @Test
  void testQueryNonExistentEventReturnsEmptyList() throws JsonProcessingException, NostrException {
    EventFilter eventFilter = new EventFilter(Factory.createGenericEventId());
    AuthorFilter authorFilter = new AuthorFilter(Factory.createNewIdentity().getPublicKey());

    final String subscriberId = Factory.generateRandomHex64String();

    ReqMessage reqMessage = new ReqMessage(subscriberId, new Filters(eventFilter, authorFilter));
    TestSubscriber<BaseMessage> reqSubscriber = new TestSubscriber<>();
    ReactiveNostrRelayClient nostrRelayService = new ReactiveNostrRelayClient(relayUri);

    nostrRelayService.send(reqMessage, reqSubscriber);
    List<EventIF> genericEvents = getGenericEvents(reqSubscriber.getItems());

    assertTrue(genericEvents.isEmpty());
  }

  @Test
  void testEventCreation() throws IOException, NostrException, NoSuchAlgorithmException {
    Identity identity = Factory.createNewIdentity();
    TextNoteEvent event = new TextNoteEvent(identity, Factory.lorumIpsum());

    TestSubscriber<OkMessage> okMessageSubscriber = new TestSubscriber<>();
    new ReactiveNostrRelayClient(relayUri).send(new EventMessage(event), okMessageSubscriber);

    assertEquals(
        new OkMessage(event.getId(), true, "success: request processed").encode(),
        okMessageSubscriber.getItems().getFirst().encode());
  }

  @Test
  void testReqFilteredByEventAndAuthor() throws IOException, NostrException, NoSuchAlgorithmException {
    Identity identity = Factory.createNewIdentity();
    TextNoteEvent event = new TextNoteEvent(identity, Factory.lorumIpsum());

    TestSubscriber<OkMessage> eventSubscriber = new TestSubscriber<>();
    ReactiveNostrRelayClient superconductorReactiveNostrRelayClient = new ReactiveNostrRelayClient(relayUri);

    superconductorReactiveNostrRelayClient.send(new EventMessage(event), eventSubscriber);

    List<OkMessage> items = eventSubscriber.getItems();
    OkMessage okMessage = new OkMessage(event.getId(), true, "success: request processed");
    assertEquals(okMessage.encode(), items.getFirst().encode());

//  #--------------------- REQ -------------------------
    Filters filters = new Filters(
        new AuthorFilter(identity.getPublicKey()),
        new EventFilter(new GenericEventId(event.getId())));

    final String subscriberId = Factory.generateRandomHex64String();

    ReqMessage reqMessage = new ReqMessage(subscriberId, filters);
    TestSubscriber<ReqMessage> reqSubscriber = new TestSubscriber<>();
    superconductorReactiveNostrRelayClient.send(reqMessage, reqSubscriber);

    List<EventIF> returnedReqGenericEvents = getGenericEvents(reqSubscriber.getItems());

    assertEquals(returnedReqGenericEvents.getFirst().getId(), event.getId());
    assertEquals(returnedReqGenericEvents.getFirst().getContent(), event.getContent());
    assertEquals(returnedReqGenericEvents.getFirst().getPublicKey().toHexString(), event.getPublicKey().toHexString());
  }

  public static <T extends BaseMessage> List<EventIF> getGenericEvents(List<T> returnedBaseMessages) {
    return returnedBaseMessages.stream()
        .filter(EventMessage.class::isInstance)
        .map(EventMessage.class::cast)
        .map(EventMessage::getEvent)
        .toList();
  }

  @Test
  <T extends BaseMessage> void testTwoEventsFilteredByEventAndAuthorUsingTwoEventSubscribers() throws IOException, NostrException, NoSuchAlgorithmException {
    Identity identity = Factory.createNewIdentity();
    String content1 = Factory.lorumIpsum();
    ReactiveNostrRelayClient superconductorReactiveNostrRelayClient = new ReactiveNostrRelayClient(relayUri);

//    # -------------- EVENT 1 of 3 -------------------
    TextNoteEvent event1 = new TextNoteEvent(identity, content1);

    TestSubscriber<OkMessage> event1Subscriber = new TestSubscriber<>();
    superconductorReactiveNostrRelayClient.send(new EventMessage(event1), event1Subscriber);

    List<OkMessage> event1SubscriberItems = event1Subscriber.getItems();
    OkMessage okMessage1 = new OkMessage(event1.getId(), true, "success: request processed");
    assertEquals(okMessage1.encode(), event1SubscriberItems.getFirst().encode());

//    # -------------- EVENT 2 of 3 -------------------
    String content2 = Factory.lorumIpsum();
    TextNoteEvent event2 = new TextNoteEvent(identity, content2);

    TestSubscriber<OkMessage> event2Subscriber = new TestSubscriber<>();
    superconductorReactiveNostrRelayClient.send(new EventMessage(event2), event2Subscriber);

    List<OkMessage> event2SubscriberItems = event2Subscriber.getItems();
    OkMessage okMessage2 = new OkMessage(event2.getId(), true, "success: request processed");
    assertEquals(okMessage2.encode(), event2SubscriberItems.getFirst().encode());

//    # -------------- EVENT 3 of 3 -------------------
    String content3 = Factory.lorumIpsum();
    TextNoteEvent event3 = new TextNoteEvent(identity, content3);

    TestSubscriber<OkMessage> event3Subscriber = new TestSubscriber<>();
    superconductorReactiveNostrRelayClient.send(new EventMessage(event3), event3Subscriber);

    List<OkMessage> event3SubscriberItems = event3Subscriber.getItems();
    OkMessage okMessage3 = new OkMessage(event3.getId(), true, "success: request processed");
    assertEquals(okMessage3.encode(), event3SubscriberItems.getFirst().encode());

//  #--------------------- REQ -------------------------
    final String subscriberId = Factory.generateRandomHex64String();

    Filters filters = new Filters(
        new AuthorFilter(identity.getPublicKey()),
        new EventFilter(new GenericEventId(event1.getId())),
        new EventFilter(new GenericEventId(event2.getId())));

    ReqMessage reqMessage = new ReqMessage(subscriberId, filters);

    TestSubscriber<T> reqSubscriber = new TestSubscriber<>();
    superconductorReactiveNostrRelayClient.send(reqMessage, reqSubscriber);

    List<EventIF> returnedReqGenericEvents = getGenericEvents(reqSubscriber.getItems());
    log.debug("size: [{}]", returnedReqGenericEvents.size());

    assertTrue(returnedReqGenericEvents.stream().anyMatch(event -> event.getId().equals(event1.getId())));
    assertTrue(returnedReqGenericEvents.stream().anyMatch(event -> event.getContent().equals(event1.getContent())));
    assertTrue(returnedReqGenericEvents.stream().anyMatch(event -> event.getPublicKey().toHexString().equals(event1.getPublicKey().toHexString())));
  }
}
