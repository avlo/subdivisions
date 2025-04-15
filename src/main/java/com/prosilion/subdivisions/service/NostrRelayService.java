package com.prosilion.subdivisions.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.subdivisions.event.EventPublisher;
import com.prosilion.subdivisions.request.RelaySubscriptionsManager;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.base.Command;
import nostr.event.impl.GenericEvent;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import nostr.event.message.ReqMessage;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;

@Slf4j
public class NostrRelayService {
  private final EventPublisher eventPublisher;
  private final RelaySubscriptionsManager relaySubscriptionsManager;

  public NostrRelayService(@NonNull String relayUri) throws ExecutionException, InterruptedException {
    log.debug("relayUri: \n{}", relayUri);
    this.eventPublisher = new EventPublisher(relayUri);
    this.relaySubscriptionsManager = new RelaySubscriptionsManager(relayUri);
  }

  public NostrRelayService(@NonNull String relayUri, SslBundles sslBundles
  ) throws ExecutionException, InterruptedException {
    log.debug("relayUri: \n{}", relayUri);
    log.debug("sslBundles: \n{}", sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
    this.eventPublisher = new EventPublisher(relayUri, sslBundles);
    this.relaySubscriptionsManager = new RelaySubscriptionsManager(relayUri, sslBundles);
  }

  public OkMessage sendEvent(@NonNull String eventJson) throws IOException {
    return eventPublisher.sendEvent(eventJson);
  }

  public OkMessage sendEvent(@NonNull EventMessage eventMessage) throws IOException {
    return eventPublisher.sendEvent(eventMessage);
  }

  public List<GenericEvent> sendRequestReturnEvents(@NonNull ReqMessage reqMessage) throws JsonProcessingException {
    return relaySubscriptionsManager.sendRequestReturnEvents(reqMessage);
  }

  public Map<Command, List<Object>> sendRequestReturnCommandResultsMap(@NonNull String subscriberId, @NonNull String reqJson) {
    return relaySubscriptionsManager.sendRequestReturnCommandResultsMap(subscriberId, reqJson);
  }
}
