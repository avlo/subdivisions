package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import nostr.event.message.CloseMessage;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class ReactiveWebSocketClient {
  private final ReactiveWebSocketHandler reactiveWebSocketHandler;

  public ReactiveWebSocketClient(@NonNull String relayUrl) {
    this.reactiveWebSocketHandler = new ReactiveWebSocketHandler();
    System.out.println("call new ReactiveWebSocketHandler hashCode: [ " + reactiveWebSocketHandler.hashCode() + " ]");
    reactiveWebSocketHandler.connect(new ReactorNettyWebSocketClient(), getURI(relayUrl));
  }

  public ReactiveWebSocketClient(@NonNull String relayUri, @NonNull SslBundles sslBundles) {
    this.reactiveWebSocketHandler = new ReactiveWebSocketHandler();
    final ReactorNettyWebSocketClient reactorNettyWebSocketClient = new ReactorNettyWebSocketClient();
//    TODO: Secure (WSS) WebSocket needs impl
    log.info("WARNING: **** Secure (WSS) WebSocket implementation is incomplete, currently+ NOT using wss ****");
//    reactorNettyWebSocketClient.setSslContext(sslBundles.getBundle("server").createSslContext());
    reactiveWebSocketHandler.connect(reactorNettyWebSocketClient, getURI(relayUri));
    log.debug("Secure (WSS) WebSocket subdivisions connected {}", reactiveWebSocketHandler.session().orElseThrow().getId());
  }

  public <T extends BaseMessage> Flux<String> send(T message) throws JsonProcessingException {
    log.debug("111111111111111111111111");
    log.debug("111111111111111111111111");
    System.out.println("111111111111111111111111");
    System.out.println("111111111111111111111111");
    System.out.println("ReactiveWebSocketClient hashCode: [" + this.hashCode() + "]");
    log.debug("ReactiveWebSocketClient reactiveWebSocketHandler.hashCode: [{}]", reactiveWebSocketHandler.hashCode());
    System.out.println("ReactiveWebSocketClient reactiveWebSocketHandler.hashCode: [" + reactiveWebSocketHandler.hashCode() + "]");

    System.out.println("++++++++++++++++++++++++");
    System.out.println("message: [" + message + "]");
    System.out.println("------------------------");

    String encodedMessage = message.encode();
    log.debug("ReactiveWebSocketClient send().message: \n{}", encodedMessage);
    System.out.println("ReactiveWebSocketClient send().message: \n" + encodedMessage);
    System.out.println("++++++++++++++++++++++++");
    System.out.println("111111111111111111111111");
    System.out.println("111111111111111111111111");
    log.debug("111111111111111111111111");
    log.debug("111111111111111111111111");
    return Mono
        .fromRunnable(
            () -> reactiveWebSocketHandler.send(encodedMessage))
        .thenMany(
            reactiveWebSocketHandler.receive().map(String::trim));
  }

  public void closeSocket() {
    reactiveWebSocketHandler.disconnect();
  }

  public Flux<String> disconnect(@NonNull String subscriptionId) throws JsonProcessingException {
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
