package com.prosilion.subdivisions.request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import nostr.base.Command;
import nostr.event.message.ReqMessage;
import org.apache.commons.lang3.stream.Streams;

public class RequestConsolidator {
  private final Map<String, RelaySubscriptionsManager> map;

  public RequestConsolidator() {
    this(new HashMap<>());
  }

  public RequestConsolidator(Map<String, String> relayNameUriMap) {
    this.map = relayNameUriMap.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            value ->
                new RelaySubscriptionsManager(value.getValue())));
  }

  public void addRelay(String name, String uri) {
    this.map.put(name, new RelaySubscriptionsManager(uri));
  }

  public void removeRelay(String name) {
    this.map.get(name).closeAllSessions();
    this.map.remove(name);
  }

  //  List<PubKey> sendReq(ReqFilter(NIP-XXX))
//  List<Event> eventsMatchingNipXXX = relay.sendReq(ReqFilter(NIP-XXX))
//		return eventsMatchingNipXXX.getPubKeys()


  public List<Map<Command, List<String>>> sendRequest(@NonNull ReqMessage reqMessage) {
    return Streams.failableStream(map.values().stream()).map(entry ->
        entry.sendRequestReturnCommandResultsMap(reqMessage)).stream().toList();
  }
}
//  Map<PubKey, Map<Event<rep-tag (NIP-XXX)>, Relay>
