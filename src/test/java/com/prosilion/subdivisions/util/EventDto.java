package com.prosilion.subdivisions.util;

import com.prosilion.nostr.event.BaseEvent;
import com.prosilion.nostr.event.GenericEventKind;
import com.prosilion.nostr.event.GenericEventKindIF;
import com.prosilion.nostr.user.Signature;

public record EventDto(BaseEvent event) {

  public GenericEventKindIF convertBaseEventToDto() {
    return new GenericEventKind(
        event.getId(),
        event.getPublicKey(),
        event.getCreatedAt(),
        event.getKind(),
        event.getTags(),
        event.getContent(),
        Signature.fromString(event.getSignature()));
  }
}
