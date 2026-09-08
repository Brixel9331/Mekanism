package mekanism.client.render.item;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ItemDecoration {

    boolean render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y);
}
