package com.prosilion.subdivisions.client.virtualthread;

import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.ReqMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.stream.Streams;
import org.springframework.lang.NonNull;

public class VThreadRequestConsolidator {
  private final Map<String, VThreadRelaySubscriptionsManager> map = new HashMap<>();

  public List<BaseMessage> send(@NonNull ReqMessage reqMessage, @NonNull String url) {
    addRelay(url);
    return Streams.failableStream(map.values().stream()).map(mgr ->
            mgr.send(reqMessage)).stream()
        .distinct()
        .flatMap(List::stream).toList();
  }

  public void addRelay(String url) {
    map.putIfAbsent(url, new VThreadRelaySubscriptionsManager(url));
  }

  public void removeRelay(@NonNull String url) {
    map.get(url).closeAllSessions();
    map.remove(url);
  }
}
