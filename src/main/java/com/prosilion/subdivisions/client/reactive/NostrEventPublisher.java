package com.prosilion.subdivisions.client.reactive;

import com.prosilion.nostr.message.CanonicalAuthenticationMessage;
import com.prosilion.nostr.message.EventMessage;
import com.prosilion.nostr.message.OkMessage;
import com.prosilion.subdivisions.client.RequestSubscriber;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import lombok.NonNull;

@Slf4j
public class NostrEventPublisher {
  private final EventPublisherSubscriber publisher;
  private boolean authenticated = false;

  public NostrEventPublisher(@NonNull String relayUrl) {
    this.publisher = new EventPublisherSubscriber(relayUrl);
  }

  public OkMessage send(
      @NonNull EventMessage eventMessage) {
    return sendUsingLocalPublisher(eventMessage, new RequestSubscriber<>());
  }

  public OkMessage send(
      @NonNull EventMessage eventMessage,
      @NonNull Duration timeout) {
    return sendUsingLocalPublisher(eventMessage, new RequestSubscriber<>(timeout));
  }

  public void send(
      @NonNull EventMessage eventMessage,
      @NonNull RequestSubscriber<OkMessage> subscriber) {
    sendToPublisher(eventMessage, subscriber);
  }

  public OkMessage send(
      @NonNull CanonicalAuthenticationMessage authMessage,
      @NonNull EventMessage eventMessage) {
    return send(authMessage, eventMessage, new RequestSubscriber<>());
  }

  public OkMessage send(
      @NonNull CanonicalAuthenticationMessage authMessage,
      @NonNull EventMessage eventMessage,
      @NonNull Duration timeout) {
    return send(authMessage, eventMessage, new RequestSubscriber<>(timeout));
  }

  public OkMessage send(
      @NonNull CanonicalAuthenticationMessage authMessage,
      @NonNull EventMessage eventMessage,
      @NonNull RequestSubscriber<OkMessage> subscriber) {
    if (authenticated)
      return sendUsingLocalPublisher(eventMessage, subscriber);

    publisher.send(authMessage, subscriber);
    OkMessage okMessage = subscriber.getItems().stream()
        .findFirst()
        .filter(message -> Boolean.FALSE.equals(message.getFlag()))
        .orElseGet(() -> sendUsingLocalPublisher(eventMessage, subscriber));
    authenticated = true;
    return okMessage;
  }

  private OkMessage sendUsingLocalPublisher(
      EventMessage eventMessage,
      RequestSubscriber<OkMessage> subscriber) {
    sendToPublisher(eventMessage, subscriber);
    OkMessage first = subscriber.getItems().getFirst();
    dispose();
    return first;
  }

  private void sendToPublisher(EventMessage eventMessage, RequestSubscriber<OkMessage> subscriber) {
    publisher.send(eventMessage, subscriber);
  }

  private void dispose() {
    publisher.dispose();
  }
}
