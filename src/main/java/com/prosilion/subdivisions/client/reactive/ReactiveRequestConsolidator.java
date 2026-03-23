package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.NostrException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.ReqMessage;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;
import org.reactivestreams.Subscriber;

public class ReactiveRequestConsolidator {
  private final Map<String, ReactiveSubscriptionsManager> map = new HashMap<>();

  public <T extends ReqMessage, V extends BaseMessage> void send(
      @NonNull T reqMessage,
      @NonNull Subscriber<V> subscriber,
      @NonNull String url) throws JsonProcessingException, NostrException {
    map.putIfAbsent(url, new ReactiveSubscriptionsManager(url));
    map.get(url).send(reqMessage, subscriber);
//    for (ReactiveSubscriptionsManager mgr : map.values()) {
//      mgr.send(reqMessage, subscriber);
//    }
  }

  protected void removeRelay(@NonNull String url) {
    map.get(url).closeAllSessions();
    map.remove(url);
  }

  protected Set<String> getRelayNames() {
    return map.keySet();
  }
}

