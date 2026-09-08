package mekanism.client.render.item;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class ItemDecorationRegistry {

    private static final Map<Item, List<ItemDecoration>> DECORATIONS = new IdentityHashMap<>();

    private ItemDecorationRegistry() {
    }

    public static void register(ItemLike item, ItemDecoration decoration) {
        Objects.requireNonNull(decoration);
        DECORATIONS.computeIfAbsent(item.asItem(), key -> new ArrayList<>()).add(decoration);
    }

    public static void render(GuiGraphics graphics, Font font, ItemStack stack, int x, int y) {
        if (stack.isEmpty()) {
            return;
        }
        List<ItemDecoration> decorations = DECORATIONS.get(stack.getItem());
        if (decorations != null) {
            graphics.flush();
            for (ItemDecoration decoration : decorations) {
                if (decoration.render(graphics, font, stack, x, y)) {
                    graphics.flush();
                }
            }
        }
    }
}
