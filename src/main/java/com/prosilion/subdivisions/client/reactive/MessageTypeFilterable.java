package com.prosilion.subdivisions.client.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Objects;
import nostr.event.BaseMessage;
import nostr.event.json.codec.BaseMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

public interface MessageTypeFilterable {
  Logger log = LoggerFactory.getLogger(MessageTypeFilterable.class);  // slf4j in interfaces req's explicit

  //  TODO: method needs thorough testing, doesn't properly filter Class<V> types
  default <V extends BaseMessage> Flux<V> getTypeSpecificMessage(Flux<String> messages) {
    log.debug("MessageTypeFilterable.getTypeSpecificMessage()");
    return messages
        .map(msg -> {
          try {
            return new BaseMessageDecoder<V>().decode(msg);
          } catch (JsonProcessingException e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
//        .filter(messageClass::isInstance)
        ;
  }

  default <V extends BaseMessage> List<V> getTypeSpecificMessage(List<String> messages) {
    Flux<V> typeSpecificMessage = getTypeSpecificMessage(Flux.fromIterable(messages));
    return typeSpecificMessage.collectList().block();
  }
}
