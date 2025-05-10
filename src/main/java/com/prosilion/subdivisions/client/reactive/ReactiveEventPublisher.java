package com.prosilion.subdivisions.client.reactive;

import java.io.IOException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
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

  public Flux<OkMessage> send(@NonNull EventMessage eventMessage) throws IOException {
    log.debug("socket send EventMessage content\n  {}", eventMessage.getEvent());
    return eventSocketClient.send(eventMessage).map(OkMessage::decode);
  }
}
