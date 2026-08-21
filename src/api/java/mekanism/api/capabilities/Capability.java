package mekanism.api.capabilities;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class Capability<T> {

    private final ResourceLocation name;
    private final BlockApiLookup<T, Direction> block;
    private final ItemApiLookup<T, Void> item;
    private final EntityApiLookup<T, Direction> entity;
    private final BooleanSupplier available;

    public Capability(ResourceLocation name, Class<T> type) {
        this(name, type, () -> true);
    }

    public Capability(ResourceLocation name, Class<T> type, BooleanSupplier available) {
        this.name = Objects.requireNonNull(name);
        this.available = Objects.requireNonNull(available);
        block = BlockApiLookup.get(name, type, Direction.class);
        item = ItemApiLookup.get(name, type, Void.class);
        entity = EntityApiLookup.get(name, type, Direction.class);
        block.registerFallback((level, pos, state, tile, side) -> resolveProvider(tile, side));
        item.registerFallback((stack, context) -> resolveProvider(stack, null));
        entity.registerFallback(this::resolveProvider);
    }

    @Nullable
    private T resolveProvider(@Nullable Object target, @Nullable Direction side) {
        return available.getAsBoolean() && target instanceof CapabilityProvider provider ? provider.getCapability(this, side).orElse(null) : null;
    }

    public ResourceLocation getName() {
        return name;
    }

    public boolean isAvailable() {
        return available.getAsBoolean();
    }

    public BlockApiLookup<T, Direction> block() {
        return block;
    }

    public ItemApiLookup<T, Void> item() {
        return item;
    }

    public EntityApiLookup<T, Direction> entity() {
        return entity;
    }

    public CapabilityReference<T> find(@Nullable Object target, @Nullable Direction side) {
        if (!isAvailable()) {
            return CapabilityReference.empty();
        }
        if (target instanceof ItemStack stack && stack.isEmpty() || target instanceof Entity targetEntity && targetEntity.isRemoved() ||
              target instanceof BlockEntity tile && tile.isRemoved()) {
            return CapabilityReference.empty();
        }
        if (target instanceof CapabilityProvider provider) {
            CapabilityReference<T> reference = provider.getCapability(this, side);
            if (reference.isPresent()) {
                return reference;
            }
        }
        if (target instanceof BlockEntity tile && tile.hasLevel() && !tile.isRemoved() && tile.getLevel().hasChunkAt(tile.getBlockPos())) {
            return CapabilityReference.fromLookup(() -> isAvailable() && !tile.isRemoved() && tile.hasLevel() &&
                  tile.getLevel().hasChunkAt(tile.getBlockPos()) && tile.getLevel().getBlockEntity(tile.getBlockPos()) == tile ?
                  block.find(tile.getLevel(), tile.getBlockPos(), tile.getBlockState(), tile, side) : null);
        }
        if (target instanceof ItemStack stack && !stack.isEmpty()) {
            return CapabilityReference.fromLookup(() -> isAvailable() && !stack.isEmpty() ? item.find(stack, null) : null);
        }
        if (target instanceof Entity targetEntity && !targetEntity.isRemoved()) {
            return CapabilityReference.fromLookup(() -> isAvailable() && !targetEntity.isRemoved() ? entity.find(targetEntity, side) : null);
        }
        return CapabilityReference.empty();
    }
}
