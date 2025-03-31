package com.prosilion.nostrclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Streams;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.api.factory.impl.NIP01Impl;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;

@Slf4j
public class RelayEventClient {
  private final WebSocketClient eventSocketClient;
  private final String relayUri;
  private SslBundles sslBundles;

  public RelayEventClient(@NonNull String relayUri) throws ExecutionException, InterruptedException {
    this.relayUri = relayUri;
    log.debug("relayUri: \n{}", relayUri);
    this.eventSocketClient = new WebSocketClient(relayUri);

  }

  public RelayEventClient(@NonNull String relayUri, SslBundles sslBundles) throws ExecutionException, InterruptedException {
    this.relayUri = relayUri;
    this.sslBundles = sslBundles;
    log.debug("sslBundles: \n{}", sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
    this.eventSocketClient = new WebSocketClient(relayUri, sslBundles);
  }

  public OkMessage createEvent(@NonNull String eventJson) throws IOException {
    return getOkMessage(
        sendEvent(eventJson));
  }

  public OkMessage createEvent(@NonNull GenericEvent event) throws IOException {
    return getOkMessage(
        sendEvent(
            new NIP01Impl.EventMessageFactory(event).create()));
  }

  private static OkMessage getOkMessage(List<String> received) {
    return Streams.findLast(received.stream())
        .map(baseMessage -> {
          try {
            return new BaseMessageDecoder<OkMessage>().decode(baseMessage);
          } catch (JsonProcessingException e) {
            return null;
          }
        })
        .orElseThrow();
  }

  private List<String> sendEvent(String eventJson) throws IOException {
    eventSocketClient.send(eventJson);
    log.debug("socket send event JSON content\n  {}", eventJson);
    return getEvents();
  }

  private List<String> sendEvent(EventMessage eventMessage) throws IOException {
    eventSocketClient.send(eventMessage);
    log.debug("socket send EventMessage content\n  {}", eventMessage.getEvent());
    return getEvents();
  }

  public List<String> getEvents() {
    List<String> events = eventSocketClient.getEvents();
    log.debug("received relay response:");
    log.debug("\n" + events.stream().map(event -> String.format("  %s\n", event)).collect(Collectors.joining()));
    return events;
  }

  private WebSocketClient getStandardWebSocketClient() throws ExecutionException, InterruptedException {
    return Objects.nonNull(sslBundles) ? new WebSocketClient(relayUri, sslBundles) :
        new WebSocketClient(relayUri);
  }
}
