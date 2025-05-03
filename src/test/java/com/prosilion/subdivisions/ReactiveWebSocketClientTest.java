//package com.prosilion.subdivisions;
//
//import com.prosilion.subdivisions.client.reactive.ReactiveNostrRelayClient;
//import com.prosilion.subdivisions.util.Factory;
//import java.io.IOException;
//import lombok.extern.slf4j.Slf4j;
//import nostr.api.NIP01;
//import nostr.event.impl.GenericEvent;
//import nostr.event.message.EventMessage;
//import nostr.id.Identity;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import reactor.core.publisher.Flux;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@Slf4j
//@ExtendWith(SpringExtension.class)
//@TestPropertySource("classpath:application-test.properties")
//@ActiveProfiles("test")
//class ReactiveWebSocketClientTest {
//  private final ReactiveNostrRelayClient reactiveNostrRelayClient;
//  private final static Identity identity = Factory.createNewIdentity();
//  //  private final static String eventId = Factory.generateRandomHex64String();
////  private final static String globalSubscriberId = Factory.generateRandomHex64String();
////  private Subscription subscription;
//  private final String content;
//
////  private String relayResponse = null;
//
//  public ReactiveWebSocketClientTest(@Value("${superconductor.relay.uri}") String relayUri) throws IOException {
//    reactiveNostrRelayClient = new ReactiveNostrRelayClient(relayUri);
//    content = Factory.lorumIpsum(getClass());
//
//    GenericEvent event = new NIP01<>(identity).createTextNoteEvent(content).sign().getEvent();
//    log.debug("setup() send event:\n  {}", event.toString());
//
//    Flux<String> stringFlux = reactiveNostrRelayClient.sendEvent(new EventMessage(event, event.getId()));
//    assertNotNull(stringFlux.collectList().block());
////    await().until(() -> Objects.nonNull(relayResponse));
////    reactiveNostrRelayClient.closeSocket();
//  }
//
//  @Test
//  void testasdfas() {
//    log.debug("test reached");
//  }
//
////  @Test
////  void testReqFilteredByEventAndAuthorViaReqMessage() throws JsonProcessingException {
////    final String subscriberId = Factory.generateRandomHex64String();
////
////    EventFilter<GenericEvent> eventFilter = new EventFilter<>(new GenericEvent(eventId));
////    AuthorFilter<PublicKey> authorFilter = new AuthorFilter<>(new PublicKey(identity.getPublicKey().toHexString()));
////
////    ReqMessage reqMessage = new ReqMessage(subscriberId, new Filters(eventFilter, authorFilter));
////    Flux<GenericEvent> returnedEvents = reactiveNostrRelayClient.sendRequestReturnEvents(reqMessage);
////
////    log.debug("okMessage to UniqueSubscriberId:");
////    log.debug("  " + returnedEvents);
////    assertTrue(returnedEvents.toStream().anyMatch(event -> event.getId().equals(eventId)));
////    assertTrue(returnedEvents.toStream().anyMatch(event -> event.getContent().equals(content)));
////    assertTrue(returnedEvents.toStream().anyMatch(event -> event.getPubKey().toHexString().equals(identity.getPublicKey().toHexString())));
////
////    ReqMessage reqMessage2 = new ReqMessage(globalSubscriberId, new Filters(eventFilter, authorFilter));
////    Flux<GenericEvent> returnedEvents2 = reactiveNostrRelayClient.sendRequestReturnEvents(reqMessage2);
////
////    log.debug("okMessage:");
////    log.debug("  " + returnedEvents2);
////    assertTrue(returnedEvents2.toStream().anyMatch(event -> event.getId().equals(eventId)));
////    assertTrue(returnedEvents2.toStream().anyMatch(event -> event.getContent().equals(content)));
////    assertTrue(returnedEvents2.toStream().anyMatch(event -> event.getPubKey().toHexString().equals(identity.getPublicKey().toHexString())));
////  }
//}
