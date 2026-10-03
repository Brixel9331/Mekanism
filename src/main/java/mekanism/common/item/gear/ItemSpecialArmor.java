package mekanism.common.item.gear;

import java.util.ArrayList;
import java.util.List;
import mekanism.api.capabilities.CapabilityProvider;
import mekanism.api.capabilities.ItemCapabilityProvider;
import mekanism.common.capabilities.ItemCapabilityWrapper;
import mekanism.common.capabilities.ItemCapabilityWrapper.ItemCapability;
import mekanism.common.item.interfaces.IItemEnchantments;
import mekanism.common.item.interfaces.IItemStackProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

public abstract class ItemSpecialArmor extends ArmorItem implements IItemStackProperties, ItemCapabilityProvider, IItemEnchantments {

    protected ItemSpecialArmor(ArmorMaterial material, ArmorItem.Type armorType, Properties properties) {
        super(material, armorType, properties);
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return "mekanism:render/null_armor.png";
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

    protected boolean areCapabilityConfigsLoaded() {
        return true;
    }

    protected void gatherCapabilities(List<ItemCapability> capabilities, ItemStack stack, CompoundTag nbt) {
    }

    @Override
    public final CapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        if (!areCapabilityConfigsLoaded()) {
            //Only expose the capabilities if the required configs are loaded
            return null;
        }
        List<ItemCapability> capabilities = new ArrayList<>();
        gatherCapabilities(capabilities, stack, nbt);
        if (capabilities.isEmpty()) {
            return null;
        }
        return new ItemCapabilityWrapper(stack, capabilities.toArray(ItemCapability[]::new));
    }
}
