package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.NostrException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.ReqMessage;
import com.prosilion.subdivisions.client.RequestSubscriber;
import java.time.Duration;
import java.util.List;
import org.springframework.lang.NonNull;

public class NostrRequestService {
  private final NostrRequestServiceSubscriber nostrRequestServiceSubscriber;

  public NostrRequestService(@NonNull ReactiveRequestConsolidator reactiveRequestConsolidator) {
    this.nostrRequestServiceSubscriber = new NostrRequestServiceSubscriber(reactiveRequestConsolidator);
  }

  public List<BaseMessage> send(@NonNull ReqMessage reqMessage, @NonNull String relayUrl) throws JsonProcessingException, NostrException {
    return send(reqMessage, relayUrl, new RequestSubscriber<>());
  }

  public List<BaseMessage> send(@NonNull ReqMessage reqMessage, @NonNull String relayUrl, @NonNull Duration timeout) throws JsonProcessingException, NostrException {
    return send(reqMessage, relayUrl, new RequestSubscriber<>(timeout));
  }

  private List<BaseMessage> send(@NonNull ReqMessage reqMessage, @NonNull String relayUrl, RequestSubscriber<BaseMessage> subscriber) throws JsonProcessingException, NostrException {
    nostrRequestServiceSubscriber.send(reqMessage, relayUrl, subscriber);
    return subscriber.getItems();
  }
}
