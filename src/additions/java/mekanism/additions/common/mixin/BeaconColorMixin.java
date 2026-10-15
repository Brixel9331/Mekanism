package mekanism.additions.common.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mekanism.additions.common.block.plastic.BlockPlasticTransparent;
import mekanism.additions.common.block.plastic.BlockPlasticTransparentSlab;
import mekanism.additions.common.block.plastic.BlockPlasticTransparentStairs;
import mekanism.common.block.interfaces.IColoredBlock;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconColorMixin {

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"))
    private static Block mekanism$coloredPlastic(BlockState state, Operation<Block> original) {
        Block block = original.call(state);
        return mekanism$isTransparentPlastic(block) ? Blocks.WHITE_STAINED_GLASS : block;
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/DyeColor;getTextureDiffuseColors()[F"))
    private static float[] mekanism$plasticColor(DyeColor color, Operation<float[]> original, @Local(ordinal = 1) BlockState state) {
        Block block = state.getBlock();
        return mekanism$isTransparentPlastic(block) ? ((IColoredBlock) block).getColor().getRgbCodeFloat() : original.call(color);
    }

    @Unique
    private static boolean mekanism$isTransparentPlastic(Block block) {
        return block instanceof BlockPlasticTransparent || block instanceof BlockPlasticTransparentStairs || block instanceof BlockPlasticTransparentSlab;
    }
}
