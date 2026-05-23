package com.prosilion.subdivisions.client;

import com.prosilion.nostr.NostrException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import org.springframework.lang.NonNull;
import reactor.core.publisher.BaseSubscriber;

@Getter
@Slf4j
public class RequestSubscriber<T> extends BaseSubscriber<T> {
  public final static Duration DEFAULT_TIMEOUT_3000_MS = Duration.of(3000, ChronoUnit.MILLIS);
  public final static long DEFAULT_REQUEST_COUNT = Long.MAX_VALUE;
  private final AtomicBoolean areItemsPopulated = new AtomicBoolean(false);
  private final List<T> items = Collections.synchronizedList(new ArrayList<>());

  private final Duration timeout;
  private Subscription subscription;
  private final long requestCount;

  public RequestSubscriber() {
    this(DEFAULT_TIMEOUT_3000_MS);
  }

  public RequestSubscriber(long requestCount) {
    this(DEFAULT_TIMEOUT_3000_MS, requestCount);
  }

  public RequestSubscriber(@NonNull Duration timeout) {
    this(timeout, DEFAULT_REQUEST_COUNT);
  }

  public RequestSubscriber(@NonNull Duration timeout, long requestCount) {
    this.timeout = timeout;
    this.requestCount = requestCount;
  }

  @Override
  public void hookOnSubscribe(@NonNull Subscription subscription) {
    this.subscription = subscription;
    areItemsPopulated.set(false);
    subscription.request(requestCount);
  }

  @Override
  public void hookOnNext(@NonNull T value) {
    items.add(value);
    areItemsPopulated.set(true);
    subscription.request(requestCount);
  }

  public List<T> getItems() {
    await(timeout, areItemsPopulated::get);
    List<T> eventList = List.copyOf(items);
    items.clear();
    areItemsPopulated.set(false);
    return eventList;
  }

  @Override
  protected void hookOnComplete() {
    log.debug("... hookOnComplete() ...");
    areItemsPopulated.set(true);
    subscription.cancel();
  }

  public static void await(@NonNull Duration timeout, @NonNull BooleanSupplier conditionSupplier) {
    long timeoutNs = timeout.toNanos();
    long startTime = System.nanoTime();
    int sleepDebugCount = 0;
    do {
      if (conditionSupplier.getAsBoolean()) {
        log.debug("await sleep completed successfully at count#: [{}]", sleepDebugCount);
        return;
      }
      try {
        Thread.sleep(100);
        log.debug("await sleep count#: [{}]", sleepDebugCount++);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
    }
    while (System.nanoTime() - startTime < timeoutNs);
    log.debug("await sleep completed w/ exception at count#: [{}]", sleepDebugCount);
    throw new NostrException(
        String.format("RequestSubscriber duration [%dmillis == %dmin, %dsec] timed out",
            timeout.toMillis(),
            timeout.toMinutesPart(),
            timeout.toSecondsPart()));
  }
}
