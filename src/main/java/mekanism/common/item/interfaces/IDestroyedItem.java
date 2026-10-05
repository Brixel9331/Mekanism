package mekanism.common.item.interfaces;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;

public interface IDestroyedItem {

    void onDestroyed(ItemEntity entity, DamageSource source);
}
