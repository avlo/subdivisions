package com.prosilion.subdivisions.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.subdivisions.client.reactive.ReactiveWebSocketClient;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Function;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import nostr.event.BaseMessage;
import nostr.event.json.codec.BaseMessageDecoder;
import nostr.event.message.EventMessage;
import nostr.event.message.OkMessage;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class ReactiveEventPublisher {
  private final ReactiveWebSocketClient eventSocketClient;

  public ReactiveEventPublisher(@NonNull String relayUri) {
    log.debug("relayUri: \n{}", relayUri);
    this.eventSocketClient = new ReactiveWebSocketClient(relayUri);
  }

  public ReactiveEventPublisher(@NonNull String relayUri, SslBundles sslBundles) {
    log.debug("sslBundles: \n{}", sslBundles);
    final SslBundle server = sslBundles.getBundle("server");
    log.debug("sslBundles name: \n{}", server);
    log.debug("sslBundles key: \n{}", server.getKey());
    log.debug("sslBundles protocol: \n{}", server.getProtocol());
    this.eventSocketClient = new ReactiveWebSocketClient(relayUri);
  }

  public Flux<String> send(@NonNull EventMessage eventMessage) throws IOException {
    log.debug("socket send EventMessage content\n  {}", eventMessage.getEvent());
    return eventSocketClient.send(eventMessage);
  }

//  private OkMessage getOkMessage(List<String> received) {
//    return received.stream().map(ReactiveEventPublisher::getDecode).findFirst().orElseThrow();
//  }

  private final Function<Mono<String>, Mono<OkMessage>> eventOkMessageResponse = (mono) ->
      getTypeSpecificMessage(OkMessage.class, mono);

  private <V extends BaseMessage> Mono<V> getTypeSpecificMessage(Class<V> messageClass, Mono<String> messages) {
    return messages.map(msg -> {
          try {
            return new BaseMessageDecoder<V>().decode(msg);
          } catch (JsonProcessingException e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .filter(messageClass::isInstance);
  }
}
