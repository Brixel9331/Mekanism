package mekanism.client.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

public final class GuiRenderHelper {

    private GuiRenderHelper() {
    }

    public static int drawString(GuiGraphics graphics, Font font, Component text, float x, float y, int color, boolean shadow) {
        int end = font.drawInBatch(text.getVisualOrderText(), x, y, color, shadow, graphics.pose().last().pose(), graphics.bufferSource(),
              Font.DisplayMode.NORMAL, 0, 0xF000F0);
        graphics.flushIfUnmanaged();
        return end;
    }

    public static void renderEntity(GuiGraphics graphics, int x, int y, int scale, float yaw, float pitch, LivingEntity entity) {
        Quaternionf cameraRotation = new Quaternionf().rotationX(pitch * 20 * ((float) Math.PI / 180));
        Quaternionf rotation = new Quaternionf().rotationZ((float) Math.PI).mul(cameraRotation);
        float bodyRotation = entity.yBodyRot;
        float yRotation = entity.getYRot();
        float xRotation = entity.getXRot();
        float previousHeadRotation = entity.yHeadRotO;
        float headRotation = entity.yHeadRot;
        try {
            entity.yBodyRot = 180 + yaw * 20;
            entity.setYRot(180 + yaw * 40);
            entity.setXRot(-pitch * 20);
            entity.yHeadRot = entity.getYRot();
            entity.yHeadRotO = entity.getYRot();
            InventoryScreen.renderEntityInInventory(graphics, x, y, scale, rotation, cameraRotation, entity);
        } finally {
            entity.yBodyRot = bodyRotation;
            entity.setYRot(yRotation);
            entity.setXRot(xRotation);
            entity.yHeadRotO = previousHeadRotation;
            entity.yHeadRot = headRotation;
        }
    }
}
