package mekanism.common.lib;

import java.io.File;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.function.Supplier;
import mekanism.api.MekanismAPI;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

public abstract class MekanismSavedData extends SavedData {

    public abstract void load(@NotNull CompoundTag nbt);

    @Override
    public void save(@NotNull File file) {
        if (isDirty()) {
            //This is loosely based on Refined Storage's RSSavedData's system of saving first to a temp file
            // to reduce the odds of corruption if the user's computer crashes while the file is being written
            Path target = file.toPath();
            Path temporary = target.resolveSibling(file.getName() + ".tmp");
            CompoundTag data = new CompoundTag();
            data.put("data", save(new CompoundTag()));
            NbtUtils.addCurrentDataVersion(data);
            try {
                NbtIo.writeCompressed(data, temporary.toFile());
                try {
                    Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (AtomicMoveNotSupportedException ignored) {
                    Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
                }
                setDirty(false);
            } catch (IOException failure) {
                MekanismAPI.logger.error("Failed to save {}", file, failure);
            }
        }
    }

    /**
     * Note: This should only be called from the server side
     */
    public static <DATA extends MekanismSavedData> DATA createSavedData(Supplier<DATA> createFunction, String name) {
        MinecraftServer currentServer = ServerLifecycle.getCurrentServer();
        if (currentServer == null) {
            throw new IllegalStateException("Current server is null");
        }
        DimensionDataStorage dataStorage = currentServer.overworld().getDataStorage();
        return createSavedData(dataStorage, createFunction, name);
    }

    /**
     * Note: This should only be called from the server side
     */
    public static <DATA extends MekanismSavedData> DATA createSavedData(DimensionDataStorage dataStorage, Supplier<DATA> createFunction, String name) {
        return dataStorage.computeIfAbsent(tag -> {
            DATA handler = createFunction.get();
            handler.load(tag);
            return handler;
        }, createFunction, MekanismAPI.MEKANISM_MODID + "_" + name);
    }
}
