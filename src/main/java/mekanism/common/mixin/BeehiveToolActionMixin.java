package mekanism.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mekanism.api.gear.ToolAction;
import mekanism.common.util.ToolModification;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BeehiveBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeehiveBlock.class)
public abstract class BeehiveToolActionMixin {

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    private boolean mekanism$toolAction(ItemStack stack, Item item, Operation<Boolean> original) {
        return original.call(stack, item) || item == Items.SHEARS && ToolModification.canPerform(stack, ToolAction.SHEARS_HARVEST);
    }
}
