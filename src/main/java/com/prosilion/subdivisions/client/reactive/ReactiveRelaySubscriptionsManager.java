package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.message.ReqMessage;
import org.reactivestreams.Subscriber;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import reactor.core.publisher.Flux;

@Slf4j
public class ReactiveRelaySubscriptionsManager<T extends GenericEvent> implements MessageTypeFilterable {
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

  public void send(@NonNull ReqMessage reqMessage, @NonNull Subscriber<T> subscriber) throws JsonProcessingException {
    log.debug("pre-encoded ReqMessage json: \n{}", reqMessage);
    Flux<T> apply = eventsAsGenericEvents.apply(getRequestResults(reqMessage));
    apply.subscribe(subscriber);
  }

  private final Function<Flux<String>, Flux<T>> eventsAsGenericEvents = (events) ->
      getTypeSpecificMessage(EventMessage.class, events)
          .map(eventMessage -> (T) eventMessage.getEvent());

  private Flux<String> getRequestResults(ReqMessage reqMessage) throws JsonProcessingException {
    String subscriberId = reqMessage.getSubscriptionId();
    final ReactiveWebSocketClient reactiveWebSocketClient = Optional.ofNullable(subscriberIdWebSocketClientMap.get(subscriberId))
        .orElseGet(() -> {
          subscriberIdWebSocketClientMap.put(subscriberId, getReactiveWebSocketClient());
          return subscriberIdWebSocketClientMap.get(subscriberId);
        });
    return reactiveWebSocketClient.send(reqMessage);
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

  //  TODO: cleanup sneaky
  @SneakyThrows
  private ReactiveWebSocketClient getReactiveWebSocketClient() {
    return Objects.isNull(sslBundles) ?
        new ReactiveWebSocketClient(relayUri) :
        new ReactiveWebSocketClient(relayUri, sslBundles);
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
}
