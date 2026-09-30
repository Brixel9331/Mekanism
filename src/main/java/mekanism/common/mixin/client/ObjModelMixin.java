package mekanism.common.mixin.client;

import io.github.fabricators_of_create.porting_lib.models.obj.ObjModel;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ObjModel.class, remap = false)
public abstract class ObjModelMixin {

    @Redirect(method = "bakeMeshes", at = @At(value = "INVOKE", target = "Ljava/util/Collection;forEach(Ljava/util/function/Consumer;)V"))
    private void mekanism$visibleRootParts(Collection<ObjModel.ModelGroup> parts, Consumer<ObjModel.ModelGroup> bake, BlockModel owner,
          ModelBaker baker, Function<Material, TextureAtlasSprite> sprites, ModelState state) {
        for (ObjModel.ModelGroup part : parts) {
            if (owner == null || owner.isComponentVisible(part.name(), true)) {
                bake.accept(part);
            }
        }
    }
}
