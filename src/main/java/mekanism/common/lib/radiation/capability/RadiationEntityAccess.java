package mekanism.common.lib.radiation.capability;

import mekanism.api.radiation.capability.IRadiationEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public interface RadiationEntityAccess {

    IRadiationEntity mekanism$getRadiation();

    default void mekanism$writeRadiation(CompoundTag tag) {
        tag.put("mekanism:radiation", mekanism$getRadiation().serializeNBT());
    }

    default void mekanism$readRadiation(CompoundTag tag) {
        CompoundTag source = tag.contains("mekanism:radiation", Tag.TAG_COMPOUND) ? tag : tag.getCompound("ForgeCaps");
        mekanism$getRadiation().deserializeNBT(source.getCompound("mekanism:radiation"));
    }

    default void mekanism$copyRadiation(RadiationEntityAccess original) {
        mekanism$getRadiation().deserializeNBT(original.mekanism$getRadiation().serializeNBT());
    }
}
