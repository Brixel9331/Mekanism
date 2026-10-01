package mekanism.common.integration.gender;

import com.wildfire.api.WildfireAPI;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;

public class GenderCapabilityHelper {

    public static void register() {
        register("mekanism", "hazmat_gown", MekanismGenderArmor.HAZMAT);
        register("mekanism", "jetpack", MekanismGenderArmor.OPEN_FRONT);
        register("mekanism", "scuba_tank", MekanismGenderArmor.OPEN_FRONT);
        register("mekanism", "jetpack_armored", MekanismGenderArmor.HIDES_BREASTS);
        register("mekanism", "mekasuit_bodyarmor", MekanismGenderArmor.HIDES_BREASTS);
        register("mekanismtools", "bronze_chestplate", new MekanismGenderArmor(0.9F));
        register("mekanismtools", "lapis_lazuli_chestplate", new MekanismGenderArmor(0.6F, 0.1F));
        register("mekanismtools", "osmium_chestplate", new MekanismGenderArmor(1));
        register("mekanismtools", "refined_glowstone_chestplate", new MekanismGenderArmor(0.95F));
        register("mekanismtools", "refined_obsidian_chestplate", new MekanismGenderArmor(1));
        register("mekanismtools", "steel_chestplate", new MekanismGenderArmor(1));
    }

    private static void register(String namespace, String path, MekanismGenderArmor settings) {
        BuiltInRegistries.ITEM.getOptional(new ResourceLocation(namespace, path))
              .filter(item -> item instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.CHESTPLATE)
              .ifPresent(item -> WildfireAPI.addGenderArmor(item, settings));
    }
}
