package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.NostrException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.ReqMessage;
import org.reactivestreams.Subscriber;
import org.springframework.lang.NonNull;

public class NostrMeshRequestServiceSubscriber {
  private final ReactiveRequestConsolidator requestConsolidator;

  public NostrMeshRequestServiceSubscriber(@NonNull ReactiveRequestConsolidator reactiveRequestConsolidator) {
    this.requestConsolidator = reactiveRequestConsolidator;
  }

  public <T extends BaseMessage> void send(
      @NonNull ReqMessage reqMessage,
      @NonNull String relayUrl,
      @NonNull Subscriber<T> subscriber) throws JsonProcessingException, NostrException {
    this.requestConsolidator.send(reqMessage, subscriber, relayUrl);
  }
}
