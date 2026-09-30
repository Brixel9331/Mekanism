package mekanism.client.model.robit;

import java.util.List;
import mekanism.api.robit.RobitSkin;
import mekanism.common.item.ItemRobit;
import mekanism.common.registries.MekanismRobitSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class RobitItemOverrides extends ItemOverrides {

    private final ItemOverrides original;

    public RobitItemOverrides(ItemOverrides original) {
        super(null, null, List.of());
        this.original = original;
    }

    @Override
    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemRobit robit)) {
            return original.resolve(model, stack, level, entity, seed);
        }
        RegistryAccess registries;
        if (level != null) {
            registries = level.registryAccess();
        } else if (entity != null) {
            registries = entity.level().registryAccess();
        } else if (Minecraft.getInstance().level != null) {
            registries = Minecraft.getInstance().level.registryAccess();
        } else {
            return original.resolve(model, stack, level, entity, seed);
        }
        RobitSkin skin = MekanismRobitSkins.lookup(registries, robit.getRobitSkin(stack)).skin();
        BakedModel target = skin.customModel() == null ? model : RobitModels.getModel(skin.customModel());
        ItemOverrides targetOverrides = target == model ? original : target.getOverrides();
        if (targetOverrides instanceof RobitItemOverrides robitOverrides) {
            targetOverrides = robitOverrides.original;
        }
        BakedModel resolved = targetOverrides.resolve(target, stack, level, entity, seed);
        if (resolved == null) {
            return null;
        }
        List<ResourceLocation> textures = skin.textures();
        if (textures.isEmpty()) {
            return resolved;
        }
        if (!(resolved instanceof RobitBakedModel) && target instanceof RobitBakedModel) {
            resolved = new RobitBakedModel(resolved, RobitTextureAtlas::getSprite, RobitItemOverrides::new);
        }
        return RobitModels.bind(resolved, textures.get(0));
    }
}
