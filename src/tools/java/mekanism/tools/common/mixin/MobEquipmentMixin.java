package mekanism.tools.common.mixin;

import mekanism.tools.common.MobEquipmentHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Zombie.class, AbstractSkeleton.class, Piglin.class})
public abstract class MobEquipmentMixin {

    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void mekanismtools$equipSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason,
          SpawnGroupData group, CompoundTag tag, CallbackInfoReturnable<SpawnGroupData> callback) {
        MobEquipmentHelper.onLivingSpecialSpawn((LivingEntity) (Object) this, world, difficulty);
    }
}
