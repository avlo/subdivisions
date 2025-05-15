package com.prosilion.subdivisions.client.standard;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import nostr.event.message.ReqMessage;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;

@Slf4j
public class StandardNostrRelayClient {
  private final StandardEventPublisher standardEventPublisher;
  private final StandardRelaySubscriptionsManager standardRelaySubscriptionsManager;

  public StandardNostrRelayClient(@NonNull String relayUri) throws ExecutionException, InterruptedException {
    log.debug("relayUri: \n{}", relayUri);
    this.standardEventPublisher = new StandardEventPublisher(relayUri);
    this.standardRelaySubscriptionsManager = new StandardRelaySubscriptionsManager(relayUri);
  }

  public StandardNostrRelayClient(@NonNull String relayUri, SslBundles sslBundles
  ) throws ExecutionException, InterruptedException {
    log.debug("relayUri: \n{}", relayUri);
    log.debug("sslBundles: \n{}", sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
    this.standardEventPublisher = new StandardEventPublisher(relayUri, sslBundles);
    this.standardRelaySubscriptionsManager = new StandardRelaySubscriptionsManager(relayUri, sslBundles);
  }

  public OkMessage sendEvent(@NonNull EventMessage eventMessage) throws IOException {
    return standardEventPublisher.sendEvent(eventMessage);
  }

  public <T extends BaseMessage> List<T> send(@NonNull ReqMessage reqMessage) throws JsonProcessingException {
    return standardRelaySubscriptionsManager.send(reqMessage);
  }
}
