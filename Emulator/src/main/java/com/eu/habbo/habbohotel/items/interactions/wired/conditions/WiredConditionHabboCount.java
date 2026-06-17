package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.habbohotel.wired.core.WiredContext;
import com.eu.habbo.habbohotel.wired.core.WiredManager;
import com.eu.habbo.habbohotel.wired.core.WiredSourceUtil;
import com.eu.habbo.messages.ServerMessage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredConditionHabboCount extends InteractionWiredCondition {
    public static final WiredConditionType type = WiredConditionType.USER_COUNT;
    static final int MAX_USER_COUNT_LIMIT = 1000;

    private int lowerLimit = 0;
    private int upperLimit = 50;
    private int userSource = WiredSourceUtil.SOURCE_TRIGGER;

    public WiredConditionHabboCount(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredConditionHabboCount(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean evaluate(WiredContext ctx) {
        if (ctx == null || ctx.room() == null) {
            return false;
        }

        int count = (this.userSource == WiredSourceUtil.SOURCE_TRIGGER)
                ? ctx.room().getUserCount()
                : WiredSourceUtil.resolveUsers(ctx, this.userSource).size();

        return count >= this.lowerLimit && count <= this.upperLimit;
    }

    @Deprecated
    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        return false;
    }

    @Override
    public String getWiredData() {
        return WiredManager.getGson().toJson(new JsonData(
                this.lowerLimit,
                this.upperLimit,
                this.userSource
        ));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        this.onPickUp();

        String wiredData = set.getString("wired_data");
        if (wiredData == null || wiredData.isEmpty()) {
            return;
        }

        if (wiredData.startsWith("{")) {
            JsonData data;
            try {
                data = WiredManager.getGson().fromJson(wiredData, JsonData.class);
            } catch (RuntimeException exception) {
                this.onPickUp();
                return;
            }

            if (data == null) {
                return;
            }

            this.setLimits(data.lowerLimit, data.upperLimit);
            this.userSource = this.normalizeUserSource(data.userSource);
            return;
        }

        String[] data = wiredData.split(":");
        if (data.length >= 2) {
            try {
                this.setLimits(Integer.parseInt(data[0].trim()), Integer.parseInt(data[1].trim()));
            } catch (NumberFormatException ignored) {
                this.onPickUp();
            }
        }
        this.userSource = WiredSourceUtil.SOURCE_TRIGGER;
    }

    @Override
    public void onPickUp() {
        this.lowerLimit = 0;
        this.upperLimit = 50;
        this.userSource = WiredSourceUtil.SOURCE_TRIGGER;
    }

    @Override
    public WiredConditionType getType() {
        return type;
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(false);
        message.appendInt(5);
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");
        message.appendInt(3);
        message.appendInt(this.lowerLimit);
        message.appendInt(this.upperLimit);
        message.appendInt(this.userSource);
        message.appendInt(0);
        message.appendInt(this.getType().code);
        message.appendInt(0);
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        if (settings.getIntParams().length < 2) return false;
        int[] params = settings.getIntParams();
        this.setLimits(params[0], params[1]);
        this.userSource = (params.length > 2) ? this.normalizeUserSource(params[2]) : WiredSourceUtil.SOURCE_TRIGGER;

        return true;
    }

    void setLimits(int lowerLimit, int upperLimit) {
        int normalizedLower = this.normalizeLimit(lowerLimit);
        int normalizedUpper = this.normalizeLimit(upperLimit);

        if (normalizedLower > normalizedUpper) {
            this.lowerLimit = normalizedUpper;
            this.upperLimit = normalizedLower;
            return;
        }

        this.lowerLimit = normalizedLower;
        this.upperLimit = normalizedUpper;
    }

    int normalizeLimit(int value) {
        return Math.max(0, Math.min(MAX_USER_COUNT_LIMIT, value));
    }

    int normalizeUserSource(int value) {
        return WiredSourceUtil.isDefaultUserSource(value) ? value : WiredSourceUtil.SOURCE_TRIGGER;
    }

    static class JsonData {
        int lowerLimit;
        int upperLimit;
        int userSource;

        public JsonData(int lowerLimit, int upperLimit, int userSource) {
            this.lowerLimit = lowerLimit;
            this.upperLimit = upperLimit;
            this.userSource = userSource;
        }
    }
}
