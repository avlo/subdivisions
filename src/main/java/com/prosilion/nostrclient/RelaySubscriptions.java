package com.prosilion.nostrclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nostr.base.Command;
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

  private Map<Command, Optional<String>> sendNostrRequest(
      @NonNull String clientUuid,
      @NonNull String reqJson) throws IOException, ExecutionException, InterruptedException {
    List<String> returnedEvents = request(clientUuid, reqJson);

    log.debug("55555555555555555");
    log.debug("after REQUEST:");
    log.debug("key:\n  [{}]\n", clientUuid);
    log.debug("-----------------");
    log.debug("returnedEvents:");
    log.debug(returnedEvents.stream().map(event -> String.format("  %s\n", event)).collect(Collectors.joining()));
    log.debug("55555555555555555");

    Optional<String> eoseMessageOptional = returnedEvents.stream().map(baseMessage -> {
          try {
            return new BaseMessageDecoder<>().decode(baseMessage);
          } catch (JsonProcessingException e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .filter(EoseMessage.class::isInstance)
        .map(EoseMessage.class::cast)
        .findFirst()
        .map(EoseMessage::getSubscriptionId);

    Optional<String> eventMessageOptional = returnedEvents.stream().map(baseMessage -> {
          try {
            return new BaseMessageDecoder<>().decode(baseMessage);
          } catch (JsonProcessingException e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .filter(EventMessage.class::isInstance)
        .map(EventMessage.class::cast)
        .map(eventMessage -> (GenericEvent) eventMessage.getEvent())
        .sorted(Comparator.comparing(GenericEvent::getCreatedAt))
        .map(event -> new BaseEventEncoder<>(event).encode())
        .reduce((first, second) -> second); // gets last/aka, most recently dated event

    Map<Command, Optional<String>> returnMap = new HashMap<>();
    returnMap.put(Command.EOSE, eoseMessageOptional);
    returnMap.put(Command.EVENT, eventMessageOptional);
    return returnMap;
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
    return Objects.nonNull(sslBundles) ? new WebSocketClient(relayUri, sslBundles) :
        new WebSocketClient(relayUri);
  }
}
