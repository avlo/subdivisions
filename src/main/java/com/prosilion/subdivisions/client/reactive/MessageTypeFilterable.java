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
  default <T extends BaseMessage> Flux<T> getTypeSpecificMessage(Flux<String> messages) {
    log.debug("MessageTypeFilterable.getTypeSpecificMessage()");
    return messages
        .map(msg -> {
          try {
            return new BaseMessageDecoder<T>().decode(msg);
          } catch (JsonProcessingException e) {
            return null;
          }
        })
        .filter(Objects::nonNull)
//        .filter(messageClass::isInstance)
        ;
  }

  default <T extends BaseMessage> List<T> getTypeSpecificMessage(List<String> messages) {
    Flux<T> typeSpecificMessage = getTypeSpecificMessage(Flux.fromIterable(messages));
    return typeSpecificMessage.collectList().block();
  }
}
