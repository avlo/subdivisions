package com.prosilion.subdivisions.client.virtualthread;

import com.prosilion.nostr.codec.BaseMessageDecoder;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.ReqMessage;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.stream.Streams;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;

@Slf4j
public class VThreadRelaySubscriptionsManager {
  private final Map<String, VThreadWebSocketClient> subscriberIdWebSocketClientMap = new ConcurrentHashMap<>();
  private final String relayUri;
  private SslBundles sslBundles;

  public VThreadRelaySubscriptionsManager(@NonNull String relayUri) {
    this.relayUri = relayUri;
    log.debug("relayUri: \n{}", relayUri);
  }

  public VThreadRelaySubscriptionsManager(@NonNull String relayUri, @NonNull SslBundles sslBundles) {
    this.relayUri = relayUri;
    this.sslBundles = sslBundles;
    log.debug("sslBundles: \n{}", sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
  }

  public List<BaseMessage> send(@NonNull ReqMessage reqMessage) {
    log.debug("pre-encoded ReqMessage json: \n{}", reqMessage);
    return baseMessagesReturnedByReqMessage(
        getRequestResults(reqMessage));
  }

  private List<BaseMessage> baseMessagesReturnedByReqMessage(@NonNull List<String> reqMessage) {
    return Streams.failableStream(reqMessage.stream())
        .map(BaseMessageDecoder::decode).stream()
        .distinct().toList();
  }

  private List<String> getRequestResults(ReqMessage reqMessage) {
    String subscriberId = reqMessage.getSubscriptionId();
    subscriberIdWebSocketClientMap.putIfAbsent(subscriberId, getStandardWebSocketClient());
    return subscriberIdWebSocketClientMap.get(subscriberId).getPopulatedEvents();
  }

  //  TODO: cleanup sneaky
  @SneakyThrows
  private VThreadWebSocketClient getStandardWebSocketClient() {
    return Objects.nonNull(sslBundles) ?
        new VThreadWebSocketClient(relayUri, sslBundles) : new VThreadWebSocketClient(relayUri);
  }

  public void closeSession(@NonNull String... subscriberIds) {
    closeSessions(List.of(subscriberIds));
  }

  public void closeSessions(@NonNull List<String> subscriberIds) {
    subscriberIds.stream().map(subscriberIdWebSocketClientMap::get).forEach(this::closeSessions);
    subscriberIds.forEach(subscriberIdWebSocketClientMap::remove);
  }

  public void closeAllSessions() {
    closeSessions(subscriberIdWebSocketClientMap);
    subscriberIdWebSocketClientMap.clear();
  }

  private void closeSessions(Map<String, VThreadWebSocketClient> subscriberIdWebSocketClientMap) {
    closeSessions(subscriberIdWebSocketClientMap.values());
  }

  private void closeSessions(VThreadWebSocketClient... VThreadWebSocketClients) {
    closeSessions(List.of(VThreadWebSocketClients));
  }

  private void closeSessions(Collection<VThreadWebSocketClient> VThreadWebSocketClients) {
    Streams.failableStream(VThreadWebSocketClients.stream()).forEach(VThreadWebSocketClient::closeSession);
  }
}
