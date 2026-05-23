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
import java.util.function.Function;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

@Slf4j
public class SingleRelaySubscriptionsManager {
  private final Map<String, WebSocketClient> subscriberIdWebSocketClientMap = new ConcurrentHashMap<>();
  private final String relayUrl;
  private SslBundles sslBundles;

  SingleRelaySubscriptionsManager(@NonNull String relayUrl) {
    this.relayUrl = relayUrl;
  }

  SingleRelaySubscriptionsManager(@NonNull String relayUrl, @NonNull SslBundles sslBundles) {
    log.debug("{} constructor called with relay url:  [{}], sslBundles [{}]", getClass().getSimpleName(), relayUrl, sslBundles);
    this.relayUrl = relayUrl;
    this.sslBundles = sslBundles;
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: [{}]", server);
    log.debug("sslBundles key: [{}]", server.getKey());
    log.debug("sslBundles protocol: [{}]", server.getProtocol());
  }

  public void send(@NonNull ReqMessage reqMessage, @NonNull BaseSubscriber<BaseMessage> subscriber) {
    log.debug("... send(@NonNull ReqMessage reqMessage, @NonNull BaseSubscriber<BaseMessage> subscriber ... ");
    Flux<String> requestResults = getRequestResults(reqMessage);
    log.debug("... received Flux<String> requestResults = getRequestResults(reqMessage) ... ");
    Flux<BaseMessage> baseMessageFlux = baseMessagesReturnedByReqMessageFlatMap(requestResults);
    log.debug("... received Flux<BaseMessage> baseMessagesReturnedByReqMessage(requestResults) ... ");
    baseMessageFlux.subscribe(subscriber);
  }

  private Flux<String> getRequestResults(ReqMessage reqMessage) {
//    TODO: potentially throw exception if reqMessage.getSubscriptionId() already exists in map
    subscriberIdWebSocketClientMap.putIfAbsent(reqMessage.getSubscriptionId(), getReactiveWebSocketClient());
    return subscriberIdWebSocketClientMap.get(reqMessage.getSubscriptionId()).send(reqMessage);
  }

  private Flux<BaseMessage> baseMessagesReturnedByReqMessage(@NonNull Flux<String> reqMessageFlux) {
    log.debug("... (1of3) Flux<BaseMessage> baseMessagesReturnedByReqMessage(@NonNull Flux<String> reqMessageFlux) ... ");
    Flux<BaseMessage> filter = reqMessageFlux
        .map(msg -> {
          try {
            BaseMessage decode = BaseMessageDecoder.decode(msg);
            log.debug("... (2of3) Flux<BaseMessage> baseMessagesReturnedByReqMessage(@NonNull Flux<String> reqMessageFlux) ... ");
            return decode;
          } catch (JsonProcessingException e) {
            throw new NostrException(String.format("%s flux bad not good", getClass().getSimpleName()), e);
          }
        })
        .filter(Objects::nonNull);
    log.debug("... (3of3) Flux<BaseMessage> baseMessagesReturnedByReqMessage(@NonNull Flux<String> reqMessageFlux) ... ");
    return filter;
  }

  private Flux<BaseMessage> baseMessagesReturnedByReqMessageFlatMap(@NonNull Flux<String> reqMessageFlux) {
    log.debug("... (1of3) Flux<BaseMessage> baseMessagesReturnedByReqMessageFlatap(@NonNull Flux<String> reqMessageFlux) ... ");
    Function<String, Publisher<BaseMessage>> mapper = s -> {
      try {
        return Flux.just(BaseMessageDecoder.decode(s));
      } catch (JsonProcessingException e) {
        log.debug("Flux.just(BaseMessageDecoder.decode(s)) shit the bed");
        throw new RuntimeException(e);
      }
    };
    Flux<BaseMessage> baseMessageFlux = reqMessageFlux.flatMapSequential(mapper);
    return baseMessageFlux;
  }

  private WebSocketClient getReactiveWebSocketClient() {
    return Objects.isNull(sslBundles) ?
        new WebSocketClient(relayUrl) :
        new WebSocketClient(relayUrl, sslBundles);
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
    webSocketClients.forEach(WebSocketClient::closeSocket);
  }
}
