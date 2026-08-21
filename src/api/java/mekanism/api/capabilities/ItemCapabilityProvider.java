package mekanism.api.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface ItemCapabilityProvider {

    @Nullable
    CapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt);
}
