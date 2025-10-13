//package com.prosilion.reqclient;
//
//import com.prosilion.nostr.event.BadgeDefinitionEvent;
//import com.prosilion.nostr.tag.IdentifierTag;
//import com.prosilion.nostr.tag.ReferenceTag;
//import com.prosilion.nostr.user.Identity;
//import java.security.NoSuchAlgorithmException;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.lang.NonNull;
//
//@TestConfiguration
//public class ReqClientConfig {
//  @Bean
//  BadgeDefinitionEvent upvoteBadgeDefinitionEvent(
//      @NonNull Identity superconductorInstanceIdentity,
//      @NonNull String uuid,
//      @NonNull String superconductorRelayUrl) throws NoSuchAlgorithmException {
//    return new BadgeDefinitionEvent(
//        superconductorInstanceIdentity,
////        new IdentifierTag(SuperconductorKindType.UNIT_UPVOTE.getName()),
//        new IdentifierTag(uuid),
//        new ReferenceTag(superconductorRelayUrl),
//        "1");
//  }
//
//  @Bean
//  BadgeDefinitionEvent downvoteBadgeDefinitionEvent(
//      @NonNull Identity superconductorInstanceIdentity,
//      @NonNull String uuid,
//      @NonNull String superconductorRelayUrl) throws NoSuchAlgorithmException {
//    return new BadgeDefinitionEvent(
//        superconductorInstanceIdentity,
////        new IdentifierTag(SuperconductorKindType.UNIT_DOWNVOTE.getName()),
//        new IdentifierTag(uuid),
//        new ReferenceTag(superconductorRelayUrl),
//        "-1");
//  }
//}
