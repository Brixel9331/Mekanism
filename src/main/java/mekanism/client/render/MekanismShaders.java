package mekanism.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.io.IOException;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

public class MekanismShaders {

    static final ShaderTracker MEKASUIT = new ShaderTracker();
    //Merge of position_color_tex and rendertype_lightning
    static final ShaderTracker SPS = new ShaderTracker();
    //Copy of position_color_tex with support for fog
    static final ShaderTracker FLAME = new ShaderTracker();

    public static void register() {
        CoreShaderRegistrationCallback.EVENT.register(MekanismShaders::shaderRegistry);
    }

    private static void shaderRegistry(CoreShaderRegistrationCallback.RegistrationContext event) throws IOException {
        registerShader(event, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "rendertype_flame"), DefaultVertexFormat.POSITION_COLOR_TEX, FLAME);
        registerShader(event, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "rendertype_mekasuit"), DefaultVertexFormat.NEW_ENTITY, MEKASUIT);
        registerShader(event, new ResourceLocation(MekanismAPI.MEKANISM_MODID, "rendertype_sps"), DefaultVertexFormat.POSITION_COLOR_TEX, SPS);
    }

    private static void registerShader(CoreShaderRegistrationCallback.RegistrationContext event, ResourceLocation shaderLocation, VertexFormat vertexFormat, ShaderTracker tracker) throws IOException {
        event.register(shaderLocation, vertexFormat, tracker::setInstance);
    }

    static class ShaderTracker implements Supplier<ShaderInstance> {

        private ShaderInstance instance;
        final ShaderStateShard shard = new ShaderStateShard(this);

        private ShaderTracker() {
        }

        private void setInstance(ShaderInstance instance) {
            this.instance = instance;
        }

        @Override
        public ShaderInstance get() {
            return instance;
        }
    }
}