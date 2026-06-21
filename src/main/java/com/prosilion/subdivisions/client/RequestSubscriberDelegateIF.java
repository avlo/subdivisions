package com.prosilion.subdivisions.client;

import java.time.Duration;
import lombok.NonNull;
import org.reactivestreams.Subscription;

public interface RequestSubscriberDelegateIF<T> {
  void doDelegate(@NonNull T t, @NonNull Subscription subscription);
  void dispose();
  Duration getTimeout();
}
