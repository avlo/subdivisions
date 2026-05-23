package com.prosilion.subdivisions.client;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.lang.NonNull;
import reactor.core.publisher.BaseSubscriber;

@Getter
@Slf4j
public class RequestSubscriberDelegate<T> extends BaseSubscriber<T> {
  public final static long DEFAULT_REQUEST_COUNT = Long.MAX_VALUE;

  private Subscription subscription;
  private final long requestCount;
  private final RequestSubscriberDelegateIF<T> requestSubscriberDelegateIF;

  public RequestSubscriberDelegate(@NonNull RequestSubscriberDelegateIF<T> requestSubscriberDelegateIF) {
    this(requestSubscriberDelegateIF, DEFAULT_REQUEST_COUNT);
  }

  public RequestSubscriberDelegate(@NonNull RequestSubscriberDelegateIF<T> requestSubscriberDelegateIF, long requestCount) {
    this.requestSubscriberDelegateIF = requestSubscriberDelegateIF;
    this.requestCount = requestCount;
  }

  @Override
  public void hookOnSubscribe(@NonNull Subscription subscription) {
    this.subscription = subscription;
    subscription.request(requestCount);
  }

  @Override
  public void hookOnNext(@NonNull T value) {
    requestSubscriberDelegateIF.doDelegate(value);
    subscription.request(requestCount);
  }

  public void dispose() {
    requestSubscriberDelegateIF.dispose();
    super.dispose();
  }
}
