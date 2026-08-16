package mekanism.common.inventory.container.type;

import mekanism.common.inventory.container.entity.IEntityContainer;
import mekanism.common.inventory.container.type.MekanismContainerType.IMekanismContainerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType.ExtendedFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MekanismContainerType<T, CONTAINER extends AbstractContainerMenu> extends BaseMekanismContainerType<T, CONTAINER, IMekanismContainerFactory<T, CONTAINER>> {

    public static <TILE extends BlockEntity, CONTAINER extends AbstractContainerMenu> MekanismContainerType<TILE, CONTAINER> tile(Class<TILE> type,
          IMekanismContainerFactory<TILE, CONTAINER> constructor) {
        return new MekanismContainerType<>(type, constructor, (id, inv, buf) -> constructor.create(id, inv, getTileFromBuf(buf, inv, type)));
    }

    public static <TILE extends BlockEntity, CONTAINER extends AbstractContainerMenu> MekanismContainerType<TILE, CONTAINER> tile(Class<TILE> type,
          IMekanismSidedContainerFactory<TILE, CONTAINER> constructor) {
        return new MekanismContainerType<>(type, constructor, (id, inv, buf) -> constructor.create(id, inv, getTileFromBuf(buf, inv, type), true));
    }

    public static <ENTITY extends Entity, CONTAINER extends AbstractContainerMenu & IEntityContainer<ENTITY>> MekanismContainerType<ENTITY, CONTAINER> entity(Class<ENTITY> type,
          IMekanismContainerFactory<ENTITY, CONTAINER> constructor) {
        return new MekanismContainerType<>(type, constructor, (id, inv, buf) -> constructor.create(id, inv, getEntityFromBuf(buf, inv, type)));
    }

    public static <ENTITY extends Entity, CONTAINER extends AbstractContainerMenu & IEntityContainer<ENTITY>> MekanismContainerType<ENTITY, CONTAINER> entity(Class<ENTITY> type,
          IMekanismSidedContainerFactory<ENTITY, CONTAINER> constructor) {
        return new MekanismContainerType<>(type, constructor, (id, inv, buf) -> constructor.create(id, inv, getEntityFromBuf(buf, inv, type), true));
    }

    protected MekanismContainerType(Class<T> type, IMekanismContainerFactory<T, CONTAINER> mekanismConstructor, ExtendedFactory<CONTAINER> constructor) {
        super(type, mekanismConstructor, constructor);
    }

    @Nullable
    public CONTAINER create(int id, Inventory inv, Object data) {
        if (type.isInstance(data)) {
            return mekanismConstructor.create(id, inv, type.cast(data));
        }
        return null;
    }

    @Nullable
    public MenuConstructor create(Object data) {
        if (type.isInstance(data)) {
            T d = type.cast(data);
            return (id, inv, player) -> mekanismConstructor.create(id, inv, d);
        }
        return null;
    }

    @NotNull
    private static <TILE extends BlockEntity> TILE getTileFromBuf(FriendlyByteBuf buf, Inventory inventory, Class<TILE> type) {
        if (buf == null) {
            throw new IllegalArgumentException("Null packet buffer");
        } else if (!inventory.player.level().isClientSide) {
            throw new UnsupportedOperationException("This method is only supported on the client.");
        }
        BlockPos pos = buf.readBlockPos();
        BlockEntity tile = inventory.player.level().getBlockEntity(pos);
        if (!type.isInstance(tile)) {
            throw new IllegalStateException("Client could not locate tile at " + pos + " for tile container. "
                                            + "This is likely caused by a mod breaking client side tile lookup");
        }
        return type.cast(tile);
    }

    @NotNull
    private static <ENTITY extends Entity> ENTITY getEntityFromBuf(FriendlyByteBuf buf, Inventory inventory, Class<ENTITY> type) {
        if (buf == null) {
            throw new IllegalArgumentException("Null packet buffer");
        } else if (!inventory.player.level().isClientSide) {
            throw new UnsupportedOperationException("This method is only supported on the client.");
        }
        int entityId = buf.readVarInt();
        Entity e = inventory.player.level().getEntity(entityId);
        if (type.isInstance(e)) {
            //noinspection unchecked
            return (ENTITY) e;
        }
        throw new IllegalStateException("Client could not locate entity (id: " + entityId + ")  for entity container or the entity was of an invalid type. "
                                        + "This is likely caused by a mod breaking client side entity lookup.");
    }

    @FunctionalInterface
    public interface IMekanismContainerFactory<T, CONTAINER extends AbstractContainerMenu> {

        CONTAINER create(int id, Inventory inv, T data);
    }

    @FunctionalInterface
    public interface IMekanismSidedContainerFactory<T, CONTAINER extends AbstractContainerMenu> extends IMekanismContainerFactory<T, CONTAINER> {


        CONTAINER create(int id, Inventory inv, T data, boolean remote);

        @Override
        default CONTAINER create(int id, Inventory inv, T data) {
            return create(id, inv, data, false);
        }
    }
}