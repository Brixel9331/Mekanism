package mekanism.common;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import mekanism.client.lang.MekanismLangProvider;
import mekanism.client.model.MekanismItemModelProvider;
import mekanism.client.sound.MekanismSoundProvider;
import mekanism.client.state.MekanismBlockStateProvider;
import mekanism.client.texture.MekanismSpriteSourceProvider;
import mekanism.client.texture.PrideRobitTextureProvider;
import mekanism.common.advancements.MekanismAdvancementProvider;
import mekanism.common.advancements.MekanismCriteriaTriggers;
import mekanism.common.integration.computer.ComputerHelpProvider;
import mekanism.common.integration.crafttweaker.MekanismCrTExampleProvider;
import mekanism.common.loot.MekanismLootProvider;
import mekanism.common.recipe.impl.MekanismRecipeProvider;
import mekanism.common.registries.MekanismDatapackRegistryProvider;
import mekanism.common.tag.MekanismTagProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

public class MekanismDataGenerator implements DataGeneratorEntrypoint {

    public MekanismDataGenerator() {
        MekanismDataGenerator.bootstrapConfigs(Mekanism.MODID);
    }

    @Override
    public String getEffectiveModId() {
        return Mekanism.MODID;
    }

    @Override
    public void buildRegistry(net.minecraft.core.RegistrySetBuilder builder) {
        MekanismDatapackRegistryProvider.buildRegistry(builder);
    }

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        FabricDataGenerator.Pack pack = generator.createPack();
        ExistingFileHelper existingFileHelper = MekanismDataGenerator.createFileHelper();
        pack.addProvider((output, registries) -> new MekanismDatapackRegistryProvider(output, registries));
        //Bootstrap our advancement triggers as common setup doesn't run
        MekanismCriteriaTriggers.init();
        pack.addProvider((output, registries) -> new BasePackMetadataGenerator(output, MekanismLang.PACK_DESCRIPTION));
        //Client side data generators
        pack.addProvider((output, registries) -> new MekanismLangProvider(output));
        pack.addProvider((output, registries) -> new PrideRobitTextureProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new MekanismSoundProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new MekanismSpriteSourceProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new MekanismItemModelProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new MekanismBlockStateProvider(output, existingFileHelper));
        //Server side data generators
        pack.addProvider((output, registries) -> new MekanismTagProvider(output, registries));
        pack.addProvider((output, registries) -> new MekanismLootProvider(output));
        MekanismRecipeProvider recipeProvider = pack.addProvider((output, registries) -> new MekanismRecipeProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new MekanismAdvancementProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new MekanismCrTExampleProvider(output, existingFileHelper));
        pack.addProvider((output, registries) -> new ComputerHelpProvider(output, Mekanism.MODID));
        //Data generator to help with persisting data when porting across MC versions when optional deps aren't updated yet
        // DO NOT ADD OTHERS AFTER THIS ONE
        pack.addProvider((output, registries) -> new PersistingDisabledProvidersProvider(output, recipeProvider.getDisabledCompats()));
    }

    public static ExistingFileHelper createFileHelper() {
        var mods = FabricLoader.getInstance().getAllMods();
        Path[] roots = mods.stream().flatMap(mod -> mod.getRootPaths().stream())
              .filter(path -> path.getFileSystem() == FileSystems.getDefault()).toArray(Path[]::new);
        Set<String> packedMods = mods.stream()
              .filter(mod -> mod.getRootPaths().stream().anyMatch(path -> path.getFileSystem() != FileSystems.getDefault()))
              .map(mod -> mod.getMetadata().getId()).collect(Collectors.toSet());
        return ExistingFileHelper.withResources(packedMods, roots);
    }

    /**
     * Used to bootstrap configs to their default values so that if we are querying if things exist we don't have issues with it happening to early or in cases we have
     * fake tiles.
     */
    public static void bootstrapConfigs(String modid) {
        ConfigTracker.INSTANCE.configSets().forEach((type, configs) -> {
            for (ModConfig config : configs) {
                if (config.getModId().equals(modid)) {
                    //Similar to how ConfigTracker#loadDefaultServerConfigs works for loading default server configs on the client
                    // except we don't bother firing an event as it is private, and we are already at defaults if we had called earlier,
                    // and we also don't fully initialize the mod config as the spec is what we care about, and we can do so without having
                    // to reflect into package private methods
                    CommentedConfig commentedConfig = CommentedConfig.inMemory();
                    config.getSpec().correct(commentedConfig);
                    config.getSpec().acceptConfig(commentedConfig);
                }
            }
        });
    }

    /**
     * Basically a copy of {@link DataProvider#saveStable(CachedOutput, JsonElement, Path)} but it takes a consumer of the output stream instead of serializes json using GSON.
     * Use it to write arbitrary files.
     */
    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    public static CompletableFuture<?> save(CachedOutput cache, IOConsumer<OutputStream> osConsumer, Path path) {
        return CompletableFuture.runAsync(() -> {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                 HashingOutputStream hashingOutputStream = new HashingOutputStream(Hashing.sha1(), outputStream)) {
                osConsumer.accept(hashingOutputStream);
                cache.writeIfNeeded(path, outputStream.toByteArray(), hashingOutputStream.hash());
            } catch (IOException ioexception) {
                DataProvider.LOGGER.error("Failed to save file to {}", path, ioexception);
            }
        }, Util.backgroundExecutor());
    }

    @FunctionalInterface
    public interface IOConsumer<T> {
        void accept(T value) throws IOException;
    }
}