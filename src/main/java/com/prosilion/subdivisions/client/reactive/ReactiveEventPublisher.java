package com.prosilion.subdivisions.client.reactive;

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

  public <T extends OkMessage> Flux<T> send(@NonNull EventMessage eventMessage, @NonNull Subscriber<T> subscriber) {
    log.debug("socket send EventMessage content\n  {}", eventMessage.getEvent());
    try {
      Flux<T> map = eventSocketClient
          .send(eventMessage) // sending an event...
          .take(Long.MAX_VALUE)
          .map(OkMessage::decode); // ... of type OkMessage, and ignores any others (i.e., EOSE message)
      map.subscribe(subscriber);
      return map;
    } catch (Exception e) {
      return Flux.just((T) new OkMessage(eventMessage.getEvent().getEventId(), false, "error: server returned unknown response"));
    }
  }
}
