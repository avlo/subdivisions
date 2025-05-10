package com.prosilion.subdivisions.client.reactive;

import com.prosilion.subdivisions.client.reactive.ReactiveNostrRelayClient;
import com.prosilion.subdivisions.config.SuperconductorRelayConfig;
import com.prosilion.subdivisions.util.Factory;
import com.prosilion.subdivisions.util.TestSubscriber;
import java.io.IOException;
import java.util.List;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(SuperconductorRelayConfig.class)
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
class NostrRelayReactiveClientTest {
  public static final String ANSI_YELLOW = "\033[1;93m";
  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_35 = "\033[1;35m";
  private final ReactiveNostrRelayClient reactiveNostrRelayClient;
  private final String relayUri;

  public NostrRelayReactiveClientTest(@Value("${superconductor.relay.uri}") String relayUri) {
    this.reactiveNostrRelayClient = new ReactiveNostrRelayClient(relayUri);
    this.relayUri = relayUri;
  }

  @Test
  void testEventCreationUsingExplicitSubscriber() throws IOException, InterruptedException {
    log.debug("AAAAAAAAAAAAAAAAAAAAAAA");
    log.debug("AAAAAAAAAAAAAAAAAAAAAAA");

    Identity identity = Factory.createNewIdentity();
    GenericEvent event = new NIP01<>(identity).createTextNoteEvent(Factory.lorumIpsum()).sign().getEvent();
    log.trace("setup() send event:\n  {}", event.toString());

    Flux<String> flux = reactiveNostrRelayClient.sendEvent(new EventMessage(event, event.getId()));

    printConsole(flux.hashCode());

//  1) ******* if only below TestSubscriber is active, OnNext will register 1 EVENT
    TestSubscriber<String> eventSubscriber = new TestSubscriber<>();
    flux.subscribe(eventSubscriber);
//    eventSubscriber.dispose();

    List<String> items = eventSubscriber.getItems();
    String expected = "[\"OK\",\"" + event.getId() + "\",true,\"success: request processed\"]";
    assertEquals(expected, items.getFirst());

//  2) ******* if only below collectList() is active, OnNext will register 0 EVENTS
//     note: including below with above subscriber causes sending of TWO EVENTs
//    List<String> list3 = new ArrayList<>();
//    flux.collectList().subscribe(list3::addAll);
//  3) ******* if both above TestSubscriber and collectList are active, OnNext will register 2 EVENTS w/ same ID

    log.debug("AAAAAAAAAAAAAAAAAAAAAAA");
    log.debug("AAAAAAAAAAAAAAAAAAAAAAA");
  }

  @Test
  void testEventCreationUsingBlockFirst() throws IOException, InterruptedException {
    log.debug("\nBBBBBBBBBBBBBBBBBBBBBBB");
    log.debug("BBBBBBBBBBBBBBBBBBBBBBB");
    Identity identity = Factory.createNewIdentity();
    GenericEvent event = new NIP01<>(identity).createTextNoteEvent(Factory.lorumIpsum()).sign().getEvent();
    log.trace("setup() send event:\n  {}", event.toString());

    Flux<String> flux = reactiveNostrRelayClient.sendEvent(new EventMessage(event, event.getId()));
    printConsole(flux.hashCode());

//    example of blockFirst directly on a flux
    String returnedEventReq = flux.blockFirst();

    String expected = "[\"OK\",\"" + event.getId() + "\",true,\"success: request processed\"]";
    assertEquals(expected, returnedEventReq);

    log.debug("BBBBBBBBBBBBBBBBBBBBBBB");
    log.debug("BBBBBBBBBBBBBBBBBBBBBBB\n");
  }

