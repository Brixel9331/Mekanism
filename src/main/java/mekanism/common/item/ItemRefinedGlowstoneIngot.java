package mekanism.common.item;

import mekanism.common.item.interfaces.IPiglinCurrency;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemRefinedGlowstoneIngot extends Item implements IPiglinCurrency {

    public ItemRefinedGlowstoneIngot(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isPiglinCurrency(@NotNull ItemStack stack) {
        return true;
    }
}