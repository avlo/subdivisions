package com.prosilion.subdivisions.client.reactive;

import com.prosilion.nostr.message.CanonicalAuthenticationMessage;
import com.prosilion.nostr.message.EventMessage;
import com.prosilion.nostr.message.OkMessage;
import com.prosilion.subdivisions.client.RequestSubscriber;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;

@Slf4j
public class NostrEventPublisher {
  private final NostrEventPublisherSubscriber nostrEventPublisherSubscriber;

  public NostrEventPublisher(@NonNull String relayUrl) {
    log.debug("{} Ctor called with relay url: [{}]", getClass().getSimpleName(), relayUrl);
    this.nostrEventPublisherSubscriber = new NostrEventPublisherSubscriber(relayUrl);
  }

  public NostrEventPublisher(@NonNull String relayUrl, SslBundles sslBundles) {
    log.debug("{} constructor called with relay url {} and sslBundles {}", getClass().getSimpleName(), relayUrl, sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
    this.nostrEventPublisherSubscriber = new NostrEventPublisherSubscriber(relayUrl);
  }

  public OkMessage send(@NonNull EventMessage eventMessage) {
    RequestSubscriber<OkMessage> subscriber = new RequestSubscriber<>();
    nostrEventPublisherSubscriber.send(eventMessage, subscriber);
    return subscriber.getItems().getFirst();
  }

  public OkMessage send(@NonNull CanonicalAuthenticationMessage authMessage) {
    RequestSubscriber<OkMessage> subscriber = new RequestSubscriber<>();
    nostrEventPublisherSubscriber.send(authMessage, subscriber);
    return subscriber.getItems().getFirst();
  }

  public void closeSocket() {
    nostrEventPublisherSubscriber.closeSocket();
  }
}
