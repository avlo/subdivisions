package com.prosilion.reqclient.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.EventMessage;
import com.prosilion.nostr.message.ReqMessage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketSessionNostrRelayServiceSet {
  private final WebSocketSession webSocketSession;
  private final Map<String, NostrRelayServiceRedis> nostrRelayServiceMap = new HashMap<>();

  public WebSocketSessionNostrRelayServiceSet(WebSocketSession webSocketSession) {
    this.webSocketSession = webSocketSession;
  }

  public void close() {
    nostrRelayServiceMap.values().forEach(NostrRelayServiceRedis::close);
  }

  public List<EventMessage> submitReq(
      @NonNull String relayUrl,
      @NonNull ReqMessage reqMessage) throws JsonProcessingException {
    this.nostrRelayServiceMap.putIfAbsent(relayUrl, new NostrRelayServiceRedis(relayUrl));
    NostrRelayServiceRedis nostrRelayServiceRedis = nostrRelayServiceMap.get(relayUrl);
    List<EventMessage> send = nostrRelayServiceRedis.send(reqMessage);
    return send;
  }
}
