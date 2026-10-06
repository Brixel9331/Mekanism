package mekanism.common.registration;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import mekanism.api.MekanismAPI;
import mekanism.api.MekanismIMC;
import mekanism.api.gear.EnchantmentBasedModule;
import mekanism.api.gear.ICustomModule;
import mekanism.api.gear.ModuleData;
import mekanism.api.providers.IModuleDataProvider;
import mekanism.common.registration.impl.ModuleDeferredRegister;
import mekanism.common.registration.impl.ModuleRegistryObject;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModuleRegistryTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void nativeModuleRegistrationPreservesFactoriesAndMetadata() {
        ModuleDeferredRegister modules = new ModuleDeferredRegister("test");
        AtomicInteger created = new AtomicInteger();
        ModuleRegistryObject<TestModule> custom = modules.register("custom", () -> {
            created.incrementAndGet();
            return new TestModule();
        }, () -> Items.DIAMOND, builder -> builder.maxStackSize(4).rarity(Rarity.RARE).rendersHUD());
        ModuleRegistryObject<?> marker = modules.registerMarker("marker", () -> Items.EMERALD, builder -> builder.noDisable());
        ModuleRegistryObject<?> enchantment = modules.registerEnchantBased("enchantment", () -> Enchantments.SILK_TOUCH,
              () -> Items.BOOK, builder -> builder);
        assertThrows(IllegalStateException.class, custom::get);
        assertEquals(0, created.get());
        modules.register();
        assertEquals(0, created.get());
        assertSame(custom.get(), MekanismAPI.moduleRegistry().get(custom.getId()));
        assertEquals(custom.key(), MekanismAPI.moduleRegistry().getResourceKey(custom.get()).orElseThrow());
        assertEquals("module.test.custom", custom.get().getTranslationKey());
        assertEquals(4, custom.get().getMaxStackSize());
        assertEquals(Rarity.RARE, custom.get().getRarity());
        assertTrue(custom.get().rendersHUD());
        assertSame(Items.DIAMOND, custom.get().getItemProvider().asItem());
        assertNotSame(custom.get().get(), custom.get().get());
        assertEquals(2, created.get());
        assertTrue(marker.get().isNoDisable());
        assertSame(marker.get().get(), marker.get().get());
        assertSame(Enchantments.SILK_TOUCH, ((EnchantmentBasedModule<?>) enchantment.get().get()).getEnchantment());
    }

    @Test
    void supportDeclarationsRemainLazyAndIgnoreDuplicates() {
        ModuleDeferredRegister modules = new ModuleDeferredRegister("test");
        ModuleRegistryObject<TestModule> custom = modules.register("supported", TestModule::new, () -> Items.DIAMOND);
        MekanismIMC.addMekaToolModules(custom);
        MekanismIMC.addMekaToolModules(custom);
        List<IModuleDataProvider<?>> support = MekanismIMC.getModulesFor(MekanismIMC.ADD_MEKA_TOOL_MODULES);
        assertEquals(1, support.stream().filter(provider -> provider == custom).count());
        assertThrows(IllegalStateException.class, custom::get);
        assertThrows(UnsupportedOperationException.class, () -> support.add(custom));
        modules.register();
        assertSame(custom.get(), support.stream().filter(provider -> provider == custom).findFirst().orElseThrow().getModuleData());
    }

    @Test
    void allEquipmentDeclarationsCopyTheInputArray() {
        ModuleData<TestModule> first = new ModuleData<>(ModuleData.ModuleDataBuilder.custom(TestModule::new, () -> Items.DIAMOND));
        ModuleData<TestModule> second = new ModuleData<>(ModuleData.ModuleDataBuilder.custom(TestModule::new, () -> Items.EMERALD));
        IModuleDataProvider<?>[] providers = {first};
        MekanismIMC.addModulesToAll(providers);
        providers[0] = second;
        for (String target : List.of(MekanismIMC.ADD_MEKA_TOOL_MODULES, MekanismIMC.ADD_MEKA_SUIT_HELMET_MODULES,
              MekanismIMC.ADD_MEKA_SUIT_BODYARMOR_MODULES, MekanismIMC.ADD_MEKA_SUIT_PANTS_MODULES, MekanismIMC.ADD_MEKA_SUIT_BOOTS_MODULES)) {
            assertTrue(MekanismIMC.getModulesFor(target).contains(first));
            assertFalse(MekanismIMC.getModulesFor(target).contains(second));
        }
    }

    @Test
    void invalidSupportArraysCannotPartiallyRegister() {
        ModuleData<TestModule> module = new ModuleData<>(ModuleData.ModuleDataBuilder.custom(TestModule::new, () -> Items.DIAMOND));
        assertThrows(IllegalArgumentException.class, () -> MekanismIMC.addMekaToolModules());
        assertThrows(IllegalArgumentException.class, () -> MekanismIMC.addMekaToolModules((IModuleDataProvider<?>[]) null));
        assertThrows(NullPointerException.class, () -> MekanismIMC.addMekaToolModules(module, null));
        assertFalse(MekanismIMC.getModulesFor(MekanismIMC.ADD_MEKA_TOOL_MODULES).contains(module));
    }

    private static final class TestModule implements ICustomModule<TestModule> {
    }
}
