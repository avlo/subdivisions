package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Objects;
import nostr.event.BaseMessage;
import nostr.event.json.codec.BaseMessageDecoder;
import reactor.core.publisher.Flux;

public interface MessageTypeFilterable {
  default <V extends BaseMessage> Flux<V> getTypeSpecificMessage(Class<V> messageClass, Flux<String> messages) {
    return messages.map(msg -> {
      try {
        return new BaseMessageDecoder<V>().decode(msg);
      } catch (JsonProcessingException e) {
        return null;
      }
    }).filter(Objects::nonNull).filter(messageClass::isInstance);
  }

  default <V extends BaseMessage> List<V> getTypeSpecificMessage(Class<V> messageClass, List<String> messages) {
    return getTypeSpecificMessage(messageClass, Flux.fromIterable(messages)).collectList().block();
  }
}
