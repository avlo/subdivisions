package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.event.EventIF;
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

  public ReactiveEventPublisher(@NonNull String relayUri) {
    log.debug("relayUri: \n{}", relayUri);
    this.eventSocketClient = new ReactiveWebSocketClient(relayUri);
  }

  public ReactiveEventPublisher(@NonNull String relayUri, SslBundles sslBundles) {
    log.debug("sslBundles: \n{}", sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
    this.eventSocketClient = new ReactiveWebSocketClient(relayUri);
  }

  public <T extends OkMessage> void send(@NonNull EventMessage eventMessage, @NonNull Subscriber<T> subscriber) {
    debug(eventMessage, eventMessage.getEvent());
    getFlux(eventMessage, subscriber);
  }

  public <T extends OkMessage> void send(@NonNull CanonicalAuthenticationMessage authMessage, @NonNull Subscriber<T> subscriber) {
    debug(authMessage, authMessage.getEvent());
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

  private void debug(BaseMessage baseMessage, EventIF event) {
    log.debug("{} send {} content\n  {}", getClass().getSimpleName(), baseMessage.getClass().getSimpleName(), event);
  }

  public void closeSocket() {
    eventSocketClient.closeSocket();
  }
}
