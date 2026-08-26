package mekanism.client.render;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import mekanism.client.model.baked.FluidBucketModel;
import mekanism.common.registration.impl.FluidDeferredRegister.MekanismFluidType;
import mekanism.common.registration.impl.FluidDeferredRegister;
import mekanism.common.registration.impl.FluidRegistryObject;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import org.jetbrains.annotations.Nullable;

public final class FluidRenderRegistration {

    private static final Set<FluidDeferredRegister> REGISTERED = Collections.newSetFromMap(new IdentityHashMap<>());

    private FluidRenderRegistration() {
    }

    public static void register(FluidDeferredRegister register) {
        if (!REGISTERED.add(register)) {
            return;
        }
        for (FluidRegistryObject<? extends MekanismFluidType, ?, ?, ?, ?> entry : register.getAllFluids()) {
            MekanismFluidType type = entry.getFluidType();
            SimpleFluidRenderHandler handler = new SimpleFluidRenderHandler(type.stillTexture, type.flowingTexture, type.overlayTexture, type.getColor());
            FluidRenderHandlerRegistry.INSTANCE.register(entry.getStillFluid(), entry.getFlowingFluid(), handler);
            FluidVariantRenderHandler variantHandler = new FluidVariantRenderHandler() {
                @Override
                public int getColor(FluidVariant variant, @Nullable BlockAndTintGetter view, @Nullable BlockPos pos) {
                    return type.getColor();
                }
            };
            FluidVariantRendering.register(entry.getStillFluid(), variantHandler);
            FluidVariantRendering.register(entry.getFlowingFluid(), variantHandler);
            BlockRenderLayerMap.INSTANCE.putFluids(RenderType.translucent(), entry.getStillFluid(), entry.getFlowingFluid());
            ColorProviderRegistry.ITEM.register((stack, tintIndex) -> tintIndex == 1 ?
                  FluidVariantRendering.getColor(FluidVariant.of(entry.getStillFluid())) : -1, entry.getBucket());
            FluidBucketModel.register(entry);
        }
    }

    public static TextureAtlasSprite getSprite(FluidVariant variant, boolean flowing) {
        TextureAtlasSprite[] sprites = FluidVariantRendering.getSprites(variant);
        if (sprites != null) {
            return sprites[flowing ? 1 : 0];
        }
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MissingTextureAtlasSprite.getLocation());
    }
}
