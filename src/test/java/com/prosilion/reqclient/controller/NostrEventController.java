package com.prosilion.reqclient.controller;

import com.prosilion.nostr.enums.Kind;
import com.prosilion.nostr.filter.Filters;
import com.prosilion.nostr.filter.event.KindFilter;
import com.prosilion.nostr.filter.tag.RelayTagFilter;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.EventMessage;
import com.prosilion.nostr.message.ReqMessage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.server.standard.TomcatRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Slf4j
@Controller
@EnableWebSocket
public class NostrEventController extends TextWebSocketHandler implements WebSocketConfigurer {
  private final Map<WebSocketSession, WebSocketSessionNostrRelayServiceSet> mapSessions = new HashMap<>();

  private void broadcast(String sessionId, TextMessage message) throws IOException {
    for (WebSocketSession webSocketSession : mapSessions.keySet().stream()
        .filter(session -> session.getId().equals(sessionId))
        .toList()) {
      webSocketSession.sendMessage(message);
    }
  }

  @GetMapping("/request-badge-award.html")
  public String requestTest() {
    return "thymeleaf/request-badge-award";
  }

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(this, "/")
        .setHandshakeHandler(
            new DefaultHandshakeHandler(
                new TomcatRequestUpgradeStrategy()));
  }

  @Override
  public void afterConnectionEstablished(@NotNull WebSocketSession session) {
    mapSessions.put(session, new WebSocketSessionNostrRelayServiceSet(session));
  }

  /**
   * WebSocket-Termination handler.  Differentiated from Nostr-Close if/when encountered in method
   * {@link #handleTextMessage(WebSocketSession, TextMessage) }
   */
  @Override
  public void afterConnectionClosed(WebSocketSession webSocketSession, @NotNull CloseStatus status) {
    log.debug("client initiated close, sessionId [{}]", webSocketSession.getId());
    mapSessions.get(webSocketSession).close();
    closeSession(webSocketSession);
  }

  /**
   * Nostr-Event/Req/Close Handler.
   * note: Nostr-Close is differentiated from WebSocket-Close if/when encountered in method
   * {@link #afterConnectionClosed(WebSocketSession, CloseStatus) }
   */
  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws IOException {
    log.debug("Message from session [{}]", session.getId());
    log.debug("Message content [{}]", textMessage.getPayload());
    ReqMessage reqMessage = ReqMessage.decode(session.getId(), textMessage.getPayload());

    String relay = reqMessage.getFiltersList().stream().map(filters ->
        filters.getFilterByType("relay").stream()
            .map(RelayTagFilter.class::cast)
            .map(RelayTagFilter::getFilterableValue)
            .toList()
            .getFirst()).toList().getFirst().getUrl();

    List<EventMessage> foundRelays = mapSessions.get(session).submitReq(relay, createReqMesssage(session.getId()));

    for (BaseMessage baseMessage : foundRelays) {
      broadcastMessageEvent(session.getId(), baseMessage);
    }
  }

  public void broadcastMessageEvent(String sessionId, BaseMessage message) throws IOException {
    TextMessage response = new TextMessage(message.encode());
    broadcast(sessionId, response);
    log.debug("NostrEventController broadcast to\nsession:\n\t{}\nmessage:\n\t{}", sessionId, response.getPayload());
  }

  private void closeSession(WebSocketSession session) {
    try {
      session.close();
    } catch (IOException e) {
      log.debug("Non-Subscriber session closed.");
    }
  }

  private ReqMessage createReqMesssage(String subscriberId) {
    return new ReqMessage(
        subscriberId,
        new Filters(
            new KindFilter(
                Kind.BADGE_AWARD_EVENT)));
  }
}
