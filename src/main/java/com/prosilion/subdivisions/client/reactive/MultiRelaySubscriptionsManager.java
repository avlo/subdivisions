package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.NostrException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.ReqMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import reactor.core.publisher.BaseSubscriber;

public class MultiRelaySubscriptionsManager {
  private final Map<String, SingleRelaySubscriptionsManager> relaysSubscriptionsMap = new HashMap<>();

  public void send(
      @NonNull ReqMessage reqMessage,
      @NonNull String url,
      @NonNull BaseSubscriber<BaseMessage> subscriber) throws JsonProcessingException, NostrException {
    relaysSubscriptionsMap.putIfAbsent(url, new SingleRelaySubscriptionsManager(url));
    for (SingleRelaySubscriptionsManager mgr : relaysSubscriptionsMap.values()) {
      mgr.send(reqMessage, subscriber);
    }
  }

  protected void removeRelay(@NonNull String url) {
    relaysSubscriptionsMap.get(url).closeAllSessions();
    relaysSubscriptionsMap.remove(url);
  }

  protected Set<String> getRelayNames() {
    return relaysSubscriptionsMap.keySet();
  }
}