  @Test
  void testReqFilteredByEventAndAuthorViaUsingBlockFirst() throws IOException {
    log.debug("\nDDDDDDDDDDDDDDDDDDDDDDD");
    log.debug("DDDDDDDDDDDDDDDDDDDDDDD");
    Identity identity = Factory.createNewIdentity();
    String content = Factory.lorumIpsum();
    GenericEvent event = new NIP01<>(identity).createTextNoteEvent(content).sign().getEvent();

    ReactiveNostrRelayClient methodReactiveNostrRelayClient = new ReactiveNostrRelayClient(relayUri);
    Flux<String> eventFlux = methodReactiveNostrRelayClient.sendEvent(new EventMessage(event));

    String eventResponse = eventFlux.blockFirst();
    log.debug("genericEvent: " + eventResponse);
    assertEquals("[\"OK\",\"" + event.getId() + "\",true,\"success: request processed\"]", eventResponse);
    log.debug("-------------------------");

//  #--------------------- REQ -------------------------
    EventFilter<GenericEvent> eventFilter = new EventFilter<>(event);
    AuthorFilter<PublicKey> authorFilter = new AuthorFilter<>(identity.getPublicKey());

    final String subscriberId = Factory.generateRandomHex64String();

    ReqMessage reqMessage = new ReqMessage(subscriberId, new Filters(eventFilter, authorFilter));
    Flux<GenericEvent> returnedEventsToMethodSubscriberIdFlux = methodReactiveNostrRelayClient.sendRequestReturnEvents(reqMessage);

    GenericEvent returnedReqGenericEvent = returnedEventsToMethodSubscriberIdFlux.blockFirst();

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

  @Test
  void testReqFilteredByEventAndAuthorUsingSubscriber() throws IOException {
    log.debug("\nEEEEEEEEEEEEEEEEEEEEEEEE");
    log.debug("EEEEEEEEEEEEEEEEEEEEEEEE");

    Identity identity = Factory.createNewIdentity();
    String content = Factory.lorumIpsum();
    GenericEvent event = new NIP01<>(identity).createTextNoteEvent(content).sign().getEvent();

    ReactiveNostrRelayClient methodReactiveNostrRelayClient = new ReactiveNostrRelayClient(relayUri);
    Flux<String> eventFlux = methodReactiveNostrRelayClient.sendEvent(new EventMessage(event));//, event.getId()));

    TestSubscriber<String> eventSubscriber = new TestSubscriber<>();
    eventFlux.subscribe(eventSubscriber);  //  subscriber, causing EVENT emission

    List<String> eventRespose = eventSubscriber.getItems();
    String expected = "[\"OK\",\"" + event.getId() + "\",true,\"success: request processed\"]";
    assertEquals(expected, eventRespose.getFirst());

//  #--------------------- REQ -------------------------
    EventFilter<GenericEvent> eventFilter = new EventFilter<>(event);
    AuthorFilter<PublicKey> authorFilter = new AuthorFilter<>(identity.getPublicKey());

    final String subscriberId = Factory.generateRandomHex64String();

    ReqMessage reqMessage = new ReqMessage(subscriberId, new Filters(eventFilter, authorFilter));
    Flux<GenericEvent> returnedEventsToMethodSubscriberIdFlux = methodReactiveNostrRelayClient.sendRequestReturnEvents(reqMessage);

    TestSubscriber<GenericEvent> reqSubscriber = new TestSubscriber<>();
    returnedEventsToMethodSubscriberIdFlux.subscribe(reqSubscriber);  //  subscriber, causing REQ emission

    GenericEvent returnedReqGenericEvent = reqSubscriber.getItems().getFirst();
    String encode = new EventMessage(returnedReqGenericEvent).encode();
    log.debug(encode);
    log.debug("+++++++++++++++++++++++++");

    assertEquals(returnedReqGenericEvent.getId(), event.getId());
    assertEquals(returnedReqGenericEvent.getContent(), event.getContent());
    assertEquals(returnedReqGenericEvent.getPubKey().toHexString(), event.getPubKey().toHexString());
    log.debug("EEEEEEEEEEEEEEEEEEEEEEEE");
    log.debug("EEEEEEEEEEEEEEEEEEEEEEEE\n");
  }

  @Test
  void testReqFilteredByEventAndAuthorViaReqMessageUsingGlobalClient() throws IOException {
    log.debug("\nFFFFFFFFFFFFFFFFFFFFFFF");
    log.debug("FFFFFFFFFFFFFFFFFFFFFFFF");

    Identity identity = Factory.createNewIdentity();
    String content = Factory.lorumIpsum();
    GenericEvent event = new NIP01<>(identity).createTextNoteEvent(content).sign().getEvent();

    Flux<String> eventFlux = reactiveNostrRelayClient.sendEvent(new EventMessage(event));//, event.getId()));
    String eventResponse = eventFlux.blockFirst();
    log.debug("genericEvent: " + eventResponse);
    assertEquals("[\"OK\",\"" + event.getId() + "\",true,\"success: request processed\"]", eventResponse);
    log.debug("-------------------------");

//    #--------------------- REQ -------------------------
    EventFilter<GenericEvent> eventFilter = new EventFilter<>(event);
    AuthorFilter<PublicKey> authorFilter = new AuthorFilter<>(identity.getPublicKey());

    final String subscriberId = Factory.generateRandomHex64String();

    ReqMessage reqMessage = new ReqMessage(subscriberId, new Filters(eventFilter, authorFilter));
    Flux<GenericEvent> returnedEventsToMethodSubscriberIdFlux = reactiveNostrRelayClient.sendRequestReturnEvents(reqMessage);

    GenericEvent returnedReqGenericEvent = returnedEventsToMethodSubscriberIdFlux.blockFirst();

    log.debug("+++++++++++++++++++++++++");
    assertNotNull(returnedReqGenericEvent);
    String encode = new EventMessage(returnedReqGenericEvent).encode();
    log.debug(encode);
    log.debug("+++++++++++++++++++++++++");

    assertEquals(returnedReqGenericEvent.getId(), event.getId());
    assertEquals(returnedReqGenericEvent.getContent(), event.getContent());
    assertEquals(returnedReqGenericEvent.getPubKey().toHexString(), event.getPubKey().toHexString());
    log.debug("FFFFFFFFFFFFFFFFFFFFFFF");
    log.debug("FFFFFFFFFFFFFFFFFFFFFFFF\n");
  }

  /*
   * notes:
   *  below method kept as notes for what doesn't work
   *  subscribe() on infinite flux won't return anything
   *
   *  collectList() neither used nor registers subscription event, however
   *    its log.debug("BBBBBBBBBBBB " + s + "BBBBBBBBBBBB") doesn't print either, needs investigate
   */
//  @Test
  void testEventCreationUsingCollectList() {
//    log.debug("\nCCCCCCCCCCCCCCCCCCCCCCC");
//    log.debug("CCCCCCCCCCCCCCCCCCCCCCC");

//    Identity identity = Factory.createNewIdentity();
//    GenericEvent event = new NIP01<>(identity).createTextNoteEvent(Factory.lorumIpsum()).sign().getEvent();
//    log.trace("setup() send event:\n  {}", event.toString());

//    Flux<String> flux = reactiveNostrRelayClient.sendEvent(new EventMessage(event, event.getId()));

//    List<String> listNoBlock = flux.toStream().toList();
//    log.debug("returned events:");
//    listNoBlock.forEach(s -> log.debug("  " + s));

// below kept as notes for what doesn't work    
//    List<String> listNoBlock = new ArrayList<>();
//    flux.collectList().subscribe(listNoBlock::addAll);  // <------  subscribe() on infinite flux won't return anything

//    List<String> listBlock = flux.collectList().block();  // <------  subscribe() on infinite flux won't return anything
//    listBlock.forEach(s -> log.debug("BBBBBBBBBBBB " + s + "BBBBBBBBBBBB"));

//    List<String> durationBlock = flux.collectList().block(Duration.ofMillis(100)); // hangs
//    List<String> durationBlock = flux.toStream().toList(); // voids reactive and hangs 

//    log.debug("CCCCCCCCCCCCCCCCCCCCCCC");
//    log.debug("CCCCCCCCCCCCCCCCCCCCCCC\n");
  }

  final void printConsole(int i) {
    log.debug(" flux hashcode: [ " + ANSI_YELLOW + i + ANSI_RESET + " ]");
  }

  static void parseJson(String value) {
    String subscriberId = value.split(",")[1];
    String strippedStart = StringUtils.stripStart(subscriberId, "\"");
    log.debug(" " + ANSI_35 + StringUtils.stripEnd(strippedStart, "\"") + ANSI_RESET + " " + value.hashCode());
  }
}
