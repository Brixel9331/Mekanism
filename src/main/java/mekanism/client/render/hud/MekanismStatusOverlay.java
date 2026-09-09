package mekanism.client.render.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import mekanism.client.gui.GuiUtils;
import mekanism.common.item.interfaces.IModeItem;
import mekanism.common.lib.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class MekanismStatusOverlay implements HudRenderCallback {

    public static final MekanismStatusOverlay INSTANCE = new MekanismStatusOverlay();

    private int modeSwitchTimer = 0;
    private long lastTick;

    private MekanismStatusOverlay() {
    }

    public void setTimer() {
        modeSwitchTimer = 100;
    }

    @Override
    public void render(Gui gui, GuiGraphics guiGraphics, float partialTicks, int screenWidth, int screenHeight, HudLayout layout) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.options.hideGui && modeSwitchTimer > 1 && minecraft.player != null) {
            ItemStack stack = minecraft.player.getMainHandItem();
            if (IModeItem.isModeItem(stack, EquipmentSlot.MAINHAND)) {
                Component scrollTextComponent = ((IModeItem) stack.getItem()).getScrollTextComponent(stack);
                if (scrollTextComponent != null) {
                    Color color = Color.rgbad(1, 1, 1, modeSwitchTimer / 100F);
                    Font font = gui.getFont();
                    int componentWidth = font.width(scrollTextComponent);
                    int targetShift = layout.statusOffset(minecraft.gameMode == null || minecraft.gameMode.canHurtPlayer(), gui.overlayMessageTime > 0);
                    PoseStack pose = guiGraphics.pose();
                    pose.pushPose();
                    pose.translate((screenWidth - componentWidth) / 2F, screenHeight - targetShift, 0);
                    GuiUtils.drawBackdrop(guiGraphics, minecraft, 0, 0, componentWidth, color.a());
                    guiGraphics.drawString(font, scrollTextComponent, 0, 0, color.argb());
                    pose.popPose();
                }
            }
            //Only decrement the switch timer once a tick
            if (lastTick != minecraft.player.level().getGameTime()) {
                lastTick = minecraft.player.level().getGameTime();
                modeSwitchTimer--;
            }
        }
    }
}