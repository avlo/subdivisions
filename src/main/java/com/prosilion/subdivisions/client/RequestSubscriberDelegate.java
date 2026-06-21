package com.prosilion.subdivisions.client;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;

import static com.prosilion.subdivisions.client.RequestSubscriber.await;

@Slf4j
public class RequestSubscriberDelegate<T> extends BaseSubscriber<T> {
  public final static long DEFAULT_REQUEST_COUNT = Long.MAX_VALUE;

  private final RequestSubscriberDelegateIF<T> requestSubscriberDelegateIF;
  private final long requestCount;

  private Subscription subscription;

  public RequestSubscriberDelegate(@NonNull RequestSubscriberDelegateIF<T> requestSubscriberDelegateIF) {
    this(requestSubscriberDelegateIF, DEFAULT_REQUEST_COUNT);
  }

  public RequestSubscriberDelegate(@NonNull RequestSubscriberDelegateIF<T> requestSubscriberDelegateIF, long requestCount) {
    this.requestSubscriberDelegateIF = requestSubscriberDelegateIF;
    this.requestCount = requestCount;
  }

  public Subscription getSubscription() {
    await(requestSubscriberDelegateIF.getTimeout(), () ->
       subscription != null);
    return subscription;
  }

  @Override
  public void hookOnSubscribe(@NonNull Subscription subscription) {
    this.subscription = subscription;
    subscription.request(requestCount);
  }

  @Override
  public void hookOnNext(@NonNull T value) {
    requestSubscriberDelegateIF.doDelegate(value, subscription);
    subscription.request(requestCount);
  }

  public void dispose() {
    requestSubscriberDelegateIF.dispose();
    super.dispose();
  }
}
