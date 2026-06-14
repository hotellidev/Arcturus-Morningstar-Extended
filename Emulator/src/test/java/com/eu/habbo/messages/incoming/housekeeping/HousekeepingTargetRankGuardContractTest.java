package com.eu.habbo.messages.incoming.housekeeping;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HousekeepingTargetRankGuardContractTest {
    private static final List<String> RANK_GUARDED_HANDLERS = List.of(
            "HousekeepingBanUserEvent.java",
            "HousekeepingForceDisconnectUserEvent.java",
            "HousekeepingGiveCreditsEvent.java",
            "HousekeepingGiveCurrencyEvent.java",
            "HousekeepingGrantItemEvent.java",
            "HousekeepingKickUserEvent.java",
            "HousekeepingMuteUserEvent.java",
            "HousekeepingResetUserPasswordEvent.java",
            "HousekeepingSetHcSubscriptionEvent.java",
            "HousekeepingTradeLockUserEvent.java",
            "HousekeepingUnbanUserEvent.java"
    );

    @Test
    void privilegedUserActionsRejectPeerAndHigherRankTargets() throws Exception {
        String guard = Files.readString(Path.of("src/main/java/com/eu/habbo/messages/incoming/housekeeping/HousekeepingTargetRankGuard.java"));

        assertTrue(guard.contains("targetInfo.getRank().getId() < operator.getHabboInfo().getRank().getId()"),
                "Housekeeping user actions must only target lower-ranked users");
    }

    @Test
    void sensitiveHousekeepingUserActionsUseRankGuard() throws Exception {
        Path base = Path.of("src/main/java/com/eu/habbo/messages/incoming/housekeeping");

        for (String handler : RANK_GUARDED_HANDLERS) {
            String source = Files.readString(base.resolve(handler));
            assertTrue(source.contains("HousekeepingTargetRankGuard.canTargetUser(this.client.getHabbo(), userId)"),
                    handler + " must reject equal or higher-ranked targets before applying privileged user actions");
            assertTrue(source.contains("housekeeping.error.rank_too_high"),
                    handler + " must return a rank-ceiling error when the target cannot be managed");
        }
    }
}
