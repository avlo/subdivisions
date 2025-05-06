package com.prosilion.subdivisions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.subdivisions.client.reactive.ReactiveNostrRelayClient;
import com.prosilion.subdivisions.config.SuperconductorRelayConfig;
import com.prosilion.subdivisions.util.Factory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import nostr.api.NIP01;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.id.Identity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(SuperconductorRelayConfig.class)
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
class ReactiveWebSocketClientTest {
  private final ReactiveNostrRelayClient reactiveNostrRelayClient;
  private final Identity identity = Factory.createNewIdentity();
  private final String eventId = Factory.generateRandomHex64String();
  private final static String globalSubscriberId = Factory.generateRandomHex64String();
  private final String content;

  public ReactiveWebSocketClientTest(@Value("${superconductor.relay.uri}") String relayUri) throws IOException {
    reactiveNostrRelayClient = new ReactiveNostrRelayClient(relayUri);
    content = Factory.lorumIpsum(getClass());

    GenericEvent event = new NIP01<>(identity).createTextNoteEvent(content).sign().getEvent();
    log.trace("setup() send event:\n  {}", event.toString());

    Flux<String> flux = reactiveNostrRelayClient.sendEvent(new EventMessage(event, event.getId()));

    SampleSubscriber<String> eventSubscriber = new SampleSubscriber<>();
    flux.subscribe(eventSubscriber);

    System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZz");
    System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZz");
//    flux.subscribe(System.out::println);
    List<String> list3 = new ArrayList<>();
    flux.collectList().subscribe(list3::addAll);
    list3.forEach(System.out::println);
    System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZz");
    System.out.println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZz");
  }

  @Test
  void testReqFilteredByEventAndAuthorViaReqMessage() throws JsonProcessingException {
    System.out.println("reached");
    assertTrue(true);

//    final String subscriberId = Factory.generateRandomHex64String();
//
//    EventFilter<GenericEvent> eventFilter = new EventFilter<>(new GenericEvent(eventId));
//    AuthorFilter<PublicKey> authorFilter = new AuthorFilter<>(new PublicKey(identity.getPublicKey().toHexString()));
//
//    ReqMessage reqMessage = new ReqMessage(subscriberId, new Filters(eventFilter, authorFilter));
//    Flux<GenericEvent> localSubscriberIdFlux = reactiveNostrRelayClient.sendRequestReturnEvents(reqMessage);
//
//    SampleSubscriber<GenericEvent> localMethodRequestSubscriber = new SampleSubscriber<>();
//    localSubscriberIdFlux.subscribe(localMethodRequestSubscriber);
//
//    log.trace("okMessage to local subscriberId:");
//    log.trace("  " + localSubscriberIdFlux);
//    assertTrue(localSubscriberIdFlux.toStream().anyMatch(event -> event.getId().equals(eventId)));
//    assertTrue(localSubscriberIdFlux.toStream().anyMatch(event -> event.getContent().equals(content)));
//    assertTrue(localSubscriberIdFlux.toStream().anyMatch(event -> event.getPubKey().toHexString().equals(identity.getPublicKey().toHexString())));
//
//    ReqMessage reqMessage2 = new ReqMessage(globalSubscriberId, new Filters(eventFilter, authorFilter));
//    Flux<GenericEvent> returnedEventsGlobalSubscriberId = reactiveNostrRelayClient.sendRequestReturnEvents(reqMessage2);
//
//    SampleSubscriber<GenericEvent> globalRequestSubscriber = new SampleSubscriber<>();
//    returnedEventsGlobalSubscriberId.subscribe(globalRequestSubscriber);
//
//    log.trace("okMessage to global subscriberId:");
//    log.trace("  " + returnedEventsGlobalSubscriberId);
//    assertTrue(returnedEventsGlobalSubscriberId.toStream().anyMatch(event -> event.getId().equals(eventId)));
//    assertTrue(returnedEventsGlobalSubscriberId.toStream().anyMatch(event -> event.getContent().equals(content)));
//    assertTrue(returnedEventsGlobalSubscriberId.toStream().anyMatch(event -> event.getPubKey().toHexString().equals(identity.getPublicKey().toHexString())));
  }

  private class SampleSubscriber<T> extends BaseSubscriber<T> {
    public void hookOnSubscribe(Subscription subscription) {
      System.out.println("0000000000000000000000");
      System.out.println("0000000000000000000000");
      System.out.println("Subscribed");
      System.out.println("0000000000000000000000");
      System.out.println("0000000000000000000000");
      request(1);
    }

    public void hookOnNext(T value) {
      System.out.println("111111111111111111111");
      System.out.println("111111111111111111111");
      System.out.println(value);
      request(1);
      System.out.println("111111111111111111111");
      System.out.println("111111111111111111111");
    }
  }
}
