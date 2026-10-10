package mekanism.common.loot.table;

import io.github.fabricators_of_create.porting_lib.data.ModdedEntityLootSubProvider;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Set;
import java.util.stream.Stream;
import mekanism.api.providers.IEntityTypeProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.NotNull;

public abstract class BaseEntityLootTables extends ModdedEntityLootSubProvider {

    private final Set<EntityType<?>> knownEntityTypes = new ReferenceOpenHashSet<>();

    protected BaseEntityLootTables() {
        super(FeatureFlags.VANILLA_SET);
    }

    @Override
    public void add(@NotNull EntityType<?> type, @NotNull LootTable.Builder table) {
        //Overwrite the core register method to add to our list of known entity types
        //Note: This isn't the actual core method as that one takes a ResourceLocation, but all our things wil pass through this one
        super.add(type, table);
        knownEntityTypes.add(type);
    }

    @NotNull
    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return knownEntityTypes.stream();
    }

    public void add(@NotNull IEntityTypeProvider typeProvider, @NotNull LootTable.Builder table) {
        add(typeProvider.getEntityType(), table);
    }
}