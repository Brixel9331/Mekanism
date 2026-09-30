package mekanism.client.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mekanism.client.model.data.DriveArrayItemData;
import mekanism.client.model.energycube.EnergyCubeModelLoader;
import mekanism.client.model.robit.RobitItemOverrides;
import mekanism.client.model.robit.RobitModel;
import mekanism.client.model.robit.RobitTextureAtlas;
import mekanism.client.render.item.block.RenderEnergyCubeItem;
import mekanism.client.render.item.block.RenderFluidTankItem;
import mekanism.common.Mekanism;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.tile.qio.TileEntityQIODriveArray.DriveStatus;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.resources.ResourceLocation;

public final class ClientModelRegistration {

    private ClientModelRegistration() {
    }

    public static void register() {
        GeometryModelLoader.register(Map.of(
              Mekanism.rl("energy_cube"), EnergyCubeModelLoader.INSTANCE::read,
              Mekanism.rl("robit"), json -> new RobitModel(json, RobitTextureAtlas::getSprite, RobitItemOverrides::new)
        ));
        Map<Integer, ResourceLocation> drives = new HashMap<>();
        for (DriveStatus status : DriveStatus.STATUSES) {
            if (status != DriveStatus.NONE) {
                drives.put((int) status.status(), status.getModel());
            }
        }
        DriveArrayModelLoader.register(MekanismBlocks.QIO_DRIVE_ARRAY.getRegistryName(), drives, DriveArrayItemData::read);
        for (var block : List.of(MekanismBlocks.BASIC_ENERGY_CUBE, MekanismBlocks.ADVANCED_ENERGY_CUBE, MekanismBlocks.ELITE_ENERGY_CUBE,
              MekanismBlocks.ULTIMATE_ENERGY_CUBE, MekanismBlocks.CREATIVE_ENERGY_CUBE)) {
            BuiltinItemRendererRegistry.INSTANCE.register(block.asItem(), RenderEnergyCubeItem.RENDERER::renderByItem);
        }
        for (var block : List.of(MekanismBlocks.BASIC_FLUID_TANK, MekanismBlocks.ADVANCED_FLUID_TANK, MekanismBlocks.ELITE_FLUID_TANK,
              MekanismBlocks.ULTIMATE_FLUID_TANK, MekanismBlocks.CREATIVE_FLUID_TANK)) {
            BuiltinItemRendererRegistry.INSTANCE.register(block.asItem(), RenderFluidTankItem.RENDERER::renderByItem);
        }
    }
}
