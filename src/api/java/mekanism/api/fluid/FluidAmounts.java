package mekanism.api.fluid;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;

public final class FluidAmounts {

    public static final int MILLI_BUCKETS_PER_BUCKET = 1_000;
    public static final long FABRIC_UNITS_PER_MILLI_BUCKET = FluidConstants.BUCKET / MILLI_BUCKETS_PER_BUCKET;

    private FluidAmounts() {
    }

    public static long toFabric(int milliBuckets) {
        if (milliBuckets < 0) {
            throw new IllegalArgumentException("Fluid amount must not be negative");
        }
        return milliBuckets * FABRIC_UNITS_PER_MILLI_BUCKET;
    }

    public static int toMilliBuckets(long fabricAmount) {
        if (fabricAmount < 0) {
            throw new IllegalArgumentException("Fluid amount must not be negative");
        }
        return (int) Math.min(Integer.MAX_VALUE, fabricAmount / FABRIC_UNITS_PER_MILLI_BUCKET);
    }
}
