package com.eu.habbo.messages.incoming.housekeeping;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;

final class HousekeepingTargetRankGuard {
    private HousekeepingTargetRankGuard() {
    }

    static boolean canTargetUser(Habbo operator, int targetUserId) {
        if (operator == null || targetUserId <= 0) {
            return false;
        }

        HabboInfo targetInfo = Emulator.getGameEnvironment().getHabboManager().getHabboInfo(targetUserId);
        if (targetInfo == null) {
            return true;
        }

        return targetInfo.getRank().getId() < operator.getHabboInfo().getRank().getId();
    }
}
