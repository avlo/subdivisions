package com.prosilion.subdivisions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.prosilion.nostr.enums.Kind;
import com.prosilion.nostr.event.EventIF;
import com.prosilion.nostr.filter.Filters;
import com.prosilion.nostr.filter.event.AuthorFilter;
import com.prosilion.nostr.filter.event.KindFilter;
import com.prosilion.nostr.filter.tag.IdentifierTagFilter;
import com.prosilion.nostr.filter.tag.ReferencedPublicKeyFilter;
import com.prosilion.nostr.message.BaseMessage;
import com.prosilion.nostr.message.EventMessage;
import com.prosilion.nostr.message.ReqMessage;
import com.prosilion.nostr.tag.IdentifierTag;
import com.prosilion.nostr.tag.PubKeyTag;
import com.prosilion.nostr.user.Identity;
import com.prosilion.nostr.user.PublicKey;
import com.prosilion.subdivisions.util.NostrRelayReqService;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
class StandaloneReactiveClientIT {
  /**
   * definitionsCreatorIdentity:   02d49b23e02985a760e8bc2f5ee86a3089569806f5f6a670fba3317568d14262
   * <p>
   * voteSubmitterIdentity:        611eda70943b4f67d1674068f5c86cedbdc3438bb41245b129a6311e4f308295
   * <p>
   * voteReceiverIdentity:          985a5b9ea911bb8f9d9dca82c03f776d68fdc452b774295a874423a0fa5e8879
   */

  Identity definitionsCreatorIdentity =
      Identity.create("bbb4585483196998204846989544737603523651520600328805626488477202");

  Identity voteSubmitterIdentity =
      Identity.create("aaa4585483196998204846989544737603523651520600328805626488477202");

  Identity voteRecipientIdentity =
      Identity.create("ccc4585483196998204846989544737603523651520600328805626488477202");

  NostrRelayReqService nostrRelayReqService;
  String relayUrl;
  String methodEncoded;

  public StandaloneReactiveClientIT() {
    nostrRelayReqService = new NostrRelayReqService();
    this.relayUrl = "ws://localhost:5555";
//    this.relayUrl = "ws://0.0.0.0:5555";
  }

  @Test
  void testReputationDefinitionCreatorRequest() throws JsonProcessingException {
    log.info("\n\ntestReputationDefinitionCreatorRequest");
    ReqMessage reqMessage = new ReqMessage(
        generateRandomHex64String(),
        getRepuationDefinitionCreatorFilters(definitionsCreatorIdentity.getPublicKey()));

    String reqMessageEncoded = reqMessage.encode();
    log.debug("reqMessageEncoded:\n{}", reqMessageEncoded);
    log.debug("reqMessageEncoded.equals(methodEncoded)? {}", reqMessageEncoded.equals(methodEncoded));

    List<BaseMessage> send = nostrRelayReqService.send(
        reqMessage,
        relayUrl);

    List<EventIF> genericEvents = getGenericEvents(send);
    String collect = genericEvents.stream().map(event ->
        event.createPrettyPrintJson()).collect(Collectors.joining(",\n"));
    log.debug("{}", collect);
  }

  @Test
  void testAwardDefinitionCreatorRequest() throws JsonProcessingException {
    log.info("\n\ntestAwardDefinitionCreatorRequest\n");
    ReqMessage reqMessage = new ReqMessage(
        generateRandomHex64String(),
        getAwardDefinitionCreatorFilters(definitionsCreatorIdentity.getPublicKey()));

    String reqMessageEncoded = reqMessage.encode();
    log.debug("reqMessageEncoded:\n{}", reqMessageEncoded);
    log.debug("reqMessageEncoded.equals(methodEncoded)? {}", reqMessageEncoded.equals(methodEncoded));

    List<BaseMessage> send = nostrRelayReqService.send(
        reqMessage,
        relayUrl);

    List<EventIF> genericEvents = getGenericEvents(send);
    String collect = genericEvents.stream().map(event ->
        event.createPrettyPrintJson()).collect(Collectors.joining(",\n"));
    log.debug("{}", collect);
  }

//  @Test
//  void testVoteSubmitterRequest() throws JsonProcessingException {
//    log.info("\n\ntestVoteSubmitterRequest");
//    List<BaseMessage> send = nostrRelayReqService.send(
//        new ReqMessage(
//            generateRandomHex64String(),
//            geFilters(voteSubmitterIdentity.getPublicKey())),
//        relayUrl);
//
//    List<EventIF> genericEvents = getGenericEvents(send);
//    String collect = genericEvents.stream().map(event ->
//        event.createPrettyPrintJson()).collect(Collectors.joining(",\n"));
//    log.debug("{}", collect);
//  }

