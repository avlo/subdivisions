package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.NostrException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.ReqMessage;
import org.reactivestreams.Subscriber;
import org.springframework.lang.NonNull;

public class NostrRequestServiceSubscriber {
  private final ReactiveRequestConsolidator requestConsolidator;
  private final String relayUrl;

  public NostrRequestServiceSubscriber(@NonNull String relayUrl) {
    this.requestConsolidator = new ReactiveRequestConsolidator();
    this.relayUrl = relayUrl;
  }

  public <T extends BaseMessage> void send(
      @NonNull ReqMessage reqMessage,
      @NonNull Subscriber<T> subscriber) throws JsonProcessingException, NostrException {
    this.requestConsolidator.send(reqMessage, subscriber, relayUrl);
  }

  public void disconnect() {
    this.requestConsolidator.removeRelay(relayUrl);
  }
}
