package com.prosilion.subdivisions.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.subdivisions.WebSocketClient;
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
import nostr.event.BaseMessage;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import org.apache.commons.lang3.stream.Streams;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;

@Slf4j
public class RelaySubscriptionsManager {
  private final Map<String, WebSocketClient> subscriberIdWebSocketClientMap = new ConcurrentHashMap<>();
  private final String relayUri;
  private SslBundles sslBundles;

  public RelaySubscriptionsManager(@NonNull String relayUri) {
    this.relayUri = relayUri;
    log.debug("relayUri: \n{}", relayUri);
  }

  public RelaySubscriptionsManager(@NonNull String relayUri, SslBundles sslBundles) {
    this.relayUri = relayUri;
    this.sslBundles = sslBundles;
    log.debug("sslBundles: \n{}", sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
  }

  public List<String> sendRequestReturnEvents(@NonNull ReqMessage reqMessage) throws JsonProcessingException {
    return sendRequestReturnEvents(reqMessage.getSubscriptionId(), reqMessage.encode());
  }

  public List<String> sendRequestReturnEvents(@NonNull String subscriberId, @NonNull String reqJson) {
    return allEvents.apply(getRequestResults(subscriberId, reqJson));
  }

  public Map<Command, List<String>> sendRequestReturnCommandResultsMap(@NonNull ReqMessage reqMessage) throws JsonProcessingException {
    return sendRequestReturnCommandResultsMap(reqMessage.getSubscriptionId(), reqMessage.encode());
  }

  public Map<Command, List<String>> sendRequestReturnCommandResultsMap(@NonNull String subscriberId, @NonNull String reqJson) {
    List<String> returnedEvents = getRequestResults(subscriberId, reqJson);

    log.debug("55555555555555555");
    log.debug("after REQUEST:");
    log.debug("key/subscriberId:\n  [{}]\n", subscriberId);
    log.debug("-----------------");
    log.debug("returnedEvents:");
    log.debug(returnedEvents.stream().map(event -> String.format("  %s\n", event)).collect(Collectors.joining()));
    log.debug("55555555555555555");

    Map<Command, List<String>> results = new HashMap<>();
    eose.apply(returnedEvents).ifPresent(eoses -> results.put(Command.EOSE, List.of(eoses)));
    results.put(Command.EVENT, allEvents.apply(returnedEvents));

    return results;
  }

  private List<String> getRequestResults(String subscriberId, String reqJson) {
    return Optional.ofNullable(subscriberIdWebSocketClientMap.get(subscriberId))
        .orElseGet(() -> {
          subscriberIdWebSocketClientMap.put(subscriberId, getStandardWebSocketClient());
          subscriberIdWebSocketClientMap.get(subscriberId).send(reqJson);
          return subscriberIdWebSocketClientMap.get(subscriberId);
        }).getEvents();
  }

  //  TODO: cleanup sneaky
  @SneakyThrows
  private WebSocketClient getStandardWebSocketClient() {
    return Objects.nonNull(sslBundles) ? new WebSocketClient(relayUri, sslBundles) : new WebSocketClient(relayUri);
  }

  private final Function<List<String>, Optional<String>> eose = (events) ->
      getTypeSpecificMessage(EoseMessage.class, events).stream().map(EoseMessage::getSubscriptionId).findFirst();

  private final Function<List<String>, Optional<String>> newestEvent = (events) ->
      getTypeSpecificMessage(EventMessage.class, events).stream()
          .map(eventMessage -> (GenericEvent) eventMessage.getEvent())
          .sorted(Comparator.comparing(GenericEvent::getCreatedAt))
          .map(event -> new BaseEventEncoder<>(event).encode())
          .reduce((first, second) -> second);

  private final Function<List<String>, List<String>> allEvents = (events) ->
      getTypeSpecificMessage(EventMessage.class, events).stream()
          .map(eventMessage -> (GenericEvent) eventMessage.getEvent())
          .sorted(Comparator.comparing(GenericEvent::getCreatedAt))
          .map(event -> new BaseEventEncoder<>(event).encode())
          .toList();

  <V extends BaseMessage> List<V> getTypeSpecificMessage(Class<V> messageClass, List<String> messages) {
    return Streams.failableStream(messages.stream()
        .map(msg -> {
          try {
            return new BaseMessageDecoder<V>().decode(msg);
          } catch (JsonProcessingException e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .filter(messageClass::isInstance)).stream().toList();
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

  private void closeSessions(Map<String, WebSocketClient> subscriberIdWebSocketClientMap) {
    closeSessions(subscriberIdWebSocketClientMap.values());
  }

  private void closeSessions(WebSocketClient... webSocketClients) {
    closeSessions(List.of(webSocketClients));
  }

  private void closeSessions(Collection<WebSocketClient> webSocketClients) {
    Streams.failableStream(webSocketClients.stream()).forEach(WebSocketClient::closeSession);
  }
}
