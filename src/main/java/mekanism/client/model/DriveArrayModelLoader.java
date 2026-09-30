package mekanism.client.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import mekanism.client.model.baked.DriveArrayBakedModel;
import mekanism.common.lib.render.DriveArrayModelData;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class DriveArrayModelLoader {

    private DriveArrayModelLoader() {
    }

    public static void register(ResourceLocation block, Map<Integer, ResourceLocation> driveModels, Function<ItemStack, DriveArrayModelData> itemData) {
        Map<Integer, ResourceLocation> models = Map.copyOf(driveModels);
        ModelLoadingPlugin.register(context -> {
            context.addModels(models.values());
            context.modifyModelAfterBake().register(ModelModifier.WRAP_PHASE, (model, bake) -> {
                if (model == null || model instanceof DriveArrayBakedModel || !(bake.id() instanceof ModelResourceLocation id) ||
                    !id.getNamespace().equals(block.getNamespace()) || !id.getPath().equals(block.getPath())) {
                    return model;
                }
                Map<Integer, BakedModel> drives = new HashMap<>();
                models.forEach((status, location) -> {
                    BakedModel drive = bake.baker().bake(location, BlockModelRotation.X0_Y0);
                    if (drive != null) {
                        drives.put(status, drive);
                    }
                });
                return new DriveArrayBakedModel(model, drives, itemData);
            });
        });
    }
}