  //  @Test
  void testVoteReceiverRequest() throws JsonProcessingException {
    log.info("\n\ntestVoteReceiverRequest");
    List<BaseMessage> send = nostrRelayReqService.send(
        new ReqMessage(
            generateRandomHex64String(),
            getVoteReceiverFilters(voteRecipientIdentity.getPublicKey())),
        relayUrl);

    List<EventIF> genericEvents = getGenericEvents(send);
    String collect = genericEvents.stream().map(event ->
        event.createPrettyPrintJson()).collect(Collectors.joining(",\n"));
    log.debug("{}", collect);
  }

  public static <T extends BaseMessage> List<EventIF> getGenericEvents(List<T> returnedBaseMessages) {
    return returnedBaseMessages.stream()
        .filter(EventMessage.class::isInstance)
        .map(EventMessage.class::cast)
        .map(EventMessage::getEvent)
        .toList();
  }

  private String generateRandomHex64String() {
    return UUID.randomUUID().toString().concat(UUID.randomUUID().toString()).replaceAll("[^A-Za-z0-9]", "");
  }

  private Filters getRepuationDefinitionCreatorFilters(PublicKey publicKey) throws JsonProcessingException {
/*
BADGE_DEFINITION_(UPVOTE)_EVENT (generated by general relays)
  "id": "BADGE_DEFINITION_UPVOTE_EVENT_ID",
  "kind": 30009,
  "pubkey": "BADGE_DEFINITION_UPVOTE_CREATOR_PUBKEY",
  ["d", "BADGE_DEFINITION_UNIT_UPVOTE"]
 */
    Filters filters = new Filters(
        new IdentifierTagFilter(new IdentifierTag("TEST_REPUTATION")),
        new KindFilter(Kind.BADGE_DEFINITION_EVENT),
        new AuthorFilter(publicKey));
    methodEncoded = new ReqMessage(this.generateRandomHex64String(), new Filters[]{filters}).encode();
    log.debug("methodEncoded:\n{}", methodEncoded);
    return filters;
  }

  private Filters getAwardDefinitionCreatorFilters(PublicKey publicKey) throws JsonProcessingException {
/*
BADGE_DEFINITION_(UPVOTE)_EVENT (generated by general relays)
  "id": "BADGE_DEFINITION_UPVOTE_EVENT_ID",
  "kind": 30009,
  "pubkey": "BADGE_DEFINITION_UPVOTE_CREATOR_PUBKEY",
  ["d", "BADGE_DEFINITION_UNIT_UPVOTE"]
 */
    Filters filters = new Filters(
        new IdentifierTagFilter(new IdentifierTag("TEST_UNIT_UPVOTE")),
        new KindFilter(Kind.BADGE_DEFINITION_EVENT),
        new AuthorFilter(publicKey));
    methodEncoded = new ReqMessage(this.generateRandomHex64String(), new Filters[]{filters}).encode();
    log.debug("methodEncoded:\n{}", methodEncoded);
    return filters;
  }

  private Filters getVoteReceiverFilters(PublicKey publicKey) {
/*
BADGE_AWARD_(UPVOTE)_EVENT (generated by general relays)
  "id": "UPVOTE_EVENT_ID",
  "kind": 8,
  "pubkey": "VOTE_SUBMITTER_PUBKEY",
  ["p", "VOTE_RECIP_1_PUBKEY", "ws://sc.url:port"],
  ["a", "30009:BADGE_DEFINITION_UPVOTE_CREATOR_PUBKEY:BADGE_DEFINITION_UNIT_UPVOTE", "relay.url"]
 */
    return new Filters(
        new KindFilter(Kind.BADGE_AWARD_EVENT),
        new ReferencedPublicKeyFilter(new PubKeyTag(publicKey)));
  }
}
