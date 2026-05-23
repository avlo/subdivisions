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
class WebSocketClient {
  private final WebSocketHandler webSocketHandler;

  WebSocketClient(@NonNull String relayUrl) {
    log.debug("{} Ctor() called with relay url: [{}]", getClass().getSimpleName(), relayUrl);
    this.webSocketHandler = new WebSocketHandler();
    log.debug("... call new ReactiveWebSocketHandler hashCode: [{}]", webSocketHandler.hashCode());
    ReactorNettyWebSocketClient webSocketClient = new ReactorNettyWebSocketClient();
//    webSocketClient.getHttpClient().warmup();
    webSocketHandler.connect(webSocketClient, getURI(relayUrl));
  }

  WebSocketClient(@NonNull String relayUrl, @NonNull SslBundles sslBundles) {
    log.debug("{} constructor called with relay url: [{}], sslBundles [{}]", getClass().getSimpleName(), relayUrl, sslBundles);
    this.webSocketHandler = new WebSocketHandler();
    final ReactorNettyWebSocketClient reactorNettyWebSocketClient = new ReactorNettyWebSocketClient();
//    TODO: Secure (WSS) WebSocket needs impl
    log.info("WARNING: **** Secure (WSS) WebSocket implementation is incomplete, currently+ NOT using wss ****");
//    reactorNettyWebSocketClient.setSslContext(sslBundles.getBundle("server").createSslContext());
    webSocketHandler.connect(reactorNettyWebSocketClient, getURI(relayUrl));
    log.debug("Secure (WSS) WebSocket subdivisions connected {}", webSocketHandler.session().orElseThrow().getId());
  }

  <T extends BaseMessage> Flux<String> send(T message) {
    String encodedMessage = null;  // explicitly put here for throws
    try {
      encodedMessage = message.encode();
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    String finalEncodedMessage = encodedMessage;
    Flux<String> stringFlux = Mono
        .fromRunnable(
            () -> webSocketHandler.send(finalEncodedMessage))
        .thenMany(
            webSocketHandler.receive());
    return stringFlux;
  }

  void closeSocket() {
    webSocketHandler.disconnect();
  }

  Flux<String> disconnect(@NonNull String subscriptionId) throws NostrException {
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
