package mekanism.common.lib.render;

public record EnergyCubeModelData(int activeSides, int litSides) {

    public static final EnergyCubeModelData INACTIVE = new EnergyCubeModelData(0, 0);

    public EnergyCubeModelData {
        activeSides &= 63;
        litSides &= activeSides;
    }

    public boolean isActive(int side) {
        return (activeSides & 1 << side) != 0;
    }

    public boolean isLit(int side) {
        return (litSides & 1 << side) != 0;
    }

    public int cacheKey() {
        return activeSides | litSides << 6;
    }
}
