package mekanism.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import mekanism.api.RelativeSide;
import mekanism.client.ScreenChangeCallback;
import mekanism.client.gui.GuiMekanism;
import mekanism.client.render.MekanismRenderer.Model3D;
import mekanism.client.render.RenderResizableCuboid.FaceDisplay;
import mekanism.client.render.armor.MekaSuitArmor;
import mekanism.client.render.hud.RadiationOverlay;
import mekanism.client.render.lib.Quad;
import mekanism.client.render.lib.QuadUtils;
import mekanism.client.render.lib.Vertex;
import mekanism.client.render.lib.effect.BoltRenderer;
import mekanism.client.render.tileentity.IWireFrameRenderer;
import mekanism.common.Mekanism;
import mekanism.common.base.ProfilerConstants;
import mekanism.common.block.BlockBounding;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeCustomSelectionBox;
import mekanism.common.content.gear.IBlastingItem;
import mekanism.common.integration.MekanismHooks;
import mekanism.common.item.ItemConfigurator;
import mekanism.common.item.ItemConfigurator.ConfiguratorMode;
import mekanism.common.item.gear.ItemFlamethrower;
import mekanism.common.item.gear.ItemMekaSuitArmor;
import mekanism.common.lib.effect.BoltEffect;
import mekanism.common.lib.math.Pos3D;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registries.MekanismParticleTypes;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.DataType;
import mekanism.common.tile.interfaces.ISideConfiguration;
import mekanism.common.util.ChemicalUtil;
import mekanism.common.util.EnumUtils;
import mekanism.common.util.WorldUtils;
import mezz.jei.api.runtime.IRecipesGui;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext.BlockOutlineContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class RenderTickHandler {

    public final Minecraft minecraft = Minecraft.getInstance();

    private static final Map<BlockState, List<Vertex[]>> cachedWireFrames = new HashMap<>();
    private static final Map<Direction, Map<TransmissionType, Model3D>> cachedOverlays = new EnumMap<>(Direction.class);
    private static final DelayedRenderQueue transparentRenderers = new DelayedRenderQueue();
    private static final BoltRenderer boltRenderer = new BoltRenderer();

    private final FirstPersonArmPose<AbstractClientPlayer> armPose = new FirstPersonArmPose<>();
    private boolean outliningArea = false;

    public static void clearQueued() {
        RadiationOverlay.INSTANCE.resetRadiation();
        transparentRenderers.clear();
        boltRenderer.clear();
    }

    public static void resetCached() {
        cachedOverlays.clear();
        cachedWireFrames.clear();
    }

    public static void renderBolt(Object renderer, BoltEffect bolt) {
        boltRenderer.update(renderer, bolt, MekanismRenderer.getPartialTick());
    }

    public static void register() {
        RenderTickHandler handler = new RenderTickHandler();
        WorldRenderEvents.START.register(context -> transparentRenderers.clear());
        WorldRenderEvents.AFTER_TRANSLUCENT.register(handler::renderWorld);
        WorldRenderEvents.BLOCK_OUTLINE.register(handler::onBlockHover);
        RenderFrameCallback.END.register(handler::tickEnd);
        RenderArmCallback.EVENT.register(handler::renderArm);
        registerScreenEvents();
    }

    public static void registerScreenEvents() {
        if (FabricLoader.getInstance().isModLoaded(MekanismHooks.JEI_MOD_ID)) {
            ScreenChangeCallback.EVENT.register(RenderTickHandler::guiOpening);
        }
    }

    //Note: This listener is only registered if JEI is loaded
    private static void guiOpening(Minecraft client, Screen previous, Screen next) {
        if (previous instanceof GuiMekanism<?> screen) {
            //If JEI is loaded and our current screen is a mekanism gui,
            // check if the new screen is a JEI recipe screen
            if (next instanceof IRecipesGui) {
                //If it is mark on our current screen that we are switching to JEI
                screen.switchingToJEI = true;
            }
        }
    }

    public static void addTransparentRenderer(RenderType renderType, LazyRender render) {
        transparentRenderers.add(renderType, render);
    }

    public void renderWorld(WorldRenderContext context) {
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        transparentRenderers.render(context.camera(), buffers, context.matrixStack(), context.worldRenderer().ticks, context.tickDelta(), context.profiler());
        if (boltRenderer.hasBoltsToRender()) {
            renderStage(context, buffers, (camera, renderer, pose, renderTick, partialTick) -> {
                try {
                    boltRenderer.render(partialTick, pose, renderer);
                } finally {
                    renderer.endBatch(MekanismRenderType.MEK_LIGHTNING);
                }
            });
        }
    }

    private void renderStage(WorldRenderContext context, MultiBufferSource.BufferSource buffers, StageRenderer renderer) {
        Camera camera = context.camera();
        PoseStack matrix = context.matrixStack();
        try (PoseStackScope ignored = new PoseStackScope(matrix)) {
            Vec3 position = camera.getPosition();
            matrix.translate(-position.x, -position.y, -position.z);
            renderer.render(camera, buffers, matrix, context.worldRenderer().ticks, context.tickDelta());
        }
    }

    public boolean renderArm(PlayerRenderer renderer, AbstractClientPlayer player, HumanoidArm arm, PoseStack pose, MultiBufferSource buffers, int light) {
        ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chestStack.getItem() instanceof ItemMekaSuitArmor) {
            try (PoseStackScope ignored = new PoseStackScope(pose)) {
                MekaSuitArmor.BODYARMOR.renderArm(armPose.prepare(renderer.getModel(), player, arm), pose, buffers, light, OverlayTexture.NO_OVERLAY, player,
                      chestStack, arm == HumanoidArm.RIGHT);
            }
            return true;
        }
        return false;
    }

    public void tickEnd(Minecraft client, float partialTick) {
        if (!client.noRender) {
            Level world;
            Player player = minecraft.player;
            //Note: We check that the game mode is not null as if it is that means the world is unloading, and we don't actually want to be rendering
            // as our data may be out of date or invalid. For example configs could unload while it is still unloading
            //noinspection ConstantValue
            if (player != null && (world = player.level()) != null && !minecraft.isPaused() && minecraft.gameMode != null) {
                for (Player p : world.players()) {
                    //Check active jetpack and do animations
                    if (Mekanism.playerState.isJetpackOn(p)) {
                        Pos3D playerPos = new Pos3D(p).translate(0, p.getEyeHeight(), 0);
                        Vec3 playerMotion = p.getDeltaMovement();
                        float random = (world.random.nextFloat() - 0.5F) * 0.1F;
                        //This positioning code is somewhat cursed, but it seems to be mostly working and entity pose code seems cursed in general
                        float xRot;
                        if (p.isCrouching()) {
                            xRot = 20;
                            playerPos = playerPos.translate(0, 0.125, 0);
                        } else {
                            float f = p.getSwimAmount(partialTick);
                            if (p.isFallFlying()) {
                                float f1 = (float) p.getFallFlyingTicks() + partialTick;
                                float f2 = Mth.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
                                xRot = f2 * (-90.0F - p.getXRot());
                            } else {
                                float f3 = p.isInWater() ? -90.0F - p.getXRot() : -90.0F;
                                xRot = Mth.lerp(f, 0.0F, f3);
                            }
                            xRot = -xRot;
                            Pos3D eyeAdjustments;
                            if (p.isFallFlying() && (p != player || !minecraft.options.getCameraType().isFirstPerson())) {
                                eyeAdjustments = new Pos3D(0, p.getEyeHeight(Pose.STANDING), 0).xRot(xRot).yRot(p.yBodyRot);
                            } else if (p.isVisuallySwimming()) {
                                eyeAdjustments = new Pos3D(0, p.getEyeHeight(), 0).xRot(xRot).yRot(p.yBodyRot).translate(0, 0.5, 0);
                            } else {
                                eyeAdjustments = new Pos3D(0, p.getEyeHeight(), 0).xRot(xRot).yRot(p.yBodyRot);
                            }
                            playerPos = new Pos3D(p.getX() + eyeAdjustments.x, p.getY() + eyeAdjustments.y, p.getZ() + eyeAdjustments.z);
                        }
                        Pos3D vLeft = new Pos3D(-0.43, -0.55, -0.54).xRot(xRot).yRot(p.yBodyRot);
                        renderJetpackSmoke(world, playerPos.translate(vLeft, playerMotion), vLeft.scale(0.2).translate(playerMotion, vLeft.scale(random)));
                        Pos3D vRight = new Pos3D(0.43, -0.55, -0.54).xRot(xRot).yRot(p.yBodyRot);
                        renderJetpackSmoke(world, playerPos.translate(vRight, playerMotion), vRight.scale(0.2).translate(playerMotion, vRight.scale(random)));
                        Pos3D vCenter = new Pos3D((world.random.nextFloat() - 0.5) * 0.4, -0.86, -0.30).xRot(xRot).yRot(p.yBodyRot);
                        renderJetpackSmoke(world, playerPos.translate(vCenter, playerMotion), vCenter.scale(0.2).translate(playerMotion));
                    }

                    //check players and do animations for idle flame throwers & scuba
                    if (world.getGameTime() % 4 == 0) {
                        if (p.isInWater() && Mekanism.playerState.isScubaMaskOn(p)) {
                            Pos3D vec = new Pos3D(0.4, 0.4, 0.4).multiply(p.getViewVector(1)).translate(0, -0.2, 0);
                            Pos3D motion = vec.scale(0.2).translate(p.getDeltaMovement());
                            Pos3D v = new Pos3D(p).translate(0, p.getEyeHeight(), 0).translate(vec);
                            world.addParticle(MekanismParticleTypes.SCUBA_BUBBLE.get(), v.x, v.y, v.z, motion.x, motion.y + 0.2, motion.z);
                        }
                        if (!p.swinging && !Mekanism.playerState.isFlamethrowerOn(p)) {
                            ItemStack currentItem = p.getMainHandItem();
                            if (!currentItem.isEmpty() && currentItem.getItem() instanceof ItemFlamethrower && ChemicalUtil.hasGas(currentItem)) {
                                Pos3D flameVec;
                                boolean rightHanded = p.getMainArm() == HumanoidArm.RIGHT;
                                if (player == p && minecraft.options.getCameraType().isFirstPerson()) {
                                    flameVec = new Pos3D(1, 1, 1)
                                          .multiply(p.getViewVector(partialTick))
                                          .yRot(rightHanded ? 15 : -15)
                                          .translate(0, p.getEyeHeight() - 0.1, 0);
                                } else {
                                    double flameXCoord = rightHanded ? -0.2 : 0.2;
                                    double flameYCoord = 1;
                                    double flameZCoord = 1.2;
                                    if (p.isCrouching()) {
                                        flameYCoord -= 0.65;
                                        flameZCoord -= 0.15;
                                    }
                                    flameVec = new Pos3D(flameXCoord, flameYCoord, flameZCoord).yRot(p.yBodyRot);
                                }
                                Vec3 motion = p.getDeltaMovement();
                                Vec3 flameMotion = new Vec3(motion.x(), p.onGround() ? 0 : motion.y(), motion.z());
                                Vec3 mergedVec = p.position().add(flameVec);
                                world.addParticle(MekanismParticleTypes.JETPACK_FLAME.get(), mergedVec.x, mergedVec.y, mergedVec.z, flameMotion.x,
                                      flameMotion.y, flameMotion.z);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean onBlockHover(WorldRenderContext context, BlockOutlineContext outline) {
        Player player = minecraft.player;
        if (player == null || !(minecraft.hitResult instanceof BlockHitResult rayTraceResult)) {
            return true;
        }
        if (rayTraceResult.getType() != Type.MISS) {
            Level world = player.level();
            BlockPos pos = outline.blockPos();
            MultiBufferSource renderer = Objects.requireNonNull(context.consumers());
            Camera info = context.camera();
            PoseStack matrix = context.matrixStack();
            ProfilerFiller profiler = context.profiler();
            BlockState blockState = outline.blockState();

            profiler.push(ProfilerConstants.AREA_MINE_OUTLINE);
            try {
                // Draw outlines for area mining blocks
                if (!outliningArea) {
                    ItemStack stack = player.getMainHandItem();
                    if (!stack.isEmpty() && stack.getItem() instanceof IBlastingItem tool) {
                        Map<BlockPos, BlockState> blocks = tool.getBlastedBlocks(world, player, stack, pos, blockState);
                        if (!blocks.isEmpty()) {
                            outliningArea = true;
                            try {
                                for (Entry<BlockPos, BlockState> block : blocks.entrySet()) {
                                    BlockPos blastingTarget = block.getKey();
                                    if (!pos.equals(blastingTarget) && world.getWorldBorder().isWithinBounds(blastingTarget)) {
                                        WorldBlockOutline.render(context, blastingTarget, block.getValue(), player, rayTraceResult);
                                    }
                                }
                            } finally {
                                outliningArea = false;
                            }
                        }
                    }
                }
            } finally {
                profiler.pop();
            }

            boolean shouldCancel = false;
            profiler.push(ProfilerConstants.MEKANISM_OUTLINE);
            try {
                if (!blockState.isAir() && world.getWorldBorder().isWithinBounds(pos)) {
                    BlockPos actualPos = pos;
                    BlockState actualState = blockState;
                    if (blockState.getBlock() instanceof BlockBounding) {
                        TileEntityBoundingBlock tile = WorldUtils.getTileEntity(TileEntityBoundingBlock.class, world, pos);
                        if (tile != null && tile.hasReceivedCoords()) {
                            actualPos = tile.getMainPos();
                            actualState = world.getBlockState(actualPos);
                        }
                    }
                    AttributeCustomSelectionBox customSelectionBox = Attribute.get(actualState, AttributeCustomSelectionBox.class);
                    if (customSelectionBox != null) {
                        WireFrameRenderer renderWireFrame = null;
                        if (customSelectionBox.isJavaModel()) {
                            //If we use a TER to render the wire frame, grab the tile
                            BlockEntity tile = WorldUtils.getTileEntity(world, actualPos);
                            if (tile != null) {
                                BlockEntityRenderer<BlockEntity> tileRenderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(tile);
                                if (tileRenderer instanceof IWireFrameRenderer wireFrameRenderer && wireFrameRenderer.hasSelectionBox(actualState)) {
                                    renderWireFrame = (buffer, matrixStack, state, red, green, blue, alpha) -> {
                                        if (wireFrameRenderer.isCombined()) {
                                            renderQuadsWireFrame(state, buffer, matrixStack.last().pose(), world.random, red, green, blue, alpha);
                                        }
                                        wireFrameRenderer.renderWireFrame(tile, context.tickDelta(), matrixStack, buffer, red, green, blue, alpha);
                                    };
                                }
                            }
                        } else {
                            //Otherwise, skip getting the tile and just grab the model
                            renderWireFrame = (buffer, matrixStack, state, red, green, blue, alpha) ->
                                  renderQuadsWireFrame(state, buffer, matrixStack.last().pose(), world.random, red, green, blue, alpha);
                        }
                        if (renderWireFrame != null) {
                            try (PoseStackScope ignored = new PoseStackScope(matrix)) {
                                Vec3 viewPosition = info.getPosition();
                                matrix.translate(actualPos.getX() - viewPosition.x, actualPos.getY() - viewPosition.y, actualPos.getZ() - viewPosition.z);
                                //0.4 Alpha
                                renderWireFrame.render(renderer.getBuffer(RenderType.lines()), matrix, actualState, 0, 0, 0, 0x66);
                            }
                            shouldCancel = true;
                        }
                    }
                }
            } finally {
                profiler.pop();
            }

            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty() || !(stack.getItem() instanceof ItemConfigurator)) {
                //If we are not holding a configurator, look if we are in the offhand
                stack = player.getOffhandItem();
                if (stack.isEmpty() || !(stack.getItem() instanceof ItemConfigurator)) {
                    return !shouldCancel;
                }
            }
            profiler.push(ProfilerConstants.CONFIGURABLE_MACHINE);
            try {
                ConfiguratorMode state = ((ItemConfigurator) stack.getItem()).getMode(stack);
                if (state.isConfigurating()) {
                    TransmissionType type = Objects.requireNonNull(state.getTransmission(), "Configurating state requires transmission type");
                    BlockEntity tile = WorldUtils.getTileEntity(world, pos);
                    if (tile instanceof ISideConfiguration configurable) {
                        TileComponentConfig config = configurable.getConfig();
                        if (config.supports(type)) {
                            Direction face = rayTraceResult.getDirection();
                            DataType dataType = config.getDataType(type, RelativeSide.fromDirections(configurable.getDirection(), face));
                            if (dataType != null) {
                                try (PoseStackScope ignored = new PoseStackScope(matrix)) {
                                    Vec3 viewPosition = info.getPosition();
                                    matrix.translate(pos.getX() - viewPosition.x, pos.getY() - viewPosition.y, pos.getZ() - viewPosition.z);
                                    MekanismRenderer.renderObject(getOverlayModel(face, type), matrix, renderer.getBuffer(Sheets.translucentCullBlockSheet()),
                                          MekanismRenderer.getColorARGB(dataType.getColor(), 0.6F), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, FaceDisplay.FRONT,
                                          info);
                                }
                            }
                        }
                    }
                }
            } finally {
                profiler.pop();
            }
            return !shouldCancel;
        }
        return true;
    }

    private void renderQuadsWireFrame(BlockState state, VertexConsumer buffer, Matrix4f matrix, RandomSource rand, int red, int green, int blue, int alpha) {
        List<Vertex[]> allVertices = cachedWireFrames.computeIfAbsent(state, s -> {
            BakedModel bakedModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(s);
            //TODO: Eventually we may want to add support for Model data and maybe render type
            List<Vertex[]> vertices = new ArrayList<>();
            for (Direction direction : EnumUtils.DIRECTIONS) {
                QuadUtils.unpack(bakedModel.getQuads(s, direction, rand)).stream().map(Quad::getVertices).forEach(vertices::add);
            }
            QuadUtils.unpack(bakedModel.getQuads(s, null, rand)).stream().map(Quad::getVertices).forEach(vertices::add);
            return vertices;
        });
        renderVertexWireFrame(allVertices, buffer, matrix, red, green, blue, alpha);
    }

    public static void renderVertexWireFrame(List<Vertex[]> allVertices, VertexConsumer buffer, Matrix4f matrix, int red, int green, int blue, int alpha) {
        for (Vertex[] vertices : allVertices) {
            Vector4f vertex = getVertex(matrix, vertices[0]);
            Vector3f normal = vertices[0].getNormal();
            Vector4f vertex2 = getVertex(matrix, vertices[1]);
            Vector3f normal2 = vertices[1].getNormal();
            Vector4f vertex3 = getVertex(matrix, vertices[2]);
            Vector3f normal3 = vertices[2].getNormal();
            Vector4f vertex4 = getVertex(matrix, vertices[3]);
            Vector3f normal4 = vertices[3].getNormal();
            buffer.vertex(vertex.x(), vertex.y(), vertex.z()).color(red, green, blue, alpha).normal(normal.x(), normal.y(), normal.z()).endVertex();
            buffer.vertex(vertex2.x(), vertex2.y(), vertex2.z()).color(red, green, blue, alpha).normal(normal2.x(), normal2.y(), normal2.z()).endVertex();

            buffer.vertex(vertex3.x(), vertex3.y(), vertex3.z()).color(red, green, blue, alpha).normal(normal3.x(), normal3.y(), normal3.z()).endVertex();
            buffer.vertex(vertex4.x(), vertex4.y(), vertex4.z()).color(red, green, blue, alpha).normal(normal4.x(), normal4.y(), normal4.z()).endVertex();

            buffer.vertex(vertex2.x(), vertex2.y(), vertex2.z()).color(red, green, blue, alpha).normal(normal2.x(), normal2.y(), normal2.z()).endVertex();
            buffer.vertex(vertex3.x(), vertex3.y(), vertex3.z()).color(red, green, blue, alpha).normal(normal3.x(), normal3.y(), normal3.z()).endVertex();

            buffer.vertex(vertex.x(), vertex.y(), vertex.z()).color(red, green, blue, alpha).normal(normal.x(), normal.y(), normal.z()).endVertex();
            buffer.vertex(vertex4.x(), vertex4.y(), vertex4.z()).color(red, green, blue, alpha).normal(normal4.x(), normal4.y(), normal4.z()).endVertex();
        }
    }

    private static Vector4f getVertex(Matrix4f matrix4f, Vertex vertex) {
        Vector4f vector4f = new Vector4f((float) vertex.getPos().x(), (float) vertex.getPos().y(), (float) vertex.getPos().z(), 1);
        return vector4f.mul(matrix4f);
    }

    private void renderJetpackSmoke(Level world, Vec3 pos, Vec3 motion) {
        world.addParticle(MekanismParticleTypes.JETPACK_FLAME.get(), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
        world.addParticle(MekanismParticleTypes.JETPACK_SMOKE.get(), pos.x, pos.y, pos.z, motion.x, motion.y, motion.z);
    }

    private Model3D getOverlayModel(Direction side, TransmissionType type) {
        return cachedOverlays.computeIfAbsent(side, s -> new EnumMap<>(TransmissionType.class))
              .computeIfAbsent(type, t -> new Model3D()
                    .setTexture(MekanismRenderer.overlays.get(t))
                    .prepSingleFaceModelSize(side)
              );
    }

    @FunctionalInterface
    private interface StageRenderer {

        void render(Camera camera, MultiBufferSource.BufferSource renderer, PoseStack poseStack, int renderTick, float partialTick);
    }

    @FunctionalInterface
    public interface LazyRender extends DelayedRenderQueue.RenderTask {
    }

    @FunctionalInterface
    private interface WireFrameRenderer {

        void render(VertexConsumer buffer, PoseStack matrix, BlockState state, int red, int green, int blue, int alpha);
    }
}
