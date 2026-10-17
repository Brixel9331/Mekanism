package mekanism.tools.common.item;

import mekanism.tools.common.material.MaterialCreator;
import mekanism.common.item.interfaces.IEquipmentProtection;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemRefinedGlowstoneArmor extends ItemMekanismArmor implements IEquipmentProtection {

    public ItemRefinedGlowstoneArmor(MaterialCreator material, ArmorItem.Type armorType, Properties properties) {
        super(material, armorType, properties);
    }


    @Override
    public boolean makesPiglinsNeutral(@NotNull ItemStack stack, @NotNull LivingEntity wearer) {
        return true;
    }
}