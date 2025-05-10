package com.prosilion.subdivisions.service;

import com.prosilion.subdivisions.client.reactive.ReactiveNostrRelayClient;
import com.prosilion.subdivisions.config.SuperconductorRelayConfig;
import com.prosilion.subdivisions.util.Factory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import nostr.api.NIP01;
import nostr.base.PublicKey;
import nostr.event.filter.AuthorFilter;
import nostr.event.filter.EventFilter;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import nostr.id.Identity;
import org.apache.commons.lang3.StringUtils;
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
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(SuperconductorRelayConfig.class)
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
class NostrRelayReactiveClientTest {
  public static final String ANSI_YELLOW = "\033[1;93m";
  ;
  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_BLUE = "\033[1;34m";
  public static final String ANSI_RED = "\033[1;36m";
  public static final String ANSI_35 = "\033[1;35m";
  //  public static final String ANSI_RED = "\033[0;36m";
  private final ReactiveNostrRelayClient reactiveNostrRelayClient;
  private final static String globalSubscriberId = Factory.generateRandomHex64String();
  private final String relayUri;

  public NostrRelayReactiveClientTest(@Value("${superconductor.relay.uri}") String relayUri) {
    this.reactiveNostrRelayClient = new ReactiveNostrRelayClient(relayUri);
    this.relayUri = relayUri;
  }

  /*
   * notes:
   *  SampleSubscriber registers subscription event, the latter of which also registers subscription event on collectList()
   */
  @Test
  void testEventCreationAndSubscriptionUsingExplicitSubscriber() throws IOException, InterruptedException {
    Identity identity = Factory.createNewIdentity();
    GenericEvent event = new NIP01<>(identity).createTextNoteEvent(Factory.lorumIpsum()).sign().getEvent();
    log.trace("setup() send event:\n  {}", event.toString());

    Flux<String> flux = reactiveNostrRelayClient.sendEvent(new EventMessage(event, event.getId()));

    log.debug("AAAAAAAAAAAAAAAAAAAAAAA");
    printConsole(flux.hashCode());
    log.debug("AAAAAAAAAAAAAAAAAAAAAAA");

//  1) ******* if only below SampleSubscriber is active, OnNext will register 1 EVENT
    SampleSubscriber<String> eventSubscriber = new SampleSubscriber<>();
    flux.subscribe(eventSubscriber);

//  2) ******* if only below collectList() is active, OnNext will register 0 EVENTS
//     note: including below with above subscriber causes TWO EVENTs to become sent
//    List<String> list3 = new ArrayList<>();
//    flux.collectList().subscribe(list3::addAll);
//  3) ******* if both above SampleSubscriber and collectList are active, OnNext will register 2 EVENTS w/ same ID

//    String expected = "[\"OK\",\"" + event.getId() + "\",true,\"success: request processed\"]";
////    assertEquals(1, list3.stream().filter(s -> s.contains(expected)).count());
//    assertEquals(1, list3.size());
    TimeUnit.MILLISECONDS.sleep(250);
  }

  /*
   * notes:
   *  StepVerifier does not register subscription event
   */
  @Test
  void testEventCreationAndSubscriptionUsingStepVerifier() throws IOException, InterruptedException {
    Identity identity = Factory.createNewIdentity();
    GenericEvent event = new NIP01<>(identity).createTextNoteEvent(Factory.lorumIpsum()).sign().getEvent();
    log.trace("setup() send event:\n  {}", event.toString());

    Flux<String> flux = reactiveNostrRelayClient.sendEvent(new EventMessage(event, event.getId()));

    log.debug("BBBBBBBBBBBBBBBBBBBBBBB");
    printConsole(flux.hashCode());
    log.debug("BBBBBBBBBBBBBBBBBBBBBBB");
    SampleSubscriber<String> eventSubscriber = new SampleSubscriber<>();
    flux.subscribe(eventSubscriber);

    String expected = "[\"OK\",\"" + event.getId() + "\",true,\"success: request processed\"]";
    StepVerifier
        .create(flux)
        .expectSubscription()
        .expectNext(expected)
//        .expectNext("done") //        .expectErrorMessage("boom")
//        .verifyComplete(); //        .expectComplete() //        .verify();
    ;
    TimeUnit.MILLISECONDS.sleep(250);
  }

