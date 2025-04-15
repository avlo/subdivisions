package com.prosilion.subdivisions.event;

import com.prosilion.subdivisions.WebSocketClient;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
public class EventPublisher {
  private final WebSocketClient eventSocketClient;

  public EventPublisher(@NonNull String relayUri) throws ExecutionException, InterruptedException {
    log.debug("relayUri: \n{}", relayUri);
    this.eventSocketClient = new WebSocketClient(relayUri);
  }

  public EventPublisher(@NonNull String relayUri, SslBundles sslBundles) throws ExecutionException, InterruptedException {
    log.debug("sslBundles: \n{}", sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
    this.eventSocketClient = new WebSocketClient(relayUri, sslBundles);
  }

  public OkMessage sendEvent(@NonNull String eventJson) throws IOException {
    eventSocketClient.send(eventJson);
    log.debug("socket send event JSON content\n  {}", eventJson);
    return getOkMessage(getEvents());
  }

  public OkMessage sendEvent(@NonNull EventMessage eventMessage) throws IOException {
    eventSocketClient.send(eventMessage);
    log.debug("socket send EventMessage content\n  {}", eventMessage.getEvent());
    return getOkMessage(getEvents());
  }

  private OkMessage getOkMessage(List<String> received) {
    return received.stream().map(EventPublisher::getDecode).findFirst().orElseThrow();
  }

  private List<String> getEvents() {
    List<String> events = eventSocketClient.getEvents();
    log.debug("received relay response:");
    log.debug("\n" + events.stream().map(event -> String.format("  %s\n", event)).collect(Collectors.joining()));
    return events;
  }

  @SneakyThrows
  private static OkMessage getDecode(String baseMessage) {
    return new BaseMessageDecoder<OkMessage>().decode(baseMessage);
  }
}
