package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.NostrException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.CloseMessage;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
class ReactiveWebSocketClient {
  private final ReactiveWebSocketHandler reactiveWebSocketHandler;

  protected ReactiveWebSocketClient(@NonNull String relayUrl) {
    log.debug("{} Ctor() called with relay url: [{}]", getClass().getSimpleName(), relayUrl);
    this.reactiveWebSocketHandler = new ReactiveWebSocketHandler();
    log.debug("... call new ReactiveWebSocketHandler hashCode: [{}]", reactiveWebSocketHandler.hashCode());
    ReactorNettyWebSocketClient webSocketClient = new ReactorNettyWebSocketClient();
    reactiveWebSocketHandler.connect(webSocketClient, getURI(relayUrl));
  }

  protected ReactiveWebSocketClient(@NonNull String relayUrl, @NonNull SslBundles sslBundles) {
    log.debug("{} constructor called with relay url: [{}], sslBundles [{}]", getClass().getSimpleName(), relayUrl, sslBundles);
    this.reactiveWebSocketHandler = new ReactiveWebSocketHandler();
    final ReactorNettyWebSocketClient reactorNettyWebSocketClient = new ReactorNettyWebSocketClient();
//    TODO: Secure (WSS) WebSocket needs impl
    log.info("WARNING: **** Secure (WSS) WebSocket implementation is incomplete, currently+ NOT using wss ****");
//    reactorNettyWebSocketClient.setSslContext(sslBundles.getBundle("server").createSslContext());
    reactiveWebSocketHandler.connect(reactorNettyWebSocketClient, getURI(relayUrl));
    log.debug("Secure (WSS) WebSocket subdivisions connected {}", reactiveWebSocketHandler.session().orElseThrow().getId());
  }

  protected <T extends BaseMessage> Flux<String> send(T message) {
    String encodedMessage = null;  // explicitly put here for throws
    try {
      encodedMessage = message.encode();
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    String finalEncodedMessage = encodedMessage;
    return Mono
        .fromRunnable(
            () -> reactiveWebSocketHandler.send(finalEncodedMessage))
        .thenMany(
            reactiveWebSocketHandler.receive().map(String::trim));
  }

  protected void closeSocket() {
    reactiveWebSocketHandler.disconnect();
  }

  protected Flux<String> disconnect(@NonNull String subscriptionId) throws JsonProcessingException, NostrException {
    Flux<String> stringFlux = send(new CloseMessage(subscriptionId));
    closeSocket();
    return stringFlux;
  }

  private URI getURI(@NonNull String relayUrl) {
    try {
      return new URI(relayUrl);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
