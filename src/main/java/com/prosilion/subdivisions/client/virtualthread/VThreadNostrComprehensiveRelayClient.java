//package com.prosilion.subdivisions.client.virtualthread;
//
//import com.prosilion.nostr.message.BaseMessage;
//import com.prosilion.nostr.message.EventMessage;
//import com.prosilion.nostr.message.OkMessage;
//import com.prosilion.nostr.message.ReqMessage;
//import java.io.IOException;
//import java.util.List;
//import java.util.concurrent.ExecutionException;
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ssl.SslBundle;
//import org.springframework.boot.ssl.SslBundles;
//
//@Slf4j
//public class VThreadNostrComprehensiveRelayClient {
//  private final VThreadEventPublisher VThreadEventPublisher;
//  private final VThreadRelaySubscriptionsManager VThreadRelaySubscriptionsManager;
//
//  public VThreadNostrComprehensiveRelayClient(@NonNull String relayUri) throws ExecutionException, InterruptedException {
//    log.debug("relayUri: \n{}", relayUri);
//    this.VThreadEventPublisher = new VThreadEventPublisher(relayUri);
//    this.VThreadRelaySubscriptionsManager = new VThreadRelaySubscriptionsManager(relayUri);
//  }
//
//  public VThreadNostrComprehensiveRelayClient(@NonNull String relayUri, SslBundles sslBundles) throws ExecutionException, InterruptedException {
//    log.debug("relayUri: \n{}", relayUri);
//    log.debug("sslBundles: \n{}", sslBundles);
//    final SslBundle server = sslBundles.getBundle("server");
//    log.debug("sslBundles name: \n{}", server);
//    log.debug("sslBundles key: \n{}", server.getKey());
//    log.debug("sslBundles protocol: \n{}", server.getProtocol());
//    this.VThreadEventPublisher = new VThreadEventPublisher(relayUri, sslBundles);
//    this.VThreadRelaySubscriptionsManager = new VThreadRelaySubscriptionsManager(relayUri, sslBundles);
//  }
//
//  public OkMessage send(@NonNull EventMessage eventMessage) throws IOException {
//    return VThreadEventPublisher.send(eventMessage);
//  }
//
//  public List<BaseMessage> send(@NonNull ReqMessage reqMessage) {
//    return VThreadRelaySubscriptionsManager.send(reqMessage);
//  }
//}
