package com.prosilion.subdivisions.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.NostrException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.ReqMessage;
import com.prosilion.subdivisions.client.reactive.ReactiveRequestConsolidator;
import java.util.List;
import lombok.Generated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

public class NostrRelayReqService {
  @Generated
  private static final Logger log = LoggerFactory.getLogger(NostrRelayReqService.class);
  private final ReactiveRequestConsolidator requestConsolidator;

  public NostrRelayReqService() {
    log.debug("constructor (using ReactiveRequestConsolidator)");
    this.requestConsolidator = new ReactiveRequestConsolidator();
  }

  public List<BaseMessage> send(@NonNull ReqMessage reqMessage, @NonNull String relayUrl) throws JsonProcessingException, NostrException {
    TestSubscriber<BaseMessage> subscriber = new TestSubscriber<>();
    this.requestConsolidator.send(reqMessage, subscriber, relayUrl);
    return subscriber.getItems();
  }

  public void disconnect(@NonNull String subscriptionId) {
    this.requestConsolidator.removeRelay(subscriptionId);
  }
}
