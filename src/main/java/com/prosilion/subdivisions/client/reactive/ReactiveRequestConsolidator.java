package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import nostr.event.BaseMessage;
import nostr.event.message.ReqMessage;
import org.reactivestreams.Subscriber;

public class ReactiveRequestConsolidator {
  private final Map<String, ReactiveRelaySubscriptionsManager> map;

  public ReactiveRequestConsolidator() {
    this(new HashMap<>());
  }

  public ReactiveRequestConsolidator(Map<String, String> relayNameUriMap) {
    this.map = relayNameUriMap.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            value ->
                new ReactiveRelaySubscriptionsManager(value.getValue())));
  }

  public void addRelay(String name, String uri) {
    this.map.put(name, new ReactiveRelaySubscriptionsManager(uri));
  }

  public void removeRelay(String name) {
    this.map.get(name).closeAllSessions();
    this.map.remove(name);
  }

  public <T extends ReqMessage, V extends BaseMessage> void send(@NonNull T reqMessage, @NonNull Subscriber<V> subscriber) throws JsonProcessingException {
    for (ReactiveRelaySubscriptionsManager mgr : map.values()) {
      mgr.send(reqMessage, subscriber);
    }
  }

  public <T extends ReqMessage, V extends BaseMessage> void sendRxR(@NonNull T reqMessage, @NonNull Subscriber<V> subscriber) throws JsonProcessingException {
    for (ReactiveRelaySubscriptionsManager mgr : map.values()) {
      mgr.send(reqMessage, subscriber);
    }
  }
}

