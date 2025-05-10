package com.prosilion.subdivisions.client.standard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.subdivisions.config.SuperconductorRelayConfig;
import com.prosilion.subdivisions.client.standard.StandardNostrRelayClient;
import com.prosilion.subdivisions.util.Factory;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import nostr.base.PublicKey;
import nostr.event.filter.AuthorFilter;
import nostr.event.filter.EventFilter;
import nostr.event.filter.Filters;
import nostr.event.impl.GenericEvent;
import nostr.event.message.ReqMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringJUnitConfig(SuperconductorRelayConfig.class)
@TestPropertySource("classpath:application-test.properties")
@ActiveProfiles("test")
public class NostrRelayStandardClientTest {
  private final StandardNostrRelayClient standardNostrRelayClient;

  private final static String authorPubKey = Factory.generateRandomHex64String();
  private final static String eventId = Factory.generateRandomHex64String();
  private final static String globalSubscriberId = Factory.generateRandomHex64String(); // global subscriber UUID
  private final String content;

  @Autowired
  public NostrRelayStandardClientTest(@Value("${superconductor.relay.uri}") String relayUri) throws IOException, ExecutionException, InterruptedException {
    this.standardNostrRelayClient = new StandardNostrRelayClient(relayUri);
    content = Factory.lorumIpsum(getClass());

    String globalEventJson = "[\"EVENT\",{\"id\":\"" + eventId + "\",\"kind\":1,\"content\":\"" + content + "\",\"pubkey\":\"" + authorPubKey + "\",\"created_at\":1717357053050,\"tags\":[],\"sig\":\"86f25c161fec51b9e441bdb2c09095d5f8b92fdce66cb80d9ef09fad6ce53eaa14c5e16787c42f5404905536e43ebec0e463aee819378a4acbe412c533e60546\"}]";
    log.debug("setup() send event:\n  {}", globalEventJson);

    assertTrue(this.standardNostrRelayClient.sendEvent(globalEventJson).getFlag());
  }

  @Test
  void testReqFilteredByEventAndAuthorViaReqMessage() throws JsonProcessingException {
    final String subscriberId = Factory.generateRandomHex64String();

    EventFilter<GenericEvent> eventFilter = new EventFilter<>(new GenericEvent(eventId));
    AuthorFilter<PublicKey> authorFilter = new AuthorFilter<>(new PublicKey(authorPubKey));

    ReqMessage reqMessage = new ReqMessage(subscriberId, new Filters(eventFilter, authorFilter));
    List<GenericEvent> returnedEvents = standardNostrRelayClient.sendRequestReturnEvents(reqMessage);

    log.debug("okMessage to UniqueSubscriberId:");
    log.debug("  " + returnedEvents);
    assertTrue(returnedEvents.stream().anyMatch(event -> event.getId().equals(eventId)));
    assertTrue(returnedEvents.stream().anyMatch(event -> event.getContent().equals(content)));
    assertTrue(returnedEvents.stream().anyMatch(event -> event.getPubKey().toHexString().equals(authorPubKey)));

    ReqMessage reqMessage2 = new ReqMessage(globalSubscriberId, new Filters(eventFilter, authorFilter));
    List<GenericEvent> returnedEvents2 = standardNostrRelayClient.sendRequestReturnEvents(reqMessage2);

    log.debug("okMessage:");
    log.debug("  " + returnedEvents2);
    assertTrue(returnedEvents2.stream().anyMatch(event -> event.getId().equals(eventId)));
    assertTrue(returnedEvents2.stream().anyMatch(event -> event.getContent().equals(content)));
    assertTrue(returnedEvents2.stream().anyMatch(event -> event.getPubKey().toHexString().equals(authorPubKey)));
  }
}
