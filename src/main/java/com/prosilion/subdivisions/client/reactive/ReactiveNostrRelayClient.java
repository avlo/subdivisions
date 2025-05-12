package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import nostr.event.message.ReqMessage;
import org.reactivestreams.Subscriber;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import reactor.core.publisher.Flux;

@Slf4j
public class ReactiveNostrRelayClient {
  private final ReactiveEventPublisher<OkMessage> reactiveEventPublisher;
  private final ReactiveRelaySubscriptionsManager reactiveRelaySubscriptionsManager;

  public ReactiveNostrRelayClient(@NonNull String relayUri) {
    log.debug("relayUri: \n{}", relayUri);
    this.reactiveEventPublisher = new ReactiveEventPublisher<>(relayUri);
    this.reactiveRelaySubscriptionsManager = new ReactiveRelaySubscriptionsManager(relayUri);
  }

  public ReactiveNostrRelayClient(@NonNull String relayUri, SslBundles sslBundles
  ) throws ExecutionException, InterruptedException {
    log.debug("relayUri: \n{}", relayUri);
    log.debug("sslBundles: \n{}", sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
    this.reactiveEventPublisher = new ReactiveEventPublisher<>(relayUri, sslBundles);
    this.reactiveRelaySubscriptionsManager = new ReactiveRelaySubscriptionsManager(relayUri, sslBundles);
  }

  public void send(@NonNull EventMessage eventMessage, @NonNull Subscriber<OkMessage> subscriber) throws IOException {
    reactiveEventPublisher.send(eventMessage, subscriber);
  }

  public Flux<GenericEvent> send(@NonNull ReqMessage reqMessage) throws JsonProcessingException {
    return reactiveRelaySubscriptionsManager.send(reqMessage);
  }
}
