package mekanism.client.model;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import mekanism.client.render.lib.QuadTransformation;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@FunctionalInterface
public interface ModelGeometry {

    BakedModel bake(BlockModel source, BakedModel original, ModelBaker baker, Function<Material, TextureAtlasSprite> sprites, ModelState state,
          ResourceLocation id, BlendMode blendMode);

    default Collection<ResourceLocation> getDependencies() {
        return List.of();
    }

    static QuadTransformation rootTransform(BlockModel source, ModelState state) {
        if (source.getRootTransform().isIdentity()) {
            return QuadTransformation.identity;
        }
        Matrix4f rotation = new Matrix4f().translation(0.5F, 0.5F, 0.5F).mul(state.getRotation().getMatrix()).translate(-0.5F, -0.5F, -0.5F);
        Matrix4f transform = new Matrix4f(rotation).mul(source.getRootTransform().getMatrix()).mul(rotation.invert());
        Matrix3f normalTransform = transform.normal(new Matrix3f());
        return quad -> {
            quad.vertexTransform(vertex -> {
                vertex.pos(new Vec3(transform.transformPosition(vertex.getPos().toVector3f())));
                vertex.normal(normalTransform.transform(new Vector3f(vertex.getNormal())).normalize());
            });
            Vector3f normal = normalTransform.transform(quad.getSide().step()).normalize();
            quad.setSide(Direction.getNearest(normal.x, normal.y, normal.z));
            return true;
        };
    }
}
