package com.eu.habbo.messages.incoming.housekeeping;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.permissions.Rank;
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

        int operatorRankId = operator.getHabboInfo().getRank().getId();
        int targetRankId = targetInfo.getRank().getId();

        return targetRankId < operatorRankId || isCoreRank(operatorRankId) && targetRankId <= operatorRankId;
    }

    private static boolean isCoreRank(int rankId) {
        int highestRankId = 0;
        for (Rank rank : Emulator.getGameEnvironment().getPermissionsManager().getAllRanks()) {
            highestRankId = Math.max(highestRankId, rank.getId());
        }

        return highestRankId > 0 && rankId >= highestRankId;
    }
}
