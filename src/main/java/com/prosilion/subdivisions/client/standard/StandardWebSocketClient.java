package com.prosilion.subdivisions.client.standard;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import org.awaitility.Awaitility;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
public class StandardWebSocketClient extends TextWebSocketHandler {
  private final WebSocketSession clientSession;

  private final List<String> events = Collections.synchronizedList(new ArrayList<>());
  private final AtomicBoolean completed = new AtomicBoolean(false);

  public StandardWebSocketClient(@NonNull String relayUri) throws ExecutionException, InterruptedException {
    org.springframework.web.socket.client.standard.StandardWebSocketClient standardWebSocketClient = new org.springframework.web.socket.client.standard.StandardWebSocketClient();
    this.clientSession = getClientSession(relayUri, standardWebSocketClient);
    log.debug("Non-Secure (WS) WebSocket subdivisions connected {}", clientSession.getId());
  }

  public StandardWebSocketClient(@NonNull String relayUri, @NonNull SslBundles sslBundles) throws ExecutionException, InterruptedException {
    org.springframework.web.socket.client.standard.StandardWebSocketClient standardWebSocketClient = new org.springframework.web.socket.client.standard.StandardWebSocketClient();
    standardWebSocketClient.setSslContext(sslBundles.getBundle("server").createSslContext());
    this.clientSession = getClientSession(relayUri, standardWebSocketClient);
    log.debug("Secure (WSS) WebSocket subdivisions connected {}", clientSession.getId());
  }

  private WebSocketSession getClientSession(@NonNull String relayUri, org.springframework.web.socket.client.standard.StandardWebSocketClient standardWebSocketClient) throws ExecutionException, InterruptedException {
    return standardWebSocketClient
        .execute(
            this,
            new WebSocketHttpHeaders(),
            URI.create(relayUri))
        .get();
  }

  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, TextMessage message) {
    log.debug("handleTextMessage WebSocketSession id: \n{}", session.getId());
    events.add(message.getPayload());
    log.debug("handleTextMessage TextMessage events:");
    events.forEach(event -> log.debug("  {}", event));
    completed.setRelease(true);
  }

  public <T extends BaseMessage> void send(T eventMessage) throws IOException {
    send(eventMessage.encode());
  }

  @SneakyThrows
  public void send(String json) {
    clientSession.sendMessage(new TextMessage(json));
    Awaitility.await()
        .timeout(1, TimeUnit.MINUTES)
        .untilTrue(completed);
    completed.setRelease(false);
  }

  public List<String> getEvents() {
    List<String> eventList = List.copyOf(events);
    events.clear();
    return eventList;
  }

  public void closeSession() throws IOException {
    clientSession.close();
  }
}
