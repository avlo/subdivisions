package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.NostrException;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.EventMessage;
import com.prosilion.nostr.message.OkMessage;
import com.prosilion.nostr.message.ReqMessage;
import com.prosilion.subdivisions.client.RequestSubscriber;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.lang.NonNull;

@Slf4j
public class NostrComprehensiveClient {
  private final NostrComprehensiveSubscriberClient client;
  Duration requestTimeoutDuration;

  public NostrComprehensiveClient(@NonNull String relayUrl, Duration requestTimeoutDuration) {
    log.debug("constructor called with relayUrl [{}]", relayUrl);
    this.requestTimeoutDuration = requestTimeoutDuration;
    this.client = new NostrComprehensiveSubscriberClient(relayUrl);
  }

  public NostrComprehensiveClient(@Value("${superconductor.relay.url}") @NonNull String relayUrl, @NonNull SslBundles sslBundles) throws ExecutionException, InterruptedException {
    log.debug("constructor called with relay url {} and sslBundles {}", relayUrl, sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
    this.client = new NostrComprehensiveSubscriberClient(relayUrl, sslBundles);
  }

  public OkMessage send(@NonNull EventMessage eventMessage) throws IOException {
    RequestSubscriber<OkMessage> subscriber = new RequestSubscriber<>(requestTimeoutDuration);
    client.send(eventMessage, subscriber);
    return subscriber.getItems().getFirst();
  }

  public List<BaseMessage> send(@NonNull ReqMessage reqMessage) throws JsonProcessingException, NostrException {
    RequestSubscriber<BaseMessage> subscriber = new RequestSubscriber<>(requestTimeoutDuration);
    client.send(reqMessage, subscriber);
    return subscriber.getItems();
  }

  public void disconnect() {
    client.closeSocket();
  }
}
