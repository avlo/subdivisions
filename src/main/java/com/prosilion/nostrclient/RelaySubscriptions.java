package com.prosilion.nostrclient;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nostr.base.Command;
import nostr.event.BaseMessage;
import nostr.event.impl.GenericEvent;
import nostr.event.json.codec.BaseEventEncoder;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EoseMessage;
import nostr.event.message.EventMessage;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;

@Slf4j
public class RelaySubscriptions {
  private final Map<String, WebSocketClient> subscriberIdWebSocketClientMap = new ConcurrentHashMap<>();
  private final String relayUri;
  private SslBundles sslBundles;

  public RelaySubscriptions(@NonNull String relayUri) {
    this.relayUri = relayUri;
    log.debug("relayUri: \n{}", relayUri);
  }

  public RelaySubscriptions(@NonNull String relayUri, SslBundles sslBundles) {
    this.relayUri = relayUri;
    this.sslBundles = sslBundles;
    log.debug("sslBundles: \n{}", sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
  }

  public Map<Command, Optional<String>> sendRequest(@NonNull String clientUuid, @NonNull String reqJson) throws IOException, ExecutionException, InterruptedException {
    return sendNostrRequest(reqJson, clientUuid);
  }


  private Map<Command, Optional<String>> sendNostrRequest(@NonNull String clientUuid, @NonNull String reqJson) {
    List<String> returnedEvents = request(clientUuid, reqJson);

    log.debug("55555555555555555");
    log.debug("after REQUEST:");
    log.debug("key:\n  [{}]\n", clientUuid);
    log.debug("-----------------");
    log.debug("returnedEvents:");
    log.debug(returnedEvents.stream().map(event -> String.format("  %s\n", event)).collect(Collectors.joining()));
    log.debug("55555555555555555");

    return Map.of(Command.EOSE, eose.apply(returnedEvents), Command.EVENT, event.apply(returnedEvents));
  }

  @SneakyThrows
  private <T extends BaseMessage> T decode(Class<T> clazz, String baseMessage) {
    return new BaseMessageDecoder<T>().decode(baseMessage);
  }

  private List<String> request(@NonNull String clientUuid, @NonNull String reqJson) {
    return Optional.ofNullable(subscriberIdWebSocketClientMap.get(clientUuid))
        .orElseGet(() -> {
          subscriberIdWebSocketClientMap.put(clientUuid, getStandardWebSocketClient());
          subscriberIdWebSocketClientMap.get(clientUuid).send(reqJson);
          return subscriberIdWebSocketClientMap.get(clientUuid);
        }).getEvents();
  }

  //  TODO: cleanup sneaky
  @SneakyThrows
  private WebSocketClient getStandardWebSocketClient() {
    return Objects.nonNull(sslBundles) ? new WebSocketClient(relayUri, sslBundles) : new WebSocketClient(relayUri);
  }

  private final Function<List<String>, Optional<String>> eose = (events) -> events.stream()
      .map(msg ->
          decode(EoseMessage.class, msg)).findFirst().map(EoseMessage::getSubscriptionId);

  private final Function<List<String>, Optional<String>> event = (events) -> events.stream()
      .map(msg ->
          decode(EventMessage.class, msg))
      .map(eventMessage -> (GenericEvent) eventMessage.getEvent())
      .sorted(Comparator.comparing(GenericEvent::getCreatedAt))
      .map(event -> new BaseEventEncoder<>(event).encode())
      .reduce((first, second) -> second); // gets last/aka, most recently dated event
}
