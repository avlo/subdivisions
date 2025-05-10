package com.prosilion.subdivisions.client.standard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.subdivisions.client.reactive.MessageTypeFilterable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nostr.base.Command;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import org.apache.commons.lang3.stream.Streams;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;

@Slf4j
public class StandardRelaySubscriptionsManager implements MessageTypeFilterable {
  private final Map<String, StandardWebSocketClient> subscriberIdWebSocketClientMap = new ConcurrentHashMap<>();
  private final String relayUri;
  private SslBundles sslBundles;

  public StandardRelaySubscriptionsManager(@NonNull String relayUri) {
    this.relayUri = relayUri;
    log.debug("relayUri: \n{}", relayUri);
  }

  public StandardRelaySubscriptionsManager(@NonNull String relayUri, @NonNull SslBundles sslBundles) {
    this.relayUri = relayUri;
    this.sslBundles = sslBundles;
    log.debug("sslBundles: \n{}", sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
  }

  private final Function<List<String>, Optional<String>> newestEvent = (events) ->
      getTypeSpecificMessage(EventMessage.class, events).stream()
          .map(eventMessage -> (GenericEvent) eventMessage.getEvent())
          .sorted(Comparator.comparing(GenericEvent::getCreatedAt))
          .map(event -> new BaseEventEncoder<>(event).encode())
          .reduce((first, second) -> second);

  private final Function<List<String>, List<Object>> eventsAsStrings = (events) ->
      getTypeSpecificMessage(EventMessage.class, events).stream()
          .map(eventMessage -> (GenericEvent) eventMessage.getEvent())
          .sorted(Comparator.comparing(GenericEvent::getCreatedAt))
          .map(event -> new BaseEventEncoder<>(event).encode())
          .map(Object.class::cast)
          .toList();

  private final Function<List<String>, List<GenericEvent>> eventsAsGenericEvents = (events) ->
      getTypeSpecificMessage(EventMessage.class, events).stream()
          .map(eventMessage -> (GenericEvent) eventMessage.getEvent())
          .sorted(Comparator.comparing(GenericEvent::getCreatedAt))
          .toList();

  private final Function<List<String>, Optional<String>> eose = (events) ->
      getTypeSpecificMessage(EoseMessage.class, events).stream().map(EoseMessage::getSubscriptionId).findFirst();

  public List<GenericEvent> sendRequestReturnEvents(@NonNull ReqMessage reqMessage) throws JsonProcessingException {
    log.debug("pre-encoded ReqMessage json: \n{}", reqMessage);
    return eventsAsGenericEvents.apply(
        getRequestResults(
            reqMessage.getSubscriptionId(),
            reqMessage.encode()));
  }

  public Map<Command, List<Object>> sendRequestReturnCommandResultsMap(@NonNull ReqMessage reqMessage) throws JsonProcessingException {
    return sendRequestReturnCommandResultsMap(
        reqMessage.getSubscriptionId(),
        reqMessage.encode());
  }

  public Map<Command, List<Object>> sendRequestReturnCommandResultsMap(@NonNull String subscriberId, @NonNull String reqJson) {
    List<String> returnedEvents = getRequestResults(subscriberId, reqJson);

    log.debug("55555555555555555");
    log.debug("after REQUEST:");
    log.debug("key/subscriberId:\n  [{}]\n", subscriberId);
    log.debug("-----------------");
    log.debug("returnedEvents:");
    log.debug(returnedEvents.stream().map(event -> String.format("  %s\n", event)).collect(Collectors.joining()));
    log.debug("55555555555555555");

    Map<Command, List<Object>> results = new HashMap<>();
    eose.apply(returnedEvents).ifPresent(eoses -> results.put(Command.EOSE, List.of(eoses)));
    results.put(Command.EVENT, eventsAsStrings.apply(returnedEvents));

    return results;
  }

  private List<String> getRequestResults(String subscriberId, String reqJson) {
    log.debug("subscriberId: [{}]", subscriberId);
    log.debug("reqJson: \n{}", reqJson);
    return Optional.ofNullable(subscriberIdWebSocketClientMap.get(subscriberId))
        .orElseGet(() -> {
          subscriberIdWebSocketClientMap.put(subscriberId, getStandardWebSocketClient());
          subscriberIdWebSocketClientMap.get(subscriberId).send(reqJson);
          return subscriberIdWebSocketClientMap.get(subscriberId);
        }).getEvents();
  }

  public List<GenericEvent> updateReqResults(@NonNull String subscriberId) {
    log.debug("RelaySubscriptionsManagerg updateReqResults for subscriberId: [{}]", subscriberId);
    return eventsAsGenericEvents.apply(getEvents(subscriberId));
  }

  private List<String> getEvents(@NonNull String subscriberId) {
    return Optional.ofNullable(subscriberIdWebSocketClientMap.get(subscriberId))
        .orElseThrow().getEvents();
  }

  //  TODO: cleanup sneaky
  @SneakyThrows
  private StandardWebSocketClient getStandardWebSocketClient() {
    return Objects.nonNull(sslBundles) ? new StandardWebSocketClient(relayUri, sslBundles) : new StandardWebSocketClient(relayUri);
  }

  public void closeSession(@NonNull String... subscriberIds) {
    closeSessions(List.of(subscriberIds));
  }

  public void closeSessions(@NonNull List<String> subscriberIds) {
    subscriberIds.forEach(id -> closeSessions(subscriberIdWebSocketClientMap.get(id)));
    subscriberIds.forEach(subscriberIdWebSocketClientMap::remove);
  }

  public void closeAllSessions() {
    closeSessions(subscriberIdWebSocketClientMap);
    subscriberIdWebSocketClientMap.clear();
  }

  private void closeSessions(Map<String, StandardWebSocketClient> subscriberIdWebSocketClientMap) {
    closeSessions(subscriberIdWebSocketClientMap.values());
  }

  private void closeSessions(StandardWebSocketClient... standardWebSocketClients) {
    closeSessions(List.of(standardWebSocketClients));
  }

  private void closeSessions(Collection<StandardWebSocketClient> standardWebSocketClients) {
    Streams.failableStream(standardWebSocketClients.stream()).forEach(StandardWebSocketClient::closeSession);
  }
}
