package mekanism.client.render.obj;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.block.model.BlockModel;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WrapperModelConfiguration extends BlockModel {

    protected final BlockModel internal;

    protected WrapperModelConfiguration(BlockModel internal) {
        super(null, List.of(), Map.of(), internal.hasAmbientOcclusion(), internal.getGuiLight(), internal.getTransforms(), List.of());
        this.internal = internal;
    }

    @Override
    public boolean hasTexture(@NotNull String name) {
        return internal.hasTexture(name);
    }

    @NotNull
    @Override
    public Material getMaterial(@NotNull String name) {
        return internal.getMaterial(name);
    }

    @Override
    public GuiLight getGuiLight() {
        return GuiLight.FRONT;
    }

    @Override
    public boolean hasAmbientOcclusion() {
        return internal.hasAmbientOcclusion();
    }

    @NotNull
    @Override
    public ItemTransforms getTransforms() {
        return internal.getTransforms();
    }

    @Override
    public Transformation getRootTransform() {
        return internal.getRootTransform();
    }

    @Override
    public boolean isComponentVisible(String component, boolean fallback) {
        return internal.isComponentVisible(component, fallback);
    }

}
