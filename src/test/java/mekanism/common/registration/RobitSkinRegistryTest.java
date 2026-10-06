package mekanism.common.registration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import java.util.List;
import mekanism.api.MekanismAPI;
import mekanism.api.robit.AdvancementBasedRobitSkin;
import mekanism.api.robit.BasicRobitSkin;
import mekanism.api.robit.RobitSkin;
import mekanism.api.robit.RobitSkinSerializationHelper;
import mekanism.common.registration.impl.RobitSkinDeferredRegister;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.SharedConstants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RobitSkinRegistryTest {

    private static final RobitSkinDeferredRegister SKINS = new RobitSkinDeferredRegister("test");
    private static Codec<RobitSkin> dataCodec;

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SKINS.registerSerializer("basic", () -> RobitSkinSerializationHelper.NETWORK_CODEC);
        SKINS.registerSerializer("advancement", () -> RobitSkinSerializationHelper.ADVANCEMENT_BASED_ROBIT_SKIN_CODEC);
        dataCodec = SKINS.createAndRegisterDatapack(RobitSkinSerializationHelper.NETWORK_CODEC);
    }

    @Test
    void dataPackRegistryUsesTheNamespacedSkinKeyAndIsRegisteredOnce() {
        assertEquals(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME.location(), SKINS.dataKey("allay").registry());
        assertEquals(new ResourceLocation("test", "allay"), SKINS.dataKey("allay").location());
        assertSame(dataCodec, SKINS.createAndRegisterDatapack(RobitSkinSerializationHelper.NETWORK_CODEC));
        assertEquals(1, DynamicRegistries.getDynamicRegistries().stream().filter(entry -> entry.key().equals(MekanismAPI.ROBIT_SKIN_REGISTRY_NAME)).count());
    }

    @Test
    void diskCodecPreservesUnlockConditionsAndCustomModels() {
        RobitSkin skin = new AdvancementBasedRobitSkin(List.of(new ResourceLocation("test", "allay")),
              new ResourceLocation("test", "item/robit"), new ResourceLocation("minecraft", "adventure/allay_deliver_item_to_player"));
        JsonObject json = dataCodec.encodeStart(JsonOps.INSTANCE, skin).getOrThrow(false, message -> fail(message)).getAsJsonObject();
        assertEquals("test:advancement", json.get("type").getAsString());
        assertTrue(json.has("advancement"));
        assertEquals(skin, dataCodec.parse(JsonOps.INSTANCE, json).getOrThrow(false, message -> fail(message)));
    }

    @Test
    void networkCodecOnlyTransmitsAppearance() {
        RobitSkin skin = new AdvancementBasedRobitSkin(List.of(new ResourceLocation("test", "allay")), null,
              new ResourceLocation("minecraft", "adventure/allay_deliver_item_to_player"));
        JsonElement json = RobitSkinSerializationHelper.NETWORK_CODEC.encodeStart(JsonOps.INSTANCE, skin).getOrThrow(false, message -> fail(message));
        assertFalse(json.getAsJsonObject().has("advancement"));
        RobitSkin clientSkin = RobitSkinSerializationHelper.NETWORK_CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, message -> fail(message));
        assertInstanceOf(BasicRobitSkin.class, clientSkin);
        assertEquals(skin.textures(), clientSkin.textures());
        assertNull(clientSkin.customModel());
    }

    @Test
    void missingSerializersAndEmptyTexturesAreRejected() {
        JsonObject unknown = new JsonObject();
        unknown.addProperty("type", "test:missing");
        assertTrue(dataCodec.parse(JsonOps.INSTANCE, unknown).error().isPresent());
        JsonObject empty = new JsonObject();
        empty.addProperty("type", "test:basic");
        empty.add("textures", new com.google.gson.JsonArray());
        assertTrue(dataCodec.parse(JsonOps.INSTANCE, empty).error().isPresent());
    }
}
