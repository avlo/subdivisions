package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.enums.NostrException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.EventMessage;
import com.prosilion.nostr.message.OkMessage;
import com.prosilion.nostr.message.ReqMessage;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;

@Slf4j
public class ReactiveNostrRelayClient {
  private final ReactiveEventPublisher reactiveEventPublisher;
  private final ReactiveRelaySubscriptionsManager reactiveRelaySubscriptionsManager;

  public ReactiveNostrRelayClient(@NonNull String relayUri) {
    log.debug("relayUri: \n{}", relayUri);
    this.reactiveEventPublisher = new ReactiveEventPublisher(relayUri);
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
    this.reactiveEventPublisher = new ReactiveEventPublisher(relayUri, sslBundles);
    this.reactiveRelaySubscriptionsManager = new ReactiveRelaySubscriptionsManager(relayUri, sslBundles);
  }

  public void send(@NonNull EventMessage eventMessage, @NonNull Subscriber<OkMessage> subscriber) throws IOException {
    reactiveEventPublisher.send(eventMessage, subscriber);
  }

  public <T extends BaseMessage> void send(@NonNull ReqMessage reqMessage, @NonNull Subscriber<T> subscriber) throws JsonProcessingException, NostrException {
    reactiveRelaySubscriptionsManager.send(reqMessage, subscriber);
  }
}
