package com.prosilion.subdivisions.client;

import com.prosilion.nostr.NostrException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.DurationFactory;
import org.reactivestreams.Subscription;
import org.springframework.lang.NonNull;
import reactor.core.publisher.BaseSubscriber;

@Slf4j
public class RequestSubscriber<T> extends BaseSubscriber<T> {
  private final List<T> items = Collections.synchronizedList(new ArrayList<>());
  private final AtomicBoolean areItemsPopulated = new AtomicBoolean(true);
  private final Duration timeout;
  private Subscription subscription;

  public RequestSubscriber() {
    this(DurationFactory.of(3, TimeUnit.SECONDS));
  }

  public RequestSubscriber(Duration timeout) {
    this.timeout = timeout;
  }

  @Override
  public void hookOnSubscribe(@NonNull Subscription subscription) {
    this.subscription = subscription;
    areItemsPopulated.set(false);
    subscription.request(1);
  }

  @Override
  public void hookOnNext(@NonNull T value) {
    items.add(value);
    areItemsPopulated.set(true);
    subscription.request(1);
  }

  public List<T> getItems() {
    await(timeout, areItemsPopulated::get);
    List<T> eventList = List.copyOf(items);
    items.clear();
    areItemsPopulated.set(false);
    return eventList;
  }

  public static void await(Duration timeout, BooleanSupplier conditionSupplier) {
    long timeoutNs = timeout.toNanos();
    long startTime = System.nanoTime();
    do {
      if (conditionSupplier.getAsBoolean()) {
        return;
      }
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
    }
    while (System.nanoTime() - startTime < timeoutNs);
    throw new NostrException("TestSubscriber timed out");
  }
}
