package mekanism.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.function.Consumer;
import mekanism.common.item.interfaces.IEquipmentProtection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public abstract class EquipmentDamageMixin {

    @WrapOperation(method = "hurtAndBreak", at = @At(value = "INVOKE",
          target = "Lnet/minecraft/world/item/ItemStack;hurt(ILnet/minecraft/util/RandomSource;Lnet/minecraft/server/level/ServerPlayer;)Z"))
    private <T extends LivingEntity> boolean mekanism$equipmentDamage(ItemStack stack, int damage, RandomSource random, ServerPlayer player,
          Operation<Boolean> original, int amount, T entity, Consumer<T> onBroken) {
        int adjusted = stack.getItem() instanceof IEquipmentProtection equipment ? equipment.damageItem(stack, damage, entity, onBroken) : damage;
        return original.call(stack, adjusted, random, player);
    }
}
