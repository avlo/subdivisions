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
    this.reactiveWebSocketHandler = new ReactiveWebSocketHandler();
    System.out.println("call new ReactiveWebSocketHandler hashCode: [ " + reactiveWebSocketHandler.hashCode() + " ]");
    reactiveWebSocketHandler.connect(new ReactorNettyWebSocketClient(), getURI(relayUrl));
  }

  protected ReactiveWebSocketClient(@NonNull String relayUri, @NonNull SslBundles sslBundles) {
    this.reactiveWebSocketHandler = new ReactiveWebSocketHandler();
    final ReactorNettyWebSocketClient reactorNettyWebSocketClient = new ReactorNettyWebSocketClient();
//    TODO: Secure (WSS) WebSocket needs impl
    log.info("WARNING: **** Secure (WSS) WebSocket implementation is incomplete, currently+ NOT using wss ****");
//    reactorNettyWebSocketClient.setSslContext(sslBundles.getBundle("server").createSslContext());
    reactiveWebSocketHandler.connect(reactorNettyWebSocketClient, getURI(relayUri));
    log.debug("Secure (WSS) WebSocket subdivisions connected {}", reactiveWebSocketHandler.session().orElseThrow().getId());
  }

  protected <T extends BaseMessage> Flux<String> send(T message) throws JsonProcessingException, NostrException {
    String encodedMessage = message.encode();  // explicitly put here for throws
    return Mono
        .fromRunnable(
            () -> reactiveWebSocketHandler.send(encodedMessage))
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
