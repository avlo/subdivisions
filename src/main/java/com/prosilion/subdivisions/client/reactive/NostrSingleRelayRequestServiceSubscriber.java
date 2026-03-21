package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.NostrException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.ReqMessage;
import org.reactivestreams.Subscriber;
import org.springframework.lang.NonNull;

public class NostrSingleRelayRequestServiceSubscriber {
  private final ReactiveSubscriptionsManager reactiveSubscriptionsManager;

  public NostrSingleRelayRequestServiceSubscriber(@NonNull String relayUrl) {
    this.reactiveSubscriptionsManager = new ReactiveSubscriptionsManager(relayUrl);
  }

  public <T extends BaseMessage> void send(
      @NonNull ReqMessage reqMessage,
      @NonNull Subscriber<T> subscriber) throws JsonProcessingException, NostrException {
    this.reactiveSubscriptionsManager.send(reqMessage, subscriber);
  }
}
