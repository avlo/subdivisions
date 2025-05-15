package com.prosilion.subdivisions.client.standard;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.subdivisions.client.reactive.MessageTypeFilterable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import nostr.event.message.ReqMessage;
import org.apache.commons.lang3.stream.Streams;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;

@Slf4j
public class StandardRelaySubscriptionsManager implements MessageTypeFilterable {
  private final Map<String, StandardWebSocketClient> subscriberIdWebSocketClientMap = new ConcurrentHashMap<>();
  private final String relayUri;
  private SslBundles sslBundles;

  public StandardRelaySubscriptionsManager(@NonNull String relayUri) {
    this.relayUri = relayUri;
    log.debug("relayUri: \n{}", relayUri);
  }

  public StandardRelaySubscriptionsManager(@NonNull String relayUri, @NonNull SslBundles sslBundles) {
    this.relayUri = relayUri;
    this.sslBundles = sslBundles;
    log.debug("sslBundles: \n{}", sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
  }

  public <T extends BaseMessage> List<T> send(@NonNull ReqMessage reqMessage) throws JsonProcessingException {
    log.debug("pre-encoded ReqMessage json: \n{}", reqMessage);
    return baseMessagesReturnedByReqMessage(
        getRequestResults(
            reqMessage.getSubscriptionId(),
            reqMessage.encode()));
  }

  private <T extends BaseMessage> List<T> baseMessagesReturnedByReqMessage(@NonNull List<String> reqMessage) {
    return this.getTypeSpecificMessage(reqMessage);
  }

  private List<String> getRequestResults(String subscriberId, String reqJson) {
    log.debug("subscriberId: [{}]", subscriberId);
    log.debug("reqJson: \n{}", reqJson);
    return Optional.ofNullable(subscriberIdWebSocketClientMap.get(subscriberId))
        .orElseGet(() -> {
          subscriberIdWebSocketClientMap.put(subscriberId, getStandardWebSocketClient());
          subscriberIdWebSocketClientMap.get(subscriberId).send(reqJson);
          return subscriberIdWebSocketClientMap.get(subscriberId);
        }).getEvents();
  }

  public <T extends BaseMessage> List<T> updateReqResults(@NonNull String subscriberId) {
    log.debug("RelaySubscriptionsManagerg updateReqResults for subscriberId: [{}]", subscriberId);
    return baseMessagesReturnedByReqMessage(getEvents(subscriberId));
  }

  private List<String> getEvents(@NonNull String subscriberId) {
    return Optional.ofNullable(subscriberIdWebSocketClientMap.get(subscriberId))
        .orElseThrow().getEvents();
  }

  //  TODO: cleanup sneaky
  @SneakyThrows
  private StandardWebSocketClient getStandardWebSocketClient() {
    return Objects.nonNull(sslBundles) ? new StandardWebSocketClient(relayUri, sslBundles) : new StandardWebSocketClient(relayUri);
  }

  public void closeSession(@NonNull String... subscriberIds) {
    closeSessions(List.of(subscriberIds));
  }

  public void closeSessions(@NonNull List<String> subscriberIds) {
    subscriberIds.forEach(id -> closeSessions(subscriberIdWebSocketClientMap.get(id)));
    subscriberIds.forEach(subscriberIdWebSocketClientMap::remove);
  }

  public void closeAllSessions() {
    closeSessions(subscriberIdWebSocketClientMap);
    subscriberIdWebSocketClientMap.clear();
  }

  private void closeSessions(Map<String, StandardWebSocketClient> subscriberIdWebSocketClientMap) {
    closeSessions(subscriberIdWebSocketClientMap.values());
  }

  private void closeSessions(StandardWebSocketClient... standardWebSocketClients) {
    closeSessions(List.of(standardWebSocketClients));
  }

  private void closeSessions(Collection<StandardWebSocketClient> standardWebSocketClients) {
    Streams.failableStream(standardWebSocketClients.stream()).forEach(StandardWebSocketClient::closeSession);
  }
}
