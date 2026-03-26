package com.prosilion.subdivisions.client.reactive;

import com.prosilion.nostr.message.CanonicalAuthenticationMessage;
import com.prosilion.nostr.message.EventMessage;
import com.prosilion.nostr.message.OkMessage;
import com.prosilion.subdivisions.client.RequestSubscriber;
import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

@Slf4j
public class NostrEventPublisher {
  private final EventPublisherSubscriber publisher;
  private boolean authenticated = false;

  public NostrEventPublisher(@NonNull String relayUrl) {
    this.publisher = new EventPublisherSubscriber(relayUrl);
  }

  public OkMessage send(
      @NonNull EventMessage eventMessage) {
    return send(eventMessage, new RequestSubscriber<>());
  }

  public OkMessage send(
      @NonNull EventMessage eventMessage,
      @NonNull Duration timeout) {
    return send(eventMessage, new RequestSubscriber<>(timeout));
  }

  public OkMessage send(
      @NonNull EventMessage eventMessage,
      @NonNull RequestSubscriber<OkMessage> subscriber) {
    return sendAuthenticated(eventMessage, subscriber);
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
      return sendAuthenticated(eventMessage, subscriber);

    publisher.send(authMessage, subscriber);
    OkMessage okMessage = subscriber.getItems().stream()
        .findFirst()
        .filter(message -> Boolean.FALSE.equals(message.getFlag()))
        .orElseGet(() -> sendAuthenticated(eventMessage, subscriber));
    authenticated = true;
    return okMessage;
  }

  private OkMessage sendAuthenticated(
      EventMessage eventMessage,
      RequestSubscriber<OkMessage> subscriber) {
    publisher.send(eventMessage, subscriber);
    return subscriber.getItems().getFirst();
  }

  public void closeSocket() {
    publisher.closeSocket();
  }
}
