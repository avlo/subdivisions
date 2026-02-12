package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.CanonicalAuthenticationMessage;
import com.prosilion.nostr.message.EventMessage;
import com.prosilion.nostr.message.OkMessage;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import reactor.core.publisher.Flux;

@Slf4j
public class ReactiveEventPublisher {
  private final ReactiveWebSocketClient eventSocketClient;

  public ReactiveEventPublisher(@NonNull String relayUrl) {
    log.debug("{} Ctor called with relay url: [{}]", getClass().getSimpleName(), relayUrl);
    this.eventSocketClient = new ReactiveWebSocketClient(relayUrl);
  }

  public ReactiveEventPublisher(@NonNull String relayUrl, SslBundles sslBundles) {
    log.debug("{} constructor called with relay url {} and sslBundles {}", getClass().getSimpleName(), relayUrl, sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
    this.eventSocketClient = new ReactiveWebSocketClient(relayUrl);
  }

  public <T extends OkMessage> void send(@NonNull EventMessage eventMessage, @NonNull Subscriber<T> subscriber) {
    log.debug("{} send(eventMessage, subscriber) [{}] content:\n{}",
        getClass().getSimpleName(),
        subscriber,
        eventMessage.getEvent().createPrettyPrintJson());
    getFlux(eventMessage, subscriber);
  }

  public <T extends OkMessage> void send(@NonNull CanonicalAuthenticationMessage authMessage, @NonNull Subscriber<T> subscriber) {
    log.debug("{} send(CanonicalAuthenticationMessage, subscriber) [{}] content:\n{}",
        getClass().getSimpleName(),
        subscriber,
        authMessage.event().createPrettyPrintJson());
    getFlux(authMessage, subscriber);
  }

  private <T extends OkMessage> void getFlux(BaseMessage baseMessage, Subscriber<T> subscriber) {
    try {
      Flux<T> map = eventSocketClient
          .send(baseMessage) // sending an event...
          .take(Long.MAX_VALUE)
          .map(OkMessage::decode); // ... of type OkMessage, and ignores any others (i.e., EOSE message)
      map.subscribe(subscriber);
    } catch (JsonProcessingException jpe) {
      Flux.just((T) new OkMessage(
              baseMessage.toString(), false,
              String.format(
                  "%s error: server returned unknown response for JSON content\n  %s",
                  getClass().getSimpleName(),
                  baseMessage)))
          .subscribe(subscriber);
    }
  }

  public void closeSocket() {
    eventSocketClient.closeSocket();
  }
}
