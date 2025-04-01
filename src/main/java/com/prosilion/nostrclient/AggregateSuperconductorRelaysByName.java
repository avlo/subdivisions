package com.prosilion.nostrclient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.NonNull;
import nostr.base.Command;
import nostr.event.message.ReqMessage;
import org.apache.commons.lang3.stream.Streams;

public class AggregateSuperconductorRelaysByName {
  private final Map<String, SubscriberIdsPerSuperconductorRelay> map;

  public AggregateSuperconductorRelaysByName() {
    this(new HashMap<>());
  }

  public AggregateSuperconductorRelaysByName(Map<String, String> relayNameUriMap) {
    this.map = relayNameUriMap.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            value ->
                new SubscriberIdsPerSuperconductorRelay(value.getValue())));
  }

  public void addRelay(String name, String uri) {
    this.map.put(name, new SubscriberIdsPerSuperconductorRelay(uri));
  }

  public void removeRelay(String name) {
    this.map.get(name).closeAllSessions();
    this.map.remove(name);
  }

  //  List<PubKey> sendReq(ReqFilter(NIP-XXX))
//  List<Event> eventsMatchingNipXXX = relay.sendReq(ReqFilter(NIP-XXX))
//		return eventsMatchingNipXXX.getPubKeys()


  public List<Map<Command, String>> sendRequest(@NonNull ReqMessage reqMessage) {
    return Streams.failableStream(map.values().stream()).map(entry ->
        entry.sendRequest(reqMessage)).stream().toList();
  }
}
//  Map<PubKey, Map<Event<rep-tag (NIP-XXX)>, Relay>
