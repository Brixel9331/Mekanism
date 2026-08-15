package mekanism.api.security;

import java.util.Objects;
import java.util.Optional;
import mekanism.api.MekanismAPI;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class SecurityLookup<T> {

    public static final SecurityLookup<IOwnerObject> OWNER = new SecurityLookup<>("owner", IOwnerObject.class);
    public static final SecurityLookup<ISecurityObject> SECURITY = new SecurityLookup<>("security", ISecurityObject.class);

    private final BlockApiLookup<T, Void> block;
    private final EntityApiLookup<T, Void> entity;
    private final ItemApiLookup<T, Void> item;

    private SecurityLookup(String name, Class<T> type) {
        ResourceLocation id = new ResourceLocation(MekanismAPI.MEKANISM_MODID, name);
        block = BlockApiLookup.get(id, type, Void.class);
        entity = EntityApiLookup.get(id, type, Void.class);
        item = ItemApiLookup.get(id, type, Void.class);
    }

    public BlockApiLookup<T, Void> block() {
        return block;
    }

    public EntityApiLookup<T, Void> entity() {
        return entity;
    }

    public ItemApiLookup<T, Void> item() {
        return item;
    }

    public Optional<T> find(@Nullable Object target) {
        if (target == null) {
            return Optional.empty();
        } else if (target instanceof ItemStack stack) {
            return stack.isEmpty() ? Optional.empty() : Optional.ofNullable(item.find(stack, null));
        } else if (target instanceof BlockEntity tile) {
            return Optional.ofNullable(block.find(Objects.requireNonNull(tile.getLevel(), "Security lookup requires a block entity level"),
                  tile.getBlockPos(), tile.getBlockState(), tile, null));
        } else if (target instanceof Entity value) {
            return Optional.ofNullable(entity.find(value, null));
        }
        throw new IllegalArgumentException("Security lookup requires an item stack, block entity, or entity: " + target.getClass().getName());
    }
}
