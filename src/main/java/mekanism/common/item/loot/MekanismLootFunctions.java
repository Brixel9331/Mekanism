package mekanism.common.item.loot;

import mekanism.common.Mekanism;
import mekanism.common.item.loot.PersonalStorageContentsLootFunction.PersonalStorageLootFunctionSerializer;
import mekanism.common.registration.DeferredRegister;
import mekanism.common.registration.RegistryObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;

public class MekanismLootFunctions {
    public static final DeferredRegister<LootItemFunctionType> REGISTER = new DeferredRegister<>(Mekanism.MODID, Registries.LOOT_FUNCTION_TYPE, () -> BuiltInRegistries.LOOT_FUNCTION_TYPE);

    public static final RegistryObject<LootItemFunctionType> PERSONAL_STORAGE_LOOT_FUNC = REGISTER.registerEntry("personal_storage_contents", () -> new LootItemFunctionType(new PersonalStorageLootFunctionSerializer()));
}
