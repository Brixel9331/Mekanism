package mekanism.api.chemical;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.NBTConstants;
import mekanism.api.chemical.gas.Gas;
import mekanism.api.chemical.gas.GasBuilder;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.infuse.InfuseType;
import mekanism.api.chemical.infuse.InfuseTypeBuilder;
import mekanism.api.chemical.infuse.InfusionStack;
import mekanism.api.chemical.pigment.Pigment;
import mekanism.api.chemical.pigment.PigmentBuilder;
import mekanism.api.chemical.pigment.PigmentStack;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryBuilder;
import mekanism.api.chemical.slurry.SlurryStack;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.tags.TagKey;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

class ChemicalRegistryTest {

    private static List<Fixture<?>> fixtures;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        fixtures = List.of(
              fixture(ChemicalTags.GAS, MekanismAPI.EMPTY_GAS, () -> new Gas(GasBuilder.builder()), GasStack::new,
                    GasStack::readFromNBT, GasStack::readFromPacket, NBTConstants.GAS_NAME),
              fixture(ChemicalTags.INFUSE_TYPE, MekanismAPI.EMPTY_INFUSE_TYPE, () -> new InfuseType(InfuseTypeBuilder.builder()), InfusionStack::new,
                    InfusionStack::readFromNBT, InfusionStack::readFromPacket, NBTConstants.INFUSE_TYPE_NAME),
              fixture(ChemicalTags.PIGMENT, MekanismAPI.EMPTY_PIGMENT, () -> new Pigment(PigmentBuilder.builder()), PigmentStack::new,
                    PigmentStack::readFromNBT, PigmentStack::readFromPacket, NBTConstants.PIGMENT_NAME),
              fixture(ChemicalTags.SLURRY, MekanismAPI.EMPTY_SLURRY, () -> new Slurry(SlurryBuilder.clean()), SlurryStack::new,
                    SlurryStack::readFromNBT, SlurryStack::readFromPacket, NBTConstants.SLURRY_NAME)
        );
    }

    private static <C extends Chemical<C>> Fixture<C> fixture(ChemicalTags<C> tags, C empty, Supplier<C> factory,
          BiFunction<C, Long, ChemicalStack<C>> stackFactory, Function<CompoundTag, ChemicalStack<C>> nbtReader,
          Function<FriendlyByteBuf, ChemicalStack<C>> packetReader, String nbtName) {
        Registry<C> registry = tags.getRegistry();
        C first = Registry.register(registry, new ResourceLocation("test", registry.key().location().getPath() + "_first"), factory.get());
        C second = Registry.register(registry, new ResourceLocation("test", registry.key().location().getPath() + "_second"), factory.get());
        registry.freeze();
        return new Fixture<>(tags, empty, first, second, factory, stackFactory, nbtReader, packetReader, nbtName);
    }

    private static Stream<Fixture<?>> chemicals() {
        return fixtures.stream();
    }

    @ParameterizedTest
    @MethodSource("chemicals")
    <C extends Chemical<C>> void registersNativeSyncedRegistries(Fixture<C> fixture) {
        Registry<C> registry = fixture.tags.getRegistry();
        assertSame(registry, BuiltInRegistries.REGISTRY.get(registry.key().location()));
        assertTrue(RegistryAttributeHolder.get(registry.key()).hasAttribute(RegistryAttribute.SYNCED));
        assertSame(fixture.empty, registry.get(new ResourceLocation("mekanism", "empty")));
        assertSame(fixture.first, registry.get(fixture.first.getRegistryName()));
        assertEquals(fixture.first.getRegistryName(), registry.getResourceKey(fixture.first).orElseThrow().location());
        assertSame(fixture.first, fixture.tags.getHolder(fixture.first).orElseThrow().value());
    }

    @ParameterizedTest
    @MethodSource("chemicals")
    <C extends Chemical<C>> void rejectsUnregisteredObjectsDespiteDefaultRegistryFallback(Fixture<C> fixture) {
        C unknown = fixture.factory.get();
        assertNull(unknown.getRegistryName());
        assertTrue(fixture.tags.getHolder(unknown).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> fixture.stackFactory.apply(unknown, 1L));
        assertSame(fixture.empty, ChemicalUtils.readChemicalFromRegistry(new ResourceLocation("test", "missing"), fixture.empty, fixture.tags.getRegistry()));
    }

    @ParameterizedTest
    @MethodSource("chemicals")
    <C extends Chemical<C>> void preservesOriginalNbtNamesAndLongAmounts(Fixture<C> fixture) {
        CompoundTag saved = new CompoundTag();
        saved.putString(fixture.nbtName, fixture.first.getRegistryName().toString());
        saved.putLong(NBTConstants.AMOUNT, Long.MAX_VALUE);
        ChemicalStack<C> loaded = fixture.nbtReader.apply(saved);
        assertSame(fixture.first, loaded.getType());
        assertEquals(Long.MAX_VALUE, loaded.getAmount());
        assertEquals(saved, loaded.write(new CompoundTag()));
        ChemicalStack<C> copy = loaded.copy();
        copy.shrink(1);
        assertEquals(Long.MAX_VALUE, loaded.getAmount());
        assertEquals(Long.MAX_VALUE - 1, copy.getAmount());
    }

    @ParameterizedTest
    @MethodSource("chemicals")
    <C extends Chemical<C>> void invalidSavedNamesAndAmountsBecomeEmpty(Fixture<C> fixture) {
        assertTrue(fixture.nbtReader.apply(null).isEmpty());
        assertTrue(fixture.nbtReader.apply(new CompoundTag()).isEmpty());
        for (String name : List.of("test:missing", "invalid id", "mekanism:empty")) {
            CompoundTag saved = new CompoundTag();
            saved.putString(fixture.nbtName, name);
            saved.putLong(NBTConstants.AMOUNT, 1_000);
            assertTrue(fixture.nbtReader.apply(saved).isEmpty());
        }
        for (long amount : List.of(0L, -1L)) {
            CompoundTag saved = new CompoundTag();
            saved.putString(fixture.nbtName, fixture.first.getRegistryName().toString());
            saved.putLong(NBTConstants.AMOUNT, amount);
            assertTrue(fixture.nbtReader.apply(saved).isEmpty());
        }
    }

    @ParameterizedTest
    @MethodSource("chemicals")
    <C extends Chemical<C>> void nativePacketsKeepEmptyStacksAndFollowingFieldsAligned(Fixture<C> fixture) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            fixture.stackFactory.apply(fixture.first, Long.MAX_VALUE).writeToPacket(buffer);
            fixture.stackFactory.apply(fixture.empty, 0L).writeToPacket(buffer);
            fixture.stackFactory.apply(fixture.second, 123L).writeToPacket(buffer);
            buffer.writeInt(54321);
            ChemicalStack<C> first = fixture.packetReader.apply(buffer);
            assertSame(fixture.first, first.getType());
            assertEquals(Long.MAX_VALUE, first.getAmount());
            assertTrue(fixture.packetReader.apply(buffer).isEmpty());
            ChemicalStack<C> second = fixture.packetReader.apply(buffer);
            assertSame(fixture.second, second.getType());
            assertEquals(123, second.getAmount());
            assertEquals(54321, buffer.readInt());
            assertFalse(buffer.isReadable());
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @MethodSource("chemicals")
    <C extends Chemical<C>> void rejectsUnknownPacketIds(Fixture<C> fixture) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        try {
            buffer.writeVarInt(Integer.MAX_VALUE);
            assertThrows(DecoderException.class, () -> fixture.packetReader.apply(buffer));
        } finally {
            buffer.release();
        }
    }

    @ParameterizedTest
    @MethodSource("chemicals")
    <C extends Chemical<C>> void tagReloadUpdatesExistingChemicalsAndKeepsEmptyTypesUntagged(Fixture<C> fixture) {
        Registry<C> registry = fixture.tags.getRegistry();
        TagKey<C> key = fixture.tags.tag(new ResourceLocation("test", "reload"));
        Holder<C> first = fixture.tags.getHolder(fixture.first).orElseThrow();
        Holder<C> second = fixture.tags.getHolder(fixture.second).orElseThrow();
        Holder<C> empty = fixture.tags.getHolder(fixture.empty).orElseThrow();
        try {
            assertFalse(fixture.first.is(key));
            registry.bindTags(Map.of(key, List.of(first, empty)));
            assertTrue(fixture.first.is(key));
            assertFalse(fixture.second.is(key));
            assertEquals(List.of(key), fixture.first.getTags().toList());
            assertFalse(fixture.empty.is(key));
            assertEquals(0, fixture.empty.getTags().count());
            registry.bindTags(Map.of(key, List.of(second)));
            assertFalse(fixture.first.is(key));
            assertTrue(fixture.second.is(key));
            assertEquals(0, fixture.first.getTags().count());
        } finally {
            registry.resetTags();
        }
    }

    private record Fixture<C extends Chemical<C>>(ChemicalTags<C> tags, C empty, C first, C second, Supplier<C> factory,
          BiFunction<C, Long, ChemicalStack<C>> stackFactory, Function<CompoundTag, ChemicalStack<C>> nbtReader,
          Function<FriendlyByteBuf, ChemicalStack<C>> packetReader, String nbtName) {
    }
}
