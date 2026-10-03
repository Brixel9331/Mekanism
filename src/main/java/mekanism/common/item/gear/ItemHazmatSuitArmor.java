package mekanism.common.item.gear;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.api.capabilities.CapabilityProvider;
import mekanism.api.capabilities.ItemCapabilityProvider;
import mekanism.common.Mekanism;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.radiation.item.RadiationShieldingHandler;
import mekanism.common.item.interfaces.IItemEnchantments;
import mekanism.common.item.interfaces.IItemStackProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

public class ItemHazmatSuitArmor extends ArmorItem implements IItemStackProperties, ItemCapabilityProvider, IItemEnchantments {

    private static final HazmatMaterial HAZMAT_MATERIAL = new HazmatMaterial();

    public ItemHazmatSuitArmor(ArmorItem.Type armorType, Properties properties) {
        super(HAZMAT_MATERIAL, armorType, properties.rarity(Rarity.UNCOMMON));
    }

    public static double getShieldingByArmor(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 0.25;
            case CHESTPLATE -> 0.4;
            case LEGGINGS -> 0.2;
            case BOOTS -> 0.15;
        };
    }

    @Override
    public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) {
        return IItemStackProperties.super.getDefaultTooltipHideFlags(stack) | TooltipPart.MODIFIERS.getMask();
    }

    @Override
    public CapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new ItemCapabilityWrapper(stack, RadiationShieldingHandler.create(item -> getShieldingByArmor(getType())));
    }

    @Override
    public boolean isEnchantable(@NotNull ItemStack stack) {
        return material.getEnchantmentValue() > 0 && super.isEnchantable(stack);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return isEnchantable(stack);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return isEnchantable(stack) && enchantment.category.canEnchant(this);
    }

    @NothingNullByDefault
    protected static class HazmatMaterial extends BaseSpecialArmorMaterial {

        @Override
        public String getName() {
            return Mekanism.MODID + ":hazmat";
        }
    }
}
