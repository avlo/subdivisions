package com.prosilion.subdivisions.client.standard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import nostr.event.BaseMessage;
import nostr.event.message.ReqMessage;
import org.apache.commons.lang3.stream.Streams;

public class StandardRequestConsolidator {
  private final Map<String, StandardRelaySubscriptionsManager> map;

  public StandardRequestConsolidator() {
    this(new HashMap<>());
  }

  public StandardRequestConsolidator(Map<String, String> relayNameUriMap) {
    this.map = relayNameUriMap.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            value ->
                new StandardRelaySubscriptionsManager(value.getValue())));
  }

  public void addRelay(String name, String uri) {
    this.map.put(name, new StandardRelaySubscriptionsManager(uri));
  }

  public void removeRelay(String name) {
    this.map.get(name).closeAllSessions();
    this.map.remove(name);
  }

  public List<BaseMessage> sendRequestReturnEvents(@NonNull ReqMessage reqMessage) {
    return Streams.failableStream(map.values().stream()).map(entry ->
            entry.send(reqMessage)).stream()
        .distinct()
        .flatMap(List::stream).toList();
  }
}