  /*
   * notes:
   *  collectList() neither used nor registers subscription event, however
   *    its log.debug("BBBBBBBBBBBB " + s + "BBBBBBBBBBBB") doesn't print either, needs investigate
   */
  @Test
  void testEventCreationUsingCollectList() throws IOException, InterruptedException {
    Identity identity = Factory.createNewIdentity();
    GenericEvent event = new NIP01<>(identity).createTextNoteEvent(Factory.lorumIpsum()).sign().getEvent();
    log.trace("setup() send event:\n  {}", event.toString());

    Flux<String> flux = reactiveNostrRelayClient.sendEvent(new EventMessage(event, event.getId()));

    log.debug("CCCCCCCCCCCCCCCCCCCCCCC");
    printConsole(flux.hashCode());
    log.debug("-----------------------");
    String blockedFirst = flux.blockFirst();
    parseJson(blockedFirst);
    log.debug("CCCCCCCCCCCCCCCCCCCCCCC");

// below kept as notes for what doesn't work    
//    List<String> listNoBlock = new ArrayList<>();
//    flux.collectList().subscribe(listNoBlock::addAll);  // <------  subscribe() on infinite flux won't return anything

//    List<String> listBlock = flux.collectList().block();  // <------  subscribe() on infinite flux won't return anything
//    listBlock.forEach(s -> log.debug("BBBBBBBBBBBB " + s + "BBBBBBBBBBBB"));

//    List<String> durationBlock = flux.collectList().block(Duration.ofMillis(250)); // hangs
//    List<String> durationBlock = flux.toStream().toList(); // voids reactive and hangs 
    TimeUnit.MILLISECONDS.sleep(250);
  }

  @Test
  void testReqFilteredByEventAndAuthorViaReqMessage() throws IOException {
    log.debug("\nDDDDDDDDDDDDDDDDDDDDDDD");
    log.debug("DDDDDDDDDDDDDDDDDDDDDDD");
    Identity identity = Factory.createNewIdentity();
    String content = Factory.lorumIpsum();
    GenericEvent event = new NIP01<>(identity).createTextNoteEvent(content).sign().getEvent();

    ReactiveNostrRelayClient methodReactiveNostrRelayClient = new ReactiveNostrRelayClient(relayUri);
    Flux<String> eventFlux = methodReactiveNostrRelayClient.sendEvent(new EventMessage(event));//, event.getId()));

//    SampleSubscriber<String> eventSubscriber = new SampleSubscriber<>();
//    eventFlux.subscribe(eventSubscriber);  //  subscriber, causing EVENT emission

    String eventResponse = eventFlux.blockFirst(); //  acts as another subscriber, causing another EVENT to be emitted, dunno why
    log.debug("genericEvent: " + eventResponse);
    assertEquals("[\"OK\",\"" + event.getId() + "\",true,\"success: request processed\"]", eventResponse);
    log.debug("-------------------------");

//    eventSubscriber.dispose();

//    #--------------------- REQ -------------------------
    EventFilter<GenericEvent> eventFilter = new EventFilter<>(event);
    AuthorFilter<PublicKey> authorFilter = new AuthorFilter<>(identity.getPublicKey());

    final String subscriberId = Factory.generateRandomHex64String();

    ReqMessage reqMessage = new ReqMessage(subscriberId, new Filters(eventFilter, authorFilter));
    Flux<GenericEvent> returnedEventsToMethodSubscriberIdFlux = methodReactiveNostrRelayClient.sendRequestReturnEvents(reqMessage);

//    SampleSubscriber<GenericEvent> reqSubscriber = new SampleSubscriber<>();
//    returnedEventsToMethodSubscriberIdFlux.subscribe(reqSubscriber);  //  subscriber, causing REQ emission

    GenericEvent returnedReqGenericEvent = returnedEventsToMethodSubscriberIdFlux.blockFirst();  //  acts as another subscriber, causing another REQ to be emitted, dunno why

//    reqSubscriber.dispose();

    log.debug("+++++++++++++++++++++++++");
    assertNotNull(returnedReqGenericEvent);
    String encode = new EventMessage(returnedReqGenericEvent).encode();
    log.debug(encode);
    log.debug("+++++++++++++++++++++++++");

    assertEquals(returnedReqGenericEvent.getId(), event.getId());
    assertEquals(returnedReqGenericEvent.getContent(), event.getContent());
    assertEquals(returnedReqGenericEvent.getPubKey().toHexString(), event.getPubKey().toHexString());
    log.debug("DDDDDDDDDDDDDDDDDDDDDDD");
    log.debug("DDDDDDDDDDDDDDDDDDDDDDD\n");

//    final SampleSubscriber<GenericEvent> localMethodRequestSubscriber = new SampleSubscriber<>();
//    returnedEventsToMethodSubscriberIdFlux.subscribe(localMethodRequestSubscriber);
//
//    GenericEvent genericEvent1 = returnedEventsToMethodSubscriberIdFlux.blockFirst();
//    localMethodRequestSubscriber.dispose();


//    ReqMessage reqMessage2 = new ReqMessage(globalSubscriberId, new Filters(eventFilter, authorFilter));
//    Flux<GenericEvent> returnedEventsToGlobalSubscriberIdFlux = methodReactiveNostrRelayClient.sendRequestReturnEvents(reqMessage2);
//
//    SampleSubscriber<GenericEvent> globalRequestSubscriber = new SampleSubscriber<>();
//    returnedEventsToGlobalSubscriberIdFlux.subscribe(globalRequestSubscriber);
//
//    log.trace("okMessage to global subscriberId:");
//    log.trace("  " + returnedEventsToGlobalSubscriberIdFlux);
//    GenericEvent genericEvent2 = returnedEventsToGlobalSubscriberIdFlux.blockFirst();
//    assertEquals(event.getId(), genericEvent2.getId());
//    assertEquals(event.getContent(), genericEvent2.getContent());
//    assertEquals(event.getPubKey().toHexString(), genericEvent2.getPubKey().toHexString());
  }

