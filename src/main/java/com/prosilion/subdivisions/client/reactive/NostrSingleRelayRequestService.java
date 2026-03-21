package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.NostrException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.ReqMessage;
import com.prosilion.subdivisions.client.RequestSubscriber;
import java.time.Duration;
import java.util.List;
import org.springframework.lang.NonNull;

public class NostrSingleRelayRequestService {
  private final NostrSingleRelayRequestServiceSubscriber nostrSingleRelayRequestServiceSubscriber;

  public NostrSingleRelayRequestService(@NonNull String relayUrl) {
    this.nostrSingleRelayRequestServiceSubscriber = new NostrSingleRelayRequestServiceSubscriber(relayUrl);
  }

  public List<BaseMessage> send(@NonNull ReqMessage reqMessage) throws JsonProcessingException, NostrException {
    return send(reqMessage, new RequestSubscriber<>());
  }

  public List<BaseMessage> send(@NonNull ReqMessage reqMessage, @NonNull Duration timeout) throws JsonProcessingException, NostrException {
    return send(reqMessage, new RequestSubscriber<>(timeout));
  }

  private List<BaseMessage> send(@NonNull ReqMessage reqMessage, RequestSubscriber<BaseMessage> subscriber) throws JsonProcessingException, NostrException {
    nostrSingleRelayRequestServiceSubscriber.send(reqMessage, subscriber);
    return subscriber.getItems();
  }
}
