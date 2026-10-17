package mekanism.tools.common.material;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

record MekanismArmorMaterial(BaseMekanismMaterial material) implements ArmorMaterial {

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return material.getDurabilityForType(type);
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return material.getDefenseForType(type);
    }

    @Override
    public int getEnchantmentValue() {
        return material.getArmorEnchantability();
    }

    @Override
    public SoundEvent getEquipSound() {
        return material.getEquipSound();
    }

    @Override
    public Ingredient getRepairIngredient() {
        return material.getArmorRepairMaterial();
    }

    @Override
    public String getName() {
        return material.getName();
    }

    @Override
    public float getToughness() {
        return material.getToughness();
    }

    @Override
    public float getKnockbackResistance() {
        return material.getKnockbackResistance();
    }
}
