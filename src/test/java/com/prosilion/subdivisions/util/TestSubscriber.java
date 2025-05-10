package com.prosilion.subdivisions.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;

@Slf4j
public class TestSubscriber<T> extends BaseSubscriber<T> {
  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_BLUE = "\033[1;34m";
  public static final String ANSI_RED = "\033[1;36m";

  private List<T> items;
  private AtomicBoolean completed;

  @Override
  public void hookOnSubscribe(@NonNull Subscription subscription) {
    this.items = Collections.synchronizedList(new ArrayList<>());
    this.completed = new AtomicBoolean(false);

    requestUnbounded();

    log.debug("0000000000000000000000");
    log.debug(" Subscription object hashCode: [ " + ANSI_BLUE + subscription.hashCode() + ANSI_RESET + " ]");
    log.debug("0000000000000000000000");
  }

  @Override
  public void hookOnNext(@NonNull T value) {
    requestUnbounded();

    log.debug("111111111111111111111");
    String subscriberId = value.toString().split(",")[1];
    String strippedStart = StringUtils.stripStart(subscriberId, "\"");
    log.debug(" On Next: " + ANSI_RED + StringUtils.stripEnd(strippedStart, "\"") + ANSI_RESET + " " + value.hashCode());
    log.debug("---------------------");
    completed.setRelease(false);
    items.add(value);
    completed.setRelease(true);
    log.debug("item list:");
    items.forEach(item -> log.debug("  " + item.toString()));
    log.debug("111111111111111111111");
  }

  public List<T> getItems() {
    Awaitility.await()
        .timeout(1, TimeUnit.MINUTES)
        .untilTrue(completed);
//      completed.setRelease(false);
    List<T> eventList = List.copyOf(items);
    items.clear();
    return eventList;
  }

  //    below included only informatively / as reminder of their existence
  @Override
  protected void hookOnCancel() { super.hookOnCancel(); }
  @Override
  protected void hookOnComplete() { super.hookOnComplete(); }
  @Override
  protected void hookOnError(@NonNull Throwable throwable) { super.hookOnError(throwable); }
  @Override
  protected void hookFinally(@NonNull SignalType type) { super.hookFinally(type); }
  @Override
  public void dispose() { super.dispose(); }
  @Override
  public boolean isDisposed() { return super.isDisposed(); }
  @Override
  protected @NonNull Subscription upstream() { return super.upstream(); }
  @Override
  public @NonNull Context currentContext() { return super.currentContext(); }
}
