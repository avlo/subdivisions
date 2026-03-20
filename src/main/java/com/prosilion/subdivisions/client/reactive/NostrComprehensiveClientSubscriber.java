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
public class NostrComprehensiveClientSubscriber {
  private final NostrEventPublisherSubscriber nostrEventPublisherSubscriber;
  private final ReactiveSubscriptionsManager reactiveSubscriptionsManager;

  public NostrComprehensiveClientSubscriber(@NonNull String relayUrl) {
    log.debug("{} Ctor() called with relay url: [{}]", getClass().getSimpleName(), relayUrl);
    this.nostrEventPublisherSubscriber = new NostrEventPublisherSubscriber(relayUrl);
    this.reactiveSubscriptionsManager = new ReactiveSubscriptionsManager(relayUrl);
  }

  public NostrComprehensiveClientSubscriber(@NonNull String relayUrl, SslBundles sslBundles) {
    log.debug("{} constructor called with relay url {} and sslBundles {}", getClass().getSimpleName(), relayUrl, sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
    this.nostrEventPublisherSubscriber = new NostrEventPublisherSubscriber(relayUrl, sslBundles);
    this.reactiveSubscriptionsManager = new ReactiveSubscriptionsManager(relayUrl, sslBundles);
  }

  public <T extends OkMessage> void send(@NonNull EventMessage eventMessage, @NonNull Subscriber<T> subscriber) throws IOException {
    nostrEventPublisherSubscriber.send(eventMessage, subscriber);
  }

  public <T extends OkMessage> void send(@NonNull CanonicalAuthenticationMessage authMessage, @NonNull Subscriber<T> subscriber) throws NostrException {
    nostrEventPublisherSubscriber.send(authMessage, subscriber);
  }

  public <T extends BaseMessage> void send(@NonNull ReqMessage reqMessage, @NonNull Subscriber<T> subscriber) throws JsonProcessingException, NostrException {
    reactiveSubscriptionsManager.send(reqMessage, subscriber);
  }

  public void closeSocket() {
    nostrEventPublisherSubscriber.closeSocket();
    reactiveSubscriptionsManager.closeAllSessions();
  }
}
