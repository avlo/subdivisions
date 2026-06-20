package com.prosilion.subdivisions.client;

import com.prosilion.nostr.event.internal.Relay;
import lombok.NonNull;

public interface RequestSubscriberDelegateIF<T> {
  void doDelegate(@NonNull T t, @NonNull Relay relay);
  void dispose();
  Relay getRelay();
}
