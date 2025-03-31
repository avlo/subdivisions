//package com.prosilion.nostrclient;
//
//import java.util.List;
//import java.util.Map;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//public class RelaysAggregate {
//  private final List<RelaySubscriptions> relaySubscriptions;
//
//  @Autowired
//  public RelaysAggregate(Map<String, String> relays) {
//    this.relaySubscriptions = relays.values().stream().map(RelaySubscriptions::new).toList();
//  }
//
//
//}
////  Map<PubKey, Map<Event<rep-tag (NIP-XXX)>, Relay>
