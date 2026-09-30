package mekanism.common.lib.render;

import java.util.Objects;

public record DriveArrayModelData(long statuses) {

    public static final int SLOTS = 12;
    public static final int STATUS_COUNT = 5;
    public static final DriveArrayModelData EMPTY = new DriveArrayModelData(0);

    public DriveArrayModelData {
        long normalized = 0;
        for (int slot = 0; slot < SLOTS; slot++) {
            long status = statuses >>> (slot * 3) & 7;
            if (status < STATUS_COUNT) {
                normalized |= status << (slot * 3);
            }
        }
        statuses = normalized;
    }

    public static DriveArrayModelData of(byte[] statuses) {
        long packed = 0;
        for (int slot = 0; slot < Math.min(SLOTS, statuses.length); slot++) {
            int status = statuses[slot];
            if (status > 0 && status < STATUS_COUNT) {
                packed |= (long) status << (slot * 3);
            }
        }
        return new DriveArrayModelData(packed);
    }

    public int status(int slot) {
        Objects.checkIndex(slot, SLOTS);
        return (int) (statuses >>> (slot * 3) & 7);
    }

    public byte[] toArray() {
        byte[] result = new byte[SLOTS];
        for (int slot = 0; slot < SLOTS; slot++) {
            result[slot] = (byte) status(slot);
        }
        return result;
    }
}
