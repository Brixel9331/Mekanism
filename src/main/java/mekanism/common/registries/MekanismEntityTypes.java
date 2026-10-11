package mekanism.common.registries;

import mekanism.common.Mekanism;
import mekanism.common.entity.EntityFlame;
import mekanism.common.entity.EntityRobit;
import mekanism.common.registration.impl.EntityTypeDeferredRegister;
import mekanism.common.registration.impl.EntityTypeRegistryObject;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MobCategory;

public class MekanismEntityTypes {

    private MekanismEntityTypes() {
    }

    public static final EntityTypeDeferredRegister ENTITY_TYPES = new EntityTypeDeferredRegister(Mekanism.MODID);

    public static final EntityTypeRegistryObject<EntityFlame> FLAME = ENTITY_TYPES.register("flame", FabricEntityTypeBuilder.create(MobCategory.MISC, EntityFlame::new).dimensions(EntityDimensions.scalable(0.5F, 0.5F)).fireImmune());
    public static final EntityTypeRegistryObject<EntityRobit> ROBIT = ENTITY_TYPES.register("robit", FabricEntityTypeBuilder.create(MobCategory.MISC, EntityRobit::new).dimensions(EntityDimensions.scalable(0.6F, 0.65F)).fireImmune().disableSummon(), EntityRobit::getDefaultAttributes);
}
