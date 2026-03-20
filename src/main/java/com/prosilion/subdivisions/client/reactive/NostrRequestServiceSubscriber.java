package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.NostrException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.ReqMessage;
import java.util.Objects;
import org.reactivestreams.Subscriber;
import org.springframework.lang.NonNull;

public class NostrRequestServiceSubscriber {
  private ReactiveRequestConsolidator requestConsolidator;

  public <T extends BaseMessage> void send(
      @NonNull ReqMessage reqMessage,
      @NonNull String relayUrl,
      @NonNull Subscriber<T> subscriber) throws JsonProcessingException, NostrException {
    if (Objects.isNull(this.requestConsolidator))
      requestConsolidator = new ReactiveRequestConsolidator();
    this.requestConsolidator.send(reqMessage, subscriber, relayUrl);
  }

  public void disconnect(@NonNull String subscriptionId) {
    this.requestConsolidator.removeRelay(subscriptionId);
  }
}
