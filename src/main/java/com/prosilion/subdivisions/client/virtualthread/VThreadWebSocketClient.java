package com.prosilion.subdivisions.client.virtualthread;

import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.util.Util;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
class VThreadWebSocketClient extends TextWebSocketHandler {
  private final WebSocketSession clientSession;
  
  @Getter
  private final List<String> events = Collections.synchronizedList(new ArrayList<>());

  protected VThreadWebSocketClient(@NonNull String relayUri) throws ExecutionException, InterruptedException {
    StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();
    this.clientSession = getClientSession(relayUri, standardWebSocketClient);
    log.debug("Non-Secure (WS) WebSocket subdivisions connected {}", clientSession.getId());
  }

  protected VThreadWebSocketClient(@NonNull String relayUri, @NonNull SslBundles sslBundles) throws ExecutionException, InterruptedException {
    StandardWebSocketClient standardWebSocketClient = new StandardWebSocketClient();
    standardWebSocketClient.setSslContext(sslBundles.getBundle("server").createSslContext());
    this.clientSession = getClientSession(relayUri, standardWebSocketClient);
    log.debug("Secure (WSS) WebSocket subdivisions connected {}", clientSession.getId());
  }

  protected <T extends BaseMessage> void send(T message) throws IOException {
    clientSession.sendMessage(
        new TextMessage(
            message.encode()));
  }

  protected List<String> getPopulatedEvents() {
    List<String> eventList = List.copyOf(events);
    events.clear();
    return eventList;
  }

  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage textMessage) {
    log.debug("handleTextMessage WebSocketSession id:\n [{}]", session.getId());
    events.add(textMessage.getPayload());
    log.debug("handleTextMessage TextMessage events:");
    events.forEach(event -> log.debug("  {}", Util.prettyFormatJson(event)));
  }

  private WebSocketSession getClientSession(String relayUri, StandardWebSocketClient standardWebSocketClient) throws ExecutionException, InterruptedException {
    CompletableFuture<WebSocketSession> execute = standardWebSocketClient.execute(
        this,
        new WebSocketHttpHeaders(),
        URI.create(relayUri));

    WebSocketSession webSocketSession = execute.get();
    
    return webSocketSession;
  }

  protected void closeSession() throws IOException {
    clientSession.close();
  }
}
