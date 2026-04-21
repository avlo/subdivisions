package com.prosilion.subdivisions.client.reactive;

import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.CanonicalAuthenticationMessage;
import com.prosilion.nostr.message.OkMessage;
import com.prosilion.nostr.message.ReqMessage;
import com.prosilion.subdivisions.client.RequestSubscriber;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.reactivestreams.Subscriber;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Flux;

public class NostrSingleRequestService {
  public List<BaseMessage> send(
      @NonNull ReqMessage reqMessage,
      @NonNull String relayUrl) {
    return send(reqMessage, relayUrl, RequestSubscriber.DEFAULT_TIMEOUT_3000_MS);
  }

  public List<BaseMessage> send(
      @NonNull ReqMessage reqMessage,
      @NonNull String relayUrl,
      @NonNull Duration timeout) {
    return sendAuthenticated(reqMessage, relayUrl, new RequestSubscriber<>(timeout));
  }

  public SingleRelaySubscriptionsManager send(
      @NonNull ReqMessage reqMessage,
      @NonNull String relayUrl,
      @NonNull RequestSubscriber<BaseMessage> subscriber) {
    return sendUsingSubscriptionsManager(reqMessage, relayUrl, subscriber);
  }

  //  TODO: impl auth
  public List<BaseMessage> send(
      @NonNull CanonicalAuthenticationMessage authMessage,
      @NonNull ReqMessage reqMessage,
      @NonNull String relayUrl,
      @NonNull RequestSubscriber<BaseMessage> subscriber) {
//    RequestSubscriber<BaseMessage> authSubscriber = new RequestSubscriber<>();
//    getFlux(authMessage, authSubscriber);
//    return subscriber.getItems().stream()
//        .filter(OkMessage.class::isInstance)
//        .map(OkMessage::getClass)
//        .findFirst()
//        .filter(okMessage -> Boolean.FALSE.equals(okMessage.getFlag()))
//        .orElseGet(() -> sendAuthenticated(reqMessage, relayUrl, authSubscriber));
    return new ArrayList<>();
  }

  private List<BaseMessage> sendAuthenticated(
      ReqMessage reqMessage,
      String relayUrl,
      RequestSubscriber<BaseMessage> subscriber) {
    SingleRelaySubscriptionsManager manager = sendUsingSubscriptionsManager(reqMessage, relayUrl, subscriber);
    List<BaseMessage> items = subscriber.getItems();
    subscriber.dispose();
    manager.closeAllSessions();
    return items;
  }

  private SingleRelaySubscriptionsManager sendUsingSubscriptionsManager(
      ReqMessage reqMessage,
      String relayUrl,
      RequestSubscriber<BaseMessage> subscriber) {
    SingleRelaySubscriptionsManager manager = new SingleRelaySubscriptionsManager(relayUrl);
    manager.send(reqMessage, subscriber);
    return manager;
  }

  private <T extends BaseMessage> void getFlux(CanonicalAuthenticationMessage baseMessage, Subscriber<T> subscriber) {
    Flux<T> map = new WebSocketClient("asdsd")
        .send(baseMessage) // sending an event...
        .take(Long.MAX_VALUE)
        .map(OkMessage::decode); // ... of type OkMessage, and ignores any others (i.e., EOSE message)
    map.subscribe(subscriber);
  }
}
