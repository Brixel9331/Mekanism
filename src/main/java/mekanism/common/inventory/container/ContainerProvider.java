package mekanism.common.inventory.container;

import java.util.function.Consumer;
import mekanism.api.text.ILangEntry;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContainerProvider implements MenuProvider {

    private final Component displayName;
    private final MenuConstructor provider;

    public ContainerProvider(ILangEntry translationHelper, MenuConstructor provider) {
        this(translationHelper.translate(), provider);
    }

    public ContainerProvider(Component displayName, MenuConstructor provider) {
        this.displayName = displayName;
        this.provider = provider;
    }

    public static void open(ServerPlayer player, MenuProvider provider) {
        open(player, provider, buffer -> {});
    }

    public static void open(ServerPlayer player, MenuProvider provider, BlockPos pos) {
        open(player, provider, buffer -> buffer.writeBlockPos(pos));
    }

    public static void open(ServerPlayer player, MenuProvider provider, Consumer<FriendlyByteBuf> writer) {
        if (provider != null) {
            player.openMenu(withOpeningData(provider, writer));
        }
    }

    public static ExtendedScreenHandlerFactory withOpeningData(MenuProvider provider, Consumer<FriendlyByteBuf> writer) {
        return new ExtendedScreenHandlerFactory() {
            @Override
            public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buffer) {
                writer.accept(buffer);
            }

            @Override
            public Component getDisplayName() {
                return provider.getDisplayName();
            }

            @Override
            public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                return provider.createMenu(id, inventory, player);
            }
        };
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, @NotNull Inventory inv, @NotNull Player player) {
        return provider.createMenu(i, inv, player);
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return displayName;
    }
}