package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.NostrException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.ReqMessage;
import jakarta.validation.constraints.NotEmpty;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.reactivestreams.Subscriber;

public class ReactiveRequestConsolidator {
  private final Map<String, ReactiveRelaySubscriptionsManager> map;

  public ReactiveRequestConsolidator() {
    this(new HashMap<>());
  }

  public ReactiveRequestConsolidator(@NotEmpty Map<String, String> relayNameUriMap) {
    this.map = relayNameUriMap.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            value ->
                new ReactiveRelaySubscriptionsManager(value.getValue())));
  }

  public void addRelay(@NonNull String name, @NonNull String uri) {
    this.map.putIfAbsent(name, new ReactiveRelaySubscriptionsManager(uri));
  }

  public void removeRelay(@NonNull String name) {
    this.map.get(name).closeAllSessions();
    this.map.remove(name);
  }

  public <T extends ReqMessage, V extends BaseMessage> void send(@NonNull T reqMessage, @NonNull Subscriber<V> subscriber) throws JsonProcessingException, NostrException {
    for (ReactiveRelaySubscriptionsManager mgr : map.values()) {
      mgr.send(reqMessage, subscriber);
    }
  }

  public Set<String> getRelayNames() {
    return map.keySet();
  }
}

