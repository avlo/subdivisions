package com.prosilion.subdivisions.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.Awaitility;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;

@Slf4j
public class TestSubscriber<T> extends BaseSubscriber<T> {
  
  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_BLUE = "\033[1;34m";

  private final List<T> items = Collections.synchronizedList(new ArrayList<>());
  private final AtomicBoolean completed = new AtomicBoolean(false);
  private Subscription subscription;
  
  @Override
  public void hookOnSubscribe(@NonNull Subscription subscription) {
    log.debug("0000000000000000000000");
    log.debug("0000000000000000000000");
//    log.debug("in TestSubscriber.hookOnSubscribe()");
    log.debug("00000 1of2) hookOnSubscribe should be FALSE, is: [{}]", completed.get());
    this.subscription = subscription;
    subscription.request(1);

    log.debug("TestSubscriber item list:");
    items.forEach(item -> log.debug("  " + item.toString()));
    
    log.debug("00000 2of2) hookOnSubscribe should still be FALSE, is: [{}]", completed.get());
//    log.debug(" Subscription object hashCode: [ " + ANSI_BLUE + subscription.hashCode() + ANSI_RESET + " ]");
    log.debug("0000000000000000000000");
    log.debug("0000000000000000000000");
  }

  @Override
  public void hookOnNext(@NonNull T value) {
    log.debug("1111111111111111111111");
    log.debug("1111111111111111111111");
//    log.debug("in TestSubscriber.hookOnNext()");
//    completed.setRelease(false);
    log.debug("11111 1of3) hookOnNext should be EITHER, is: [{}]", completed.get());
    completed.set(false);
    log.debug("-------------:");
    subscription.request(1);
//    completed.setRelease(true);
    log.debug("11111 2of3) hookOnNext should be FALSE, is: [{}]", completed.get());
    completed.set(true);
    log.debug("-------------:");
    log.debug("11111 3of3) hookOnNext should be TRUE, is: [{}]", completed.get());
    items.add(value);
    log.debug("TestSubscriber item list:");
    items.forEach(item -> log.debug("  " + item.toString()));
    log.debug("1111111111111111111111");
    log.debug("1111111111111111111111");
  }

  public List<T> getItems() {
    log.debug("2222222222222222222222");
    log.debug("2222222222222222222222");
//    log.debug("in TestSubscriber.getItems()");
//    Awaitility.await()
//        .timeout(3, TimeUnit.SECONDS)
//        .until(() -> completed.getAndSet(true));
    log.debug("22222 1of2) getItems should be EITHER, is: [{}]", completed.get());
    Awaitility.await()
        .timeout(3, TimeUnit.SECONDS)
        .untilTrue(completed);
    log.debug("--------------------");
    log.debug("22222 2of2) getItems should be TRUE, is: [{}]", completed.get());
//    List<T> eventList = List.copyOf(items);
//    items.clear();
    log.debug("2222222222222222222222");
    log.debug("2222222222222222222222");
    return items;
  }

  //    below included only informatively / as reminder of their existence
  @Override
  protected void hookOnCancel() {
    log.debug("3333333333333333333333");
    log.debug("3333333333333333333333");
  }

  @Override
  protected void hookOnComplete() {
    log.debug("in TestSubscriber.hookOnComplete()");
    log.debug("4444444444444444444444");
    log.debug("4444444444444444444444");
    completed.setRelease(true);
    log.debug("4444444444444444444444");
    log.debug("4444444444444444444444");
  }

  @Override
  protected void hookOnError(@NonNull Throwable throwable) {
    log.debug("5555555555555555555555");
    log.debug("5555555555555555555555");
  }

  @Override
  protected void hookFinally(@NonNull SignalType type) {
    log.debug("6666666666666666666666");
    log.debug("6666666666666666666666");
  }

  @Override
  public void dispose() {
    log.debug("7777777777777777777777");
    log.debug("7777777777777777777777");
  }

  @Override
  public boolean isDisposed() {
    return super.isDisposed();
  }

  @Override
  protected @NonNull Subscription upstream() {
    return super.upstream();
  }

  @Override
  public @NonNull Context currentContext() {
    return super.currentContext();
  }
}
