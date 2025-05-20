package com.prosilion.subdivisions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.subdivisions.client.reactive.ReactiveNostrRelayClient;
import com.prosilion.subdivisions.config.SuperconductorRelayConfig;
import com.prosilion.subdivisions.util.Factory;
import com.prosilion.subdivisions.util.TestSubscriber;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nostr.api.NIP01;
import nostr.base.PublicKey;
import nostr.event.BaseMessage;
import nostr.event.filter.AuthorFilter;
import nostr.event.filter.EventFilter;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import nostr.event.message.ReqMessage;
import nostr.id.Identity;
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
  <T extends BaseMessage> void testQueryNonExistentEventReturnsEmptyList() throws JsonProcessingException {
    EventFilter<GenericEvent> eventFilter = new EventFilter<>(Factory.createGenericEvent());
    AuthorFilter<PublicKey> authorFilter = new AuthorFilter<>(Factory.createNewIdentity().getPublicKey());

    final String subscriberId = Factory.generateRandomHex64String();

    ReqMessage reqMessage = new ReqMessage(subscriberId, new Filters(eventFilter, authorFilter));
    TestSubscriber<T> reqSubscriber = new TestSubscriber<>();
    ReactiveNostrRelayClient nostrRelayService = new ReactiveNostrRelayClient(relayUri);

    nostrRelayService.send(reqMessage, reqSubscriber);
    List<GenericEvent> genericEvents = getGenericEvents(reqSubscriber.getItems());

    assertTrue(genericEvents.isEmpty());
  }

  @Test
  <T extends BaseMessage> void testEventCreation() throws IOException {
    Identity identity = Factory.createNewIdentity();
    GenericEvent event = new NIP01(identity).createTextNoteEvent(Factory.lorumIpsum()).sign().getEvent();

    TestSubscriber<OkMessage> okMessageSubscriber = new TestSubscriber<>();
    new ReactiveNostrRelayClient(relayUri).send(new EventMessage(event), okMessageSubscriber);

    assertEquals(
        new OkMessage(event.getId(), true, "success: request processed").encode(),
        okMessageSubscriber.getItems().getFirst().encode());
  }

  @Test
  <T extends BaseMessage> void testReqFilteredByEventAndAuthor() throws IOException {
    Identity identity = Factory.createNewIdentity();
    String content = Factory.lorumIpsum();
    GenericEvent event = new NIP01(identity).createTextNoteEvent(content).sign().getEvent();

    ReactiveNostrRelayClient superconductorReactiveNostrRelayClient = new ReactiveNostrRelayClient(relayUri);
    TestSubscriber<OkMessage> eventSubscriber = new TestSubscriber<>();
    superconductorReactiveNostrRelayClient.send(new EventMessage(event), eventSubscriber);//, event.getId()));

    List<OkMessage> items = eventSubscriber.getItems();
    OkMessage okMessage = new OkMessage(event.getId(), true, "success: request processed");
    assertEquals(okMessage.encode(), items.getFirst().encode());

//  #--------------------- REQ -------------------------
    EventFilter<GenericEvent> eventFilter = new EventFilter<>(event);
    AuthorFilter<PublicKey> authorFilter = new AuthorFilter<>(identity.getPublicKey());

    final String subscriberId = Factory.generateRandomHex64String();

    ReqMessage reqMessage = new ReqMessage(subscriberId, new Filters(eventFilter, authorFilter));
    TestSubscriber<T> reqSubscriber = new TestSubscriber<>();
    superconductorReactiveNostrRelayClient.send(reqMessage, reqSubscriber);

    List<GenericEvent> returnedReqGenericEvents = getGenericEvents(reqSubscriber.getItems());

    assertEquals(returnedReqGenericEvents.getFirst().getId(), event.getId());
    assertEquals(returnedReqGenericEvents.getFirst().getContent(), event.getContent());
    assertEquals(returnedReqGenericEvents.getFirst().getPubKey().toHexString(), event.getPubKey().toHexString());
  }

  public static <T extends BaseMessage> List<GenericEvent> getGenericEvents(List<T> returnedBaseMessages) {
    return returnedBaseMessages.stream()
        .filter(EventMessage.class::isInstance)
        .map(EventMessage.class::cast)
        .map(EventMessage::getEvent)
        .map(GenericEvent.class::cast)
        .toList();
  }

  @Test
  <T extends BaseMessage> void testTwoEventsFilteredByEventAndAuthorUsingTwoEventSubscribers() throws IOException {
    Identity identity = Factory.createNewIdentity();
    String content1 = Factory.lorumIpsum();
    ReactiveNostrRelayClient superconductorReactiveNostrRelayClient = new ReactiveNostrRelayClient(relayUri);

//    # -------------- EVENT 1 of 3 -------------------
    GenericEvent event1 = new NIP01(identity).createTextNoteEvent(content1).sign().getEvent();

    TestSubscriber<OkMessage> event1Subscriber = new TestSubscriber<>();
    superconductorReactiveNostrRelayClient.send(new EventMessage(event1), event1Subscriber);//, event.getId()));

    List<OkMessage> event1SubscriberItems = event1Subscriber.getItems();
    OkMessage okMessage1 = new OkMessage(event1.getId(), true, "success: request processed");
    assertEquals(okMessage1.encode(), event1SubscriberItems.getFirst().encode());

//    # -------------- EVENT 2 of 3 -------------------
    String content2 = Factory.lorumIpsum();
    GenericEvent event2 = new NIP01(identity).createTextNoteEvent(content2).sign().getEvent();

    TestSubscriber<OkMessage> event2Subscriber = new TestSubscriber<>();
    superconductorReactiveNostrRelayClient.send(new EventMessage(event2), event2Subscriber);//, event.getId()));

    List<OkMessage> event2SubscriberItems = event2Subscriber.getItems();
    OkMessage okMessage2 = new OkMessage(event2.getId(), true, "success: request processed");
    assertEquals(okMessage2.encode(), event2SubscriberItems.getFirst().encode());

//    # -------------- EVENT 3 of 3 -------------------
    String content3 = Factory.lorumIpsum();
    GenericEvent event3 = new NIP01(identity).createTextNoteEvent(content3).sign().getEvent();

    TestSubscriber<OkMessage> event3Subscriber = new TestSubscriber<>();
    superconductorReactiveNostrRelayClient.send(new EventMessage(event3), event3Subscriber);//, event.getId()));

    List<OkMessage> event3SubscriberItems = event3Subscriber.getItems();
    OkMessage okMessage3 = new OkMessage(event3.getId(), true, "success: request processed");
    assertEquals(okMessage3.encode(), event3SubscriberItems.getFirst().encode());

//  #--------------------- REQ -------------------------
    EventFilter<GenericEvent> event1Filter = new EventFilter<>(event1);
    AuthorFilter<PublicKey> authorFilter = new AuthorFilter<>(identity.getPublicKey());

    EventFilter<GenericEvent> event2Filter = new EventFilter<>(event2);

    final String subscriberId = Factory.generateRandomHex64String();

//    ReqMessage reqMessage = new ReqMessage(subscriberId,
//        List.of(
//            new Filters(event1Filter, authorFilter),
//            new Filters(event2Filter, authorFilter)));

    ReqMessage reqMessage = new ReqMessage(subscriberId, new Filters(event1Filter, event2Filter, authorFilter));

    TestSubscriber<T> reqSubscriber = new TestSubscriber<>();
    superconductorReactiveNostrRelayClient.send(reqMessage, reqSubscriber);

    List<GenericEvent> returnedReqGenericEvents = getGenericEvents(reqSubscriber.getItems());
    log.debug("size: [{}]", returnedReqGenericEvents.size());

    assertTrue(returnedReqGenericEvents.stream().anyMatch(event -> event.getId().equals(event1.getId())));
    assertTrue(returnedReqGenericEvents.stream().anyMatch(event -> event.getContent().equals(event1.getContent())));
    assertTrue(returnedReqGenericEvents.stream().anyMatch(event -> event.getPubKey().toHexString().equals(event1.getPubKey().toHexString())));
  }
}
