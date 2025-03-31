package com.prosilion.nostrclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
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
import org.apache.commons.lang3.stream.Streams;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;

@Slf4j
public class RelaySubscriptionsManager {
  private final Map<String, WebSocketClient> subscriberIdWebSocketClientMap = new ConcurrentHashMap<>();
  private final String relayUri;
  private SslBundles sslBundles;

  public RelaySubscriptionsManager(@NonNull String relayUri) {
    this.relayUri = relayUri;
    log.debug("relayUri: \n{}", relayUri);
  }

  public RelaySubscriptionsManager(@NonNull String relayUri, SslBundles sslBundles) {
    this.relayUri = relayUri;
    this.sslBundles = sslBundles;
    log.debug("sslBundles: \n{}", sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
  }

  public Map<Command, String> sendRequest(@NonNull String clientUuid, @NonNull String reqJson) {
    return sendNostrRequest(clientUuid, reqJson);
  }

  private Map<Command, String> sendNostrRequest(@NonNull String clientUuid, @NonNull String reqJson) {
    List<String> returnedEvents = request(clientUuid, reqJson);

    log.debug("55555555555555555");
    log.debug("after REQUEST:");
    log.debug("key:\n  [{}]\n", clientUuid);
    log.debug("-----------------");
    log.debug("returnedEvents:");
    log.debug(returnedEvents.stream().map(event -> String.format("  %s\n", event)).collect(Collectors.joining()));
    log.debug("55555555555555555");

    Map<Command, String> results = new HashMap<>();
    eose.apply(returnedEvents).ifPresent(events -> results.put(Command.EOSE, events));
    event.apply(returnedEvents).ifPresent(events -> results.put(Command.EVENT, events));

    return results;
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

  private final Function<List<String>, Optional<String>> eose = (events) ->
      getTypeSpecificMessage(EoseMessage.class, events).stream().map(EoseMessage::getSubscriptionId).findFirst();

  private final Function<List<String>, Optional<String>> event = (events) ->
      getTypeSpecificMessage(EventMessage.class, events).stream()
          .map(eventMessage -> (GenericEvent) eventMessage.getEvent())
          .sorted(Comparator.comparing(GenericEvent::getCreatedAt))
          .map(event -> new BaseEventEncoder<>(event).encode())
          .reduce((first, second) -> second);

  <V extends BaseMessage> List<V> getTypeSpecificMessage(Class<V> messageClass, List<String> messages) {
    return Streams.failableStream(messages.stream()
        .map(msg -> {
          try {
            return new BaseMessageDecoder<V>().decode(msg);
          } catch (JsonProcessingException e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .filter(messageClass::isInstance)).stream().toList();
  }
}
