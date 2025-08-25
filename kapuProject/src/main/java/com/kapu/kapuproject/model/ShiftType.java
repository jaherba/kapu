package com.kapu.kapuproject.model;

public enum ShiftType {
    NIGHT_SHIFT_SF(true),
    NIGHT_SHIFT(false),
    NIGHT_SHIFT_PLUS(false),

    MORNING_SF(true),
    MORNING_REZE(false),
    MORNING_CAFE_1(false),
    MORNING_CAFE_2(false),

    TS_1(false),
    TS_2(false),

    SEPP(true),

    AFTERNOON_SF(true),
    AFTERNOON_REZE(false),
    AFTERNOON_CAFE(false),
    AFTERNOON_GASTRO(false),

    AS_1(false),
    AS_2(false),

    EXTRA(false);

    private final boolean shiftLeaderOnly;

    ShiftType(boolean shiftLeaderOnly) {
        this.shiftLeaderOnly = shiftLeaderOnly;
    }

    public boolean isShiftLeaderOnly() {
        return shiftLeaderOnly;
    }
}
