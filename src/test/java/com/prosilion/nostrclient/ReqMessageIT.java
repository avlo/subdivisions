//package com.prosilion.nostrclient;
//
//import java.io.IOException;
//import java.util.Map;
//import java.util.Optional;
//import java.util.concurrent.ExecutionException;
//import lombok.NonNull;
//import lombok.extern.slf4j.Slf4j;
//import nostr.base.Command;
//import org.junit.jupiter.api.Test;
//import org.springframework.test.context.ActiveProfiles;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@Slf4j
//@ActiveProfiles("test")
//class ReqMessageIT {
//  private final RelaySubscriptions relaySubscriptions;
//
//  ReqMessageIT(@NonNull RelaySubscriptions relaySubscriptions) {
//    this.relaySubscriptions = relaySubscriptions;
//  }
//
//  @Test
//  void testReqFilteredByEventAndAuthor() throws IOException, ExecutionException, InterruptedException {
//    String uuidKey = "dddeee6101d3d152c6270e18f5622d1f8bce4ac5da9ab62d7c3cc0006e5914cc";
//    String authorPubkey = "dddeeef81439ff794cf5ac5f7bff9121e257f399829e472c7a14d3e86fe76984";
//
//    Map<Command, Optional<String>> returnedJsonMap = relaySubscriptions.sendRequest(
//        createReqJson(uuidKey, authorPubkey),
//        uuidKey
//    );
//    log.debug("okMessage:");
//    log.debug("  " + returnedJsonMap);
//    assertTrue(returnedJsonMap.get(Command.EVENT).get().contains(uuidKey));
//    assertTrue(returnedJsonMap.get(Command.EVENT).get().contains(authorPubkey));
//  }
//
//  private String createReqJson(@NonNull String uuid, @NonNull String authorPubkey) {
//    return "[\"REQ\",\"" + uuid + "\",{\"ids\":[\"" + uuid + "\"],\"authors\":[\"" + authorPubkey + "\"]}]";
//  }
//
//  @Test
//  void testReqFilteredByEventId() throws IOException, ExecutionException, InterruptedException {
//    String uuidKey = "dddeee6101d3d152c6270e18f5622d1f8bce4ac5da9ab62d7c3cc0006e5914cc";
//
//    Map<Command, Optional<String>> returnedJsonMap = relaySubscriptions.sendRequest(
//        createEventReqJson(uuidKey),
//        uuidKey
//    );
//    log.debug("okMessage:");
//    log.debug("  " + returnedJsonMap);
//    assertTrue(returnedJsonMap.get(Command.EVENT).get().contains(uuidKey));
//  }
//
//  private String createEventReqJson(@NonNull String uuid) {
//    return "[\"REQ\",\"" + uuid + "\",{\"ids\":[\"" + uuid + "\"]}]";
//  }
//}
