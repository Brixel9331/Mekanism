package mekanism.additions.common.registries;

import mekanism.additions.common.MekanismAdditions;
import mekanism.additions.common.entity.EntityBalloon;
import mekanism.additions.common.entity.EntityObsidianTNT;
import mekanism.additions.common.entity.baby.EntityBabyCreeper;
import mekanism.additions.common.entity.baby.EntityBabyEnderman;
import mekanism.additions.common.entity.baby.EntityBabySkeleton;
import mekanism.additions.common.entity.baby.EntityBabyStray;
import mekanism.additions.common.entity.baby.EntityBabyWitherSkeleton;
import mekanism.common.registration.impl.EntityTypeDeferredRegister;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;

public class AdditionsEntityTypes {

    private AdditionsEntityTypes() {
    }

    public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(MekanismAdditions.MODID);

    public static final EntityTypeRegistryObject<EntityBabyCreeper> BABY_CREEPER = ENTITY_TYPES.register("baby_creeper", FabricEntityTypeBuilder.create(MobCategory.MONSTER, EntityBabyCreeper::new).dimensions(EntityDimensions.scalable(0.6F, 1.7F)), Creeper::createAttributes);
    public static final EntityTypeRegistryObject<EntityBabyEnderman> BABY_ENDERMAN = ENTITY_TYPES.register("baby_enderman", FabricEntityTypeBuilder.create(MobCategory.MONSTER, EntityBabyEnderman::new).dimensions(EntityDimensions.scalable(0.6F, 2.9F)), EnderMan::createAttributes);
    public static final EntityTypeRegistryObject<EntityBabySkeleton> BABY_SKELETON = ENTITY_TYPES.register("baby_skeleton", FabricEntityTypeBuilder.create(MobCategory.MONSTER, EntityBabySkeleton::new).dimensions(EntityDimensions.scalable(0.6F, 1.99F)), AbstractSkeleton::createAttributes);
    public static final EntityTypeRegistryObject<EntityBabyStray> BABY_STRAY = ENTITY_TYPES.register("baby_stray", FabricEntityTypeBuilder.create(MobCategory.MONSTER, EntityBabyStray::new).dimensions(EntityDimensions.scalable(0.6F, 1.99F)), AbstractSkeleton::createAttributes);
    public static final EntityTypeRegistryObject<EntityBabyWitherSkeleton> BABY_WITHER_SKELETON = ENTITY_TYPES.register("baby_wither_skeleton", FabricEntityTypeBuilder.create(MobCategory.MONSTER, EntityBabyWitherSkeleton::new).fireImmune().dimensions(EntityDimensions.scalable(0.7F, 2.4F)), AbstractSkeleton::createAttributes);
    public static final EntityTypeRegistryObject<EntityBalloon> BALLOON = ENTITY_TYPES.register("balloon", FabricEntityTypeBuilder.create(MobCategory.MISC, EntityBalloon::new).dimensions(EntityDimensions.scalable(0.4F, 0.45F)));
    public static final EntityTypeRegistryObject<EntityObsidianTNT> OBSIDIAN_TNT = ENTITY_TYPES.register("obsidian_tnt", FabricEntityTypeBuilder.create(MobCategory.MISC, EntityObsidianTNT::new).fireImmune().dimensions(EntityDimensions.scalable(0.98F, 0.98F)));
}
