package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.NostrException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.ReqMessage;
import com.prosilion.subdivisions.client.RequestSubscriber;
import java.util.List;
import java.util.Objects;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

public class NostrRequestService {
  @Generated
  private static final Logger log = LoggerFactory.getLogger(NostrRequestService.class);
  private ReactiveRequestConsolidator requestConsolidator;

  public List<BaseMessage> send(@NonNull ReqMessage reqMessage, @NonNull String relayUrl) throws JsonProcessingException, NostrException {
    if (Objects.isNull(this.requestConsolidator))
      requestConsolidator = new ReactiveRequestConsolidator();
    RequestSubscriber<BaseMessage> subscriber = new RequestSubscriber<>();
    this.requestConsolidator.send(reqMessage, subscriber, relayUrl);
    return subscriber.getItems();
  }

  public void disconnect(@NonNull String subscriptionId) {
    this.requestConsolidator.removeRelay(subscriptionId);
  }
}
