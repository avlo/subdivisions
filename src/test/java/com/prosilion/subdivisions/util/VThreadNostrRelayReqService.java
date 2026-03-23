//package com.prosilion.subdivisions.util;
//
//import com.prosilion.nostr.NostrException;
//import com.prosilion.nostr.message.BaseMessage;
//import com.prosilion.nostr.message.ReqMessage;
//import com.prosilion.subdivisions.client.virtualthread.VThreadRequestConsolidator;
//import java.util.List;
//import lombok.Generated;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.lang.NonNull;
//
//public class VThreadNostrRelayReqService {
//  @Generated
//  private static final Logger log = LoggerFactory.getLogger(VThreadNostrRelayReqService.class);
//  private final VThreadRequestConsolidator requestConsolidator;
//
//  public VThreadNostrRelayReqService() {
//    log.debug("constructor (using VThreadRequestConsolidator)");
//    this.requestConsolidator = new VThreadRequestConsolidator();
//  }
//
//  public List<BaseMessage> send(@NonNull ReqMessage reqMessage, @NonNull String relayUrl) throws NostrException {
//    return this.requestConsolidator.send(reqMessage, relayUrl);
//  }
//
//  public void disconnect(@NonNull String subscriptionId) {
//    this.requestConsolidator.removeRelay(subscriptionId);
//  }
//}
