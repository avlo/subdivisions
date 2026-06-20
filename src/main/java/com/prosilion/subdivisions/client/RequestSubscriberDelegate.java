package com.prosilion.subdivisions.client;

import com.prosilion.nostr.event.internal.Relay;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import lombok.NonNull;
import reactor.core.publisher.BaseSubscriber;

@Getter
@Slf4j
public class RequestSubscriberDelegate<T> extends BaseSubscriber<T> {
  public final static long DEFAULT_REQUEST_COUNT = Long.MAX_VALUE;

  private final RequestSubscriberDelegateIF<T> requestSubscriberDelegateIF;
  private final long requestCount;
  private final Relay relay;
  
  private Subscription subscription;

  public RequestSubscriberDelegate(@NonNull RequestSubscriberDelegateIF<T> requestSubscriberDelegateIF) {
    this(requestSubscriberDelegateIF, DEFAULT_REQUEST_COUNT);
  }

  public RequestSubscriberDelegate(@NonNull RequestSubscriberDelegateIF<T> requestSubscriberDelegateIF, long requestCount) {
    this.requestSubscriberDelegateIF = requestSubscriberDelegateIF;
    this.requestCount = requestCount;
    this.relay = requestSubscriberDelegateIF.getRelay();
  }

  @Override
  public void hookOnSubscribe(@NonNull Subscription subscription) {
    this.subscription = subscription;
    subscription.request(requestCount);
  }

  @Override
  public void hookOnNext(@NonNull T value) {
    requestSubscriberDelegateIF.doDelegate(value, this.relay);
    subscription.request(requestCount);
  }

  public void dispose() {
    requestSubscriberDelegateIF.dispose();
    super.dispose();
  }
}
