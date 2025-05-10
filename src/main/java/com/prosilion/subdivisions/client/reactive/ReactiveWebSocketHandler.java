package com.prosilion.subdivisions.client.reactive;

import lombok.NonNull;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.URI;
import java.util.Optional;

class ReactiveWebSocketHandler {
  private final Sinks.Many<String> sendBuffer;
  private final Sinks.Many<String> receiveBuffer;
  private Disposable subscription;
  private WebSocketSession session;

  protected ReactiveWebSocketHandler() {
//    TODO: revisit, possibly other options/approaches
    this.sendBuffer = Sinks.many().multicast().onBackpressureBuffer();
    this.receiveBuffer = Sinks.many().multicast().onBackpressureBuffer();
  }

  protected void connect(@NonNull WebSocketClient webSocketClient, @NonNull URI uri) {
    subscription =
        webSocketClient
            .execute(
                uri, this::handleSession)
            .then(
// below, Create Mono that completes empty once provided Runnable has been executed
                Mono.fromRunnable(this::onClose))
// or Create Mono, producing its value using the provided Supplier.
//              fromSupplier(java.util.function.Supplier<? extends T> supplier)
            .subscribe();
  }

  protected void send(@NonNull String message) {
    sendBuffer.tryEmitNext(message);
  }

  private Mono<Void> handleSession(WebSocketSession session) {
    onOpen(session);

    Mono<Void> input =
        session
            .receive()
            .map(WebSocketMessage::getPayloadAsText)
            .doOnNext(receiveBuffer::tryEmitNext)
            .then();

    Mono<Void> output =
        session
            .send(
                sendBuffer
                    .asFlux()
                    .map(session::textMessage)
            );

    return
        Mono
            .zip(input, output)
            .then();
  }

  protected Flux<String> receive() {
    return receiveBuffer.asFlux();
  }

  protected void disconnect() {
    if (subscription != null && !subscription.isDisposed()) {
      subscription.dispose();
      subscription = null;
      onClose();
    }
  }

  protected Optional<WebSocketSession> session() {
    return Optional.ofNullable(session);
  }

  private void onOpen(WebSocketSession session) {
    this.session = session;
  }

  private void onClose() {
    session = null;
  }
}
