package com.prosilion.subdivisions.client;

import org.springframework.lang.NonNull;

public interface RequestSubscriberDelegateIF<T> {
  void doDelegate(@NonNull T t);
  void dispose();
}
