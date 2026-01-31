package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.NostrException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.CanonicalAuthenticationMessage;
import com.prosilion.nostr.message.EventMessage;
import com.prosilion.nostr.message.OkMessage;
import com.prosilion.nostr.message.ReqMessage;
import java.io.IOException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;

@Slf4j
public class ReactiveNostrRelayClient {
  private final ReactiveEventPublisher reactiveEventPublisher;
  private final ReactiveRelaySubscriptionsManager reactiveRelaySubscriptionsManager;

  public ReactiveNostrRelayClient(@NonNull String relayUrl) {
    log.debug("{} constructor called with relay url {}", getClass().getSimpleName(), relayUrl);
    this.reactiveEventPublisher = new ReactiveEventPublisher(relayUrl);
    this.reactiveRelaySubscriptionsManager = new ReactiveRelaySubscriptionsManager(relayUrl);
  }

  public ReactiveNostrRelayClient(@NonNull String relayUrl, SslBundles sslBundles) {
    log.debug("{} constructor called with relay url {} and sslBundles {}", getClass().getSimpleName(), relayUrl, sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
    this.reactiveEventPublisher = new ReactiveEventPublisher(relayUrl, sslBundles);
    this.reactiveRelaySubscriptionsManager = new ReactiveRelaySubscriptionsManager(relayUrl, sslBundles);
  }

  public void send(@NonNull EventMessage eventMessage, @NonNull Subscriber<OkMessage> subscriber) throws IOException {
    reactiveEventPublisher.send(eventMessage, subscriber);
  }

  public void send(@NonNull CanonicalAuthenticationMessage authMessage, @NonNull Subscriber<OkMessage> subscriber) throws NostrException {
    reactiveEventPublisher.send(authMessage, subscriber);
  }

  public <T extends BaseMessage> void send(@NonNull ReqMessage reqMessage, @NonNull Subscriber<T> subscriber) throws JsonProcessingException, NostrException {
    reactiveRelaySubscriptionsManager.send(reqMessage, subscriber);
  }

  public void closeSocket() {
    reactiveEventPublisher.closeSocket();
    reactiveRelaySubscriptionsManager.closeAllSessions();
  }
}
