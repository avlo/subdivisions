//package com.prosilion.subdivisions.client.virtualthread;
//
//import com.prosilion.nostr.message.EventMessage;
//import com.prosilion.nostr.message.OkMessage;
//import java.io.IOException;
//import java.util.List;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.Executors;
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ssl.SslBundle;
//import org.springframework.boot.ssl.SslBundles;
//
//@Slf4j
//public class VThreadEventPublisher {
//  private final VThreadWebSocketClient eventSocketClient;
//
//  public VThreadEventPublisher(@NonNull String relayUri) throws ExecutionException, InterruptedException {
//    log.debug("relayUri: \n{}", relayUri);
//    this.eventSocketClient = new VThreadWebSocketClient(relayUri);
//    log.debug("eventSocketClient: \n{}", this.eventSocketClient);
//  }
//
//  public VThreadEventPublisher(@NonNull String relayUri, SslBundles sslBundles) throws ExecutionException, InterruptedException {
//    log.debug("sslBundles: \n{}", sslBundles);
//    final SslBundle server = sslBundles.getBundle("server");
//    log.debug("sslBundles name: \n{}", server);
//    log.debug("sslBundles key: \n{}", server.getKey());
//    log.debug("sslBundles protocol: \n{}", server.getProtocol());
//    this.eventSocketClient = new VThreadWebSocketClient(relayUri, sslBundles);
//  }
//
//  public OkMessage send(@NonNull EventMessage eventMessage) throws IOException {
//    eventSocketClient.send(eventMessage);
//
//    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
//      executor.submit(() -> {
//        var datas = getDataFromDatabase();     // blocking I/O
//        var processed = processData(datas);
//        saveResult(processed);
//      });
//    }
//    
//    log.debug("socket send EventMessage content\n  {}", eventMessage.getEvent());
//    List<String> events = eventSocketClient.getEvents();
//    String first = events.getFirst();
//    OkMessage decode = OkMessage.decode(first);
//    return decode;
//  }
//
//}
