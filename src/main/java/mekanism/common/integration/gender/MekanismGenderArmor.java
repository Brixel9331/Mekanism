package mekanism.common.integration.gender;

import com.wildfire.api.IGenderArmor;

public final class MekanismGenderArmor implements IGenderArmor {

    public static final MekanismGenderArmor OPEN_FRONT = new MekanismGenderArmor(false, false, 0, 0);
    public static final MekanismGenderArmor HIDES_BREASTS = new MekanismGenderArmor(true, true, 0, 0);
    static final MekanismGenderArmor HAZMAT = new MekanismGenderArmor(0.5F, 0.25F);

    private final boolean coversBreasts;
    private final boolean alwaysHidesBreasts;
    private final float physicsResistance;
    private final float tightness;

    public MekanismGenderArmor(float physicsResistance) {
        this(physicsResistance, 0);
    }

    public MekanismGenderArmor(float physicsResistance, float tightness) {
        this(true, false, physicsResistance, tightness);
    }

    private MekanismGenderArmor(boolean coversBreasts, boolean alwaysHidesBreasts, float physicsResistance, float tightness) {
        if (physicsResistance < 0 || physicsResistance > 1) {
            throw new IllegalArgumentException("Physics resistance must be between zero and one inclusive.");
        } else if (tightness < 0 || tightness > 1) {
            throw new IllegalArgumentException("Armor tightness must be between zero and one inclusive.");
        }
        this.coversBreasts = coversBreasts;
        this.alwaysHidesBreasts = alwaysHidesBreasts;
        this.physicsResistance = physicsResistance;
        this.tightness = tightness;
    }

    @Override
    public boolean coversBreasts() {
        return coversBreasts;
    }

    @Override
    public boolean alwaysHidesBreasts() {
        return alwaysHidesBreasts;
    }

    @Override
    public float physicsResistance() {
        return physicsResistance;
    }

    @Override
    public float tightness() {
        return tightness;
    }

}
