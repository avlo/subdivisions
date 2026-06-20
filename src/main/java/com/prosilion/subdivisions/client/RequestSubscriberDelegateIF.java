package com.prosilion.subdivisions.client;

import lombok.NonNull;

public interface RequestSubscriberDelegateIF<T> {
  void doDelegate(@NonNull T t);
  void dispose();
}
