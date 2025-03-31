package com.prosilion.nostrclient;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RelaysAggregate {
  private final List<RelaySubscriptionsManager> relaySubscriptionManagers;

  @Autowired
  public RelaysAggregate(Map<String, String> relays) {
    this.relaySubscriptionManagers = relays.values().stream().map(RelaySubscriptionsManager::new).toList();
  }


}
//  Map<PubKey, Map<Event<rep-tag (NIP-XXX)>, Relay>