  @Test
  void testReqFilteredByEventAndAuthorViaReqMessageUsingGlobalClient() throws IOException {
    log.debug("\nEEEEEEEEEEEEEEEEEEEEEEEE");
    log.debug("EEEEEEEEEEEEEEEEEEEEEEEE");
    Identity identity = Factory.createNewIdentity();
    String content = Factory.lorumIpsum();
    GenericEvent event = new NIP01<>(identity).createTextNoteEvent(content).sign().getEvent();

    Flux<String> eventFlux = reactiveNostrRelayClient.sendEvent(new EventMessage(event));//, event.getId()));

    String eventResponse = eventFlux.blockFirst(); //  acts as another subscriber, causing another EVENT to be emitted, dunno why
    log.debug("genericEvent: " + eventResponse);
    assertEquals("[\"OK\",\"" + event.getId() + "\",true,\"success: request processed\"]", eventResponse);
    log.debug("-------------------------");

//    #--------------------- REQ -------------------------
    EventFilter<GenericEvent> eventFilter = new EventFilter<>(event);
    AuthorFilter<PublicKey> authorFilter = new AuthorFilter<>(identity.getPublicKey());

    final String subscriberId = Factory.generateRandomHex64String();

    ReqMessage reqMessage = new ReqMessage(subscriberId, new Filters(eventFilter, authorFilter));
    Flux<GenericEvent> returnedEventsToMethodSubscriberIdFlux = reactiveNostrRelayClient.sendRequestReturnEvents(reqMessage);

    GenericEvent returnedReqGenericEvent = returnedEventsToMethodSubscriberIdFlux.blockFirst();  //  acts as another subscriber, causing another REQ to be emitted, dunno why

    log.debug("+++++++++++++++++++++++++");
    assertNotNull(returnedReqGenericEvent);
    String encode = new EventMessage(returnedReqGenericEvent).encode();
    log.debug(encode);
    log.debug("+++++++++++++++++++++++++");

    assertEquals(returnedReqGenericEvent.getId(), event.getId());
    assertEquals(returnedReqGenericEvent.getContent(), event.getContent());
    assertEquals(returnedReqGenericEvent.getPubKey().toHexString(), event.getPubKey().toHexString());
    log.debug("DDDDDDDDDDDDDDDDDDDDDDD");
    log.debug("DDDDDDDDDDDDDDDDDDDDDDD\n");
  }

  final void printConsole(int i) {
    log.debug(" flux hashcode: [ " + ANSI_YELLOW + i + ANSI_RESET + " ]");
  }

  static void parseJson(String value) {
    String subscriberId = value.split(",")[1];
    String strippedStart = StringUtils.stripStart(subscriberId, "\"");
    log.debug(" " + ANSI_35 + StringUtils.stripEnd(strippedStart, "\"") + ANSI_RESET + " " + value.hashCode());
  }

  private class SampleSubscriber<T> extends BaseSubscriber<T> {
    public void hookOnSubscribe(Subscription subscription) {
//      log.debug();
//      log.debug("0000000000000000000000");
//      log.debug(" Subscription object hashCode: [ " + ANSI_BLUE + subscription.hashCode() + ANSI_RESET + " ]");
//      log.debug("0000000000000000000000");
//      log.debug();
      request(1);
    }

    public void hookOnNext(T value) {
//      log.debug("111111111111111111111");
//      String subscriberId = value.toString().split(",")[1];
//      String strippedStart = StringUtils.stripStart(subscriberId, "\"");
//      log.debug(" On Next: " + ANSI_RED + StringUtils.stripEnd(strippedStart, "\"") + ANSI_RESET + " " + value.hashCode());
      request(1);
//      log.debug("111111111111111111111");
//      log.debug();
    }
  }
}
