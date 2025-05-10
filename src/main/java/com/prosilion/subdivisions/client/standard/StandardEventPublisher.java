package com.prosilion.subdivisions.client.standard;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;

@Slf4j
public class StandardEventPublisher {
  private final StandardWebSocketClient eventSocketClient;

  public StandardEventPublisher(@NonNull String relayUri) throws ExecutionException, InterruptedException {
    log.debug("relayUri: \n{}", relayUri);
    this.eventSocketClient = new StandardWebSocketClient(relayUri);
    log.debug("eventSocketClient: \n{}", this.eventSocketClient);
  }

  public StandardEventPublisher(@NonNull String relayUri, SslBundles sslBundles) throws ExecutionException, InterruptedException {
    log.debug("sslBundles: \n{}", sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
    this.eventSocketClient = new StandardWebSocketClient(relayUri, sslBundles);
  }

  public OkMessage sendEvent(@NonNull String eventJson) throws IOException {
    eventSocketClient.send(eventJson);
    log.debug("socket send event JSON content\n  {}", eventJson);
    return getDecoded();
  }

  public OkMessage sendEvent(@NonNull EventMessage eventMessage) throws IOException {
    eventSocketClient.send(eventMessage);
    log.debug("socket send EventMessage content\n  {}", eventMessage.getEvent());
    return getDecoded();
  }

  private OkMessage getDecoded() {
    return OkMessage.decode(eventSocketClient.getEvents().getFirst());
  }
}
