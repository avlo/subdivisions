package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.URI;
import java.net.URISyntaxException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import nostr.event.message.CloseMessage;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class ReactiveWebSocketClient {
  private final ReactiveWebSocketHandler reactiveWebSocketHandler;

  public ReactiveWebSocketClient(@NonNull String relayUrl) {
    this.reactiveWebSocketHandler = new ReactiveWebSocketHandler();
    this.reactiveWebSocketHandler.connect(new ReactorNettyWebSocketClient(), getURI(relayUrl));
  }

  public <T extends BaseMessage> Flux<String> send(T message) throws JsonProcessingException {
    log.debug("111111111111111111111111");
    log.debug("111111111111111111111111");
    System.out.println("111111111111111111111111");
    System.out.println("111111111111111111111111");
    log.debug("ReactiveWebSocketClient reactiveWebSocketHandler.hashCode: [{}]", reactiveWebSocketHandler.hashCode());
    System.out.println("ReactiveWebSocketClient reactiveWebSocketHandler.hashCode: [" + reactiveWebSocketHandler.hashCode() + "]");
    String encodedMessage = message.encode();
    log.debug("ReactiveWebSocketClient send().message: \n{}", encodedMessage);
    System.out.println("ReactiveWebSocketClient send().message: \n" + encodedMessage);
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
