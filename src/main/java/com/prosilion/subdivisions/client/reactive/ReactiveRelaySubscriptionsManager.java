package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.ReqMessage;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import reactor.core.publisher.Flux;

@Slf4j
public class ReactiveRelaySubscriptionsManager implements MessageTypeFilterable {
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

  public <T extends ReqMessage, V extends BaseMessage> void send(@NonNull T reqMessage, @NonNull Subscriber<V> subscriber) throws JsonProcessingException {
    log.debug("pre-encoded ReqMessage json: \n{}", reqMessage);
    Flux<V> apply = baseMessagesReturnedByReqMessage(getRequestResults(reqMessage));
    apply.subscribe(subscriber);
  }

  private <T extends ReqMessage> Flux<String> getRequestResults(T reqMessage) throws JsonProcessingException {
    String subscriberId = reqMessage.getSubscriptionId();
    final ReactiveWebSocketClient reactiveWebSocketClient = Optional.ofNullable(subscriberIdWebSocketClientMap.get(subscriberId))
        .orElseGet(() -> {
          subscriberIdWebSocketClientMap.put(subscriberId, getReactiveWebSocketClient());
          return subscriberIdWebSocketClientMap.get(subscriberId);
        });
    return reactiveWebSocketClient.send(reqMessage);
  }

  private <V extends BaseMessage> Flux<V> baseMessagesReturnedByReqMessage(@NonNull Flux<String> reqMessage) {
    return this.getTypeSpecificMessage(reqMessage);
  }

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
