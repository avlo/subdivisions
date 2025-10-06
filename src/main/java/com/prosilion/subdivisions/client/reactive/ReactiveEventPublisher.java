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

  public void send(@NonNull CanonicalAuthenticationMessage canonicalAuthenticationMessage, @NonNull Subscriber<OkMessage> subscriber) {
    try {
      sendMessage(canonicalAuthenticationMessage, subscriber);
    } catch (Exception e) {
      Flux.just(new OkMessage(canonicalAuthenticationMessage.getEvent().getId(),
          false,
          "error during authentication: server returned unknown response"));
    }
  }

  public void send(@NonNull EventMessage eventMessage, @NonNull Subscriber<OkMessage> subscriber) {
    log.debug("socket send EventMessage content\n  {}", eventMessage.getEvent());
    try {
      sendMessage(eventMessage, subscriber);
    } catch (Exception e) {
      Flux.just(new OkMessage(
          eventMessage.getEvent().getId(),
          false,
          "error: server returned unknown response"));
    }
  }

  private <T extends BaseMessage> void sendMessage(@NonNull T eventMessage, @NonNull Subscriber<OkMessage> subscriber) throws JsonProcessingException {
    Flux<OkMessage> map = eventSocketClient
        .send(eventMessage) // sending an event...
        .take(Long.MAX_VALUE)
        .map(OkMessage::decode); // ... of type OkMessage, and ignores any others (i.e., EOSE message)
    map.subscribe(subscriber);
  }

  public void closeSocket() {
    eventSocketClient.closeSocket();
  }
}
