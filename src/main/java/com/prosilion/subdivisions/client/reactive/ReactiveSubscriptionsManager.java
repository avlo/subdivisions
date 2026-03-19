package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.NostrException;
import com.prosilion.nostr.codec.BaseMessageDecoder;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.ReqMessage;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import reactor.core.publisher.Flux;

@Slf4j
class ReactiveSubscriptionsManager {
  private final Map<String, ReactiveWebSocketClient> subscriberIdWebSocketClientMap = new ConcurrentHashMap<>();
  private final String relayUri;
  private SslBundles sslBundles;

  protected ReactiveSubscriptionsManager(@NonNull String relayUrl) {
    log.debug("{} constructor called with relay url: [{}]", getClass().getSimpleName(), relayUrl);
    this.relayUri = relayUrl;
  }

  protected ReactiveSubscriptionsManager(@NonNull String relayUrl, @NonNull SslBundles sslBundles) {
    log.debug("{} constructor called with relay url:  [{}], sslBundles [{}]", getClass().getSimpleName(), relayUrl, sslBundles);
    this.relayUri = relayUrl;
    this.sslBundles = sslBundles;
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: [{}]", server);
    log.debug("sslBundles key: [{}]", server.getKey());
    log.debug("sslBundles protocol: [{}]", server.getProtocol());
  }

  protected <T extends ReqMessage, V extends BaseMessage> void send(@NonNull T reqMessage, @NonNull Subscriber<V> subscriber) throws JsonProcessingException, NostrException {
    log.debug("{} send(reqMessage, subscriber) ReqMessage:\n{}", getClass().getSimpleName(), reqMessage.encode());
    Flux<V> apply = baseMessagesReturnedByReqMessage(getRequestResults(reqMessage));
    apply.subscribe(subscriber);
  }

  private <T extends ReqMessage> Flux<String> getRequestResults(T reqMessage) throws JsonProcessingException {
    String subscriberId = reqMessage.getSubscriptionId();
    subscriberIdWebSocketClientMap.putIfAbsent(subscriberId, getReactiveWebSocketClient());
    ReactiveWebSocketClient reactiveWebSocketClient = subscriberIdWebSocketClientMap.get(subscriberId);
    return reactiveWebSocketClient.send(reqMessage);
  }

  private <V extends BaseMessage> Flux<V> baseMessagesReturnedByReqMessage(@NonNull Flux<String> reqMessage) {
    return (Flux<V>) reqMessage
        .map(msg -> {
          try {
            return BaseMessageDecoder.decode(msg);
          } catch (JsonProcessingException e) {
            throw new NostrException(String.format("%s flux bad not good", getClass().getSimpleName()), e);
          }
        })
        .filter(Objects::nonNull);
  }

  private ReactiveWebSocketClient getReactiveWebSocketClient() {
    return Objects.isNull(sslBundles) ?
        new ReactiveWebSocketClient(relayUri) :
        new ReactiveWebSocketClient(relayUri, sslBundles);
  }

  protected void closeSession(@NonNull String... subscriberIds) {
    closeSessions(List.of(subscriberIds));
  }

  protected void closeSessions(@NonNull List<String> subscriberIds) {
    subscriberIds.forEach(id -> closeSessions(subscriberIdWebSocketClientMap.get(id)));
    subscriberIds.forEach(subscriberIdWebSocketClientMap::remove);
  }

  protected void closeAllSessions() {
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
