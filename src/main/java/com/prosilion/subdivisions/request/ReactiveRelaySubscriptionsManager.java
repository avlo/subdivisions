package com.prosilion.subdivisions.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.subdivisions.client.reactive.ReactiveWebSocketClient;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import reactor.core.publisher.Flux;

@Slf4j
public class ReactiveRelaySubscriptionsManager {
  private final Map<String, ReactiveWebSocketClient> subscriberIdWebSocketClientMap = new ConcurrentHashMap<>();
  private final String relayUri;
  private SslBundles sslBundles;

  public ReactiveRelaySubscriptionsManager(@NonNull String relayUri) {
    this.relayUri = relayUri;
    log.debug("relayUri: \n{}", relayUri);
  }

  public ReactiveRelaySubscriptionsManager(@NonNull String relayUri, @NonNull SslBundles sslBundles) {
    this.relayUri = relayUri;
    this.sslBundles = sslBundles;
    log.debug("sslBundles: \n{}", sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
  }

  //  TODO: need flux variant of below  
  public Flux<GenericEvent> send(@NonNull ReqMessage reqMessage) throws JsonProcessingException {
    log.debug("pre-encoded ReqMessage json: \n{}", reqMessage);
    return eventsAsGenericEvents.apply(getRequestResults(reqMessage));
  }

//  public Map<Command, List<Object>> sendRequestReturnCommandResultsMap(@NonNull ReqMessage reqMessage) throws JsonProcessingException {
//    return sendRequestReturnCommandResultsMap(
//        reqMessage.getSubscriptionId(),
//        reqMessage.encode());
//  }

  //  TODO: need flux variant of below
//  public Map<Command, List<Object>> sendRequestReturnCommandResultsMap(@NonNull String subscriberId, @NonNull String reqJson) {
//    List<String> returnedEvents = getRequestResults(subscriberId, reqJson);
//
//    log.debug("55555555555555555");
//    log.debug("after REQUEST:");
//    log.debug("key/subscriberId:\n  [{}]\n", subscriberId);
//    log.debug("-----------------");
//    log.debug("returnedEvents:");
//    log.debug(returnedEvents.stream().map(event -> String.format("  %s\n", event)).collect(Collectors.joining()));
//    log.debug("55555555555555555");
//
//    Map<Command, List<Object>> results = new HashMap<>();
//    eose.apply(returnedEvents).ifPresent(eoses -> results.put(Command.EOSE, List.of(eoses)));
//    results.put(Command.EVENT, eventsAsStrings.apply(returnedEvents));
//
//    return results;
//  }

  private Flux<String> getRequestResults(ReqMessage reqMessage) throws JsonProcessingException {
    String subscriberId = reqMessage.getSubscriptionId();
    log.debug("subscriberId: [{}]", subscriberId);
    String reqJson = reqMessage.encode();
    log.debug("reqJson: \n{}", reqJson);
    return Optional.ofNullable(subscriberIdWebSocketClientMap.get(subscriberId))
        .orElseGet(() -> {
          subscriberIdWebSocketClientMap.put(subscriberId, getStandardWebSocketClient());
          return subscriberIdWebSocketClientMap.get(subscriberId);
        }).send(reqMessage);
  }

  //  TODO: cleanup sneaky
  @SneakyThrows
  private ReactiveWebSocketClient getStandardWebSocketClient() {
//    return Objects.nonNull(sslBundles) ? new ReactiveWebSocketClient(relayUri) : new ReactiveWebSocketClient(relayUri);
    return new ReactiveWebSocketClient(relayUri);
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

  private void closeSessions(Map<String, ReactiveWebSocketClient> subscriberIdWebSocketClientMap) {
    closeSessions(subscriberIdWebSocketClientMap.values());
  }

  private void closeSessions(ReactiveWebSocketClient... reactiveWebSocketClients) {
    closeSessions(List.of(reactiveWebSocketClients));
  }

  private void closeSessions(Collection<ReactiveWebSocketClient> reactiveWebSocketClients) {
    reactiveWebSocketClients.forEach(ReactiveWebSocketClient::closeSocket);
  }

  private final Function<Flux<String>, Flux<String>> eventsAsStrings = (events) ->
      getTypeSpecificMessage(EventMessage.class, events)
          .map(eventMessage -> (GenericEvent) eventMessage.getEvent())
          .map(event -> new BaseEventEncoder<>(event).encode());

  private final Function<Flux<String>, Flux<GenericEvent>> eventsAsGenericEvents = (events) ->
      getTypeSpecificMessage(EventMessage.class, events)
          .map(eventMessage -> (GenericEvent) eventMessage.getEvent());

//  private final Function<Flux<String>, Optional<String>> eose = (events) ->
//      getTypeSpecificMessage(EoseMessage.class, events).map(EoseMessage::getSubscriptionId);

  private <V extends BaseMessage> Flux<V> getTypeSpecificMessage(Class<V> messageClass, Flux<String> messages) {
    return messages.map(msg -> {
          try {
            return new BaseMessageDecoder<V>().decode(msg);
          } catch (JsonProcessingException e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .filter(messageClass::isInstance);
  }
}
