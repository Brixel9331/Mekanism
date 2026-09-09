package mekanism.common.mixin.client;

import mekanism.client.render.hud.HudLayout;
import mekanism.client.render.hud.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private int screenHeight;

    @Shadow
    protected abstract Player getCameraPlayer();
    @Shadow
    protected abstract LivingEntity getPlayerVehicleWithHealth();
    @Shadow
    protected abstract int getVehicleMaxHearts(LivingEntity entity);
    @Shadow
    protected abstract int getVisibleVehicleHeartRows(int hearts);

    @Unique
    private final HudLayout mekanism$layout = new HudLayout();

    @Inject(method = "render", at = @At("HEAD"))
    private void mekanism$background(GuiGraphics graphics, float partialTick, CallbackInfo callback) {
        mekanism$layout.reset();
        HudRenderCallback.BACKGROUND.invoker().render((Gui) (Object) this, graphics, partialTick, graphics.guiWidth(), graphics.guiHeight(), mekanism$layout);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHotbar(FLnet/minecraft/client/gui/GuiGraphics;)V", shift = At.Shift.AFTER))
    private void mekanism$hotbar(GuiGraphics graphics, float partialTick, CallbackInfo callback) {
        HudRenderCallback.HOTBAR.invoker().render((Gui) (Object) this, graphics, partialTick, graphics.guiWidth(), graphics.guiHeight(), mekanism$layout);
    }

    @Inject(method = "renderHearts", at = @At("HEAD"))
    private void mekanism$healthHeight(GuiGraphics graphics, Player player, int x, int y, int rowHeight, int regeneration, float maximumHealth,
          int health, int displayHealth, int absorption, boolean blinking, CallbackInfo callback) {
        mekanism$layout.recordHealth(screenHeight - y, maximumHealth, absorption, rowHeight);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderVehicleHealth(Lnet/minecraft/client/gui/GuiGraphics;)V", shift = At.Shift.AFTER))
    private void mekanism$statusBars(GuiGraphics graphics, float partialTick, CallbackInfo callback) {
        Player player = getCameraPlayer();
        if (player != null) {
            int vehicleRows = getVisibleVehicleHeartRows(getVehicleMaxHearts(getPlayerVehicleWithHealth()));
            boolean survival = minecraft.gameMode != null && minecraft.gameMode.canHurtPlayer();
            boolean airVisible = player.isEyeInFluid(FluidTags.WATER) || player.getAirSupply() < player.getMaxAirSupply();
            mekanism$layout.recordRightSide(vehicleRows, survival, airVisible);
        }
        HudRenderCallback.STATUS_BARS.invoker().render((Gui) (Object) this, graphics, partialTick, graphics.guiWidth(), graphics.guiHeight(), mekanism$layout);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getSleepTimer()I", ordinal = 0))
    private void mekanism$selectedItem(GuiGraphics graphics, float partialTick, CallbackInfo callback) {
        HudRenderCallback.SELECTED_ITEM.invoker().render((Gui) (Object) this, graphics, partialTick, graphics.guiWidth(), graphics.guiHeight(), mekanism$layout);
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void mekanism$crosshair(GuiGraphics graphics, CallbackInfo callback) {
        if (!HudRenderCallback.CROSSHAIR.invoker().shouldRender(minecraft)) {
            callback.cancel();
        }
    }
}
