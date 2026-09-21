package mekanism.common.registries;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

public final class MekanismAttributes {

    public static final Attribute STEP_HEIGHT = new RangedAttribute("attribute.name.mekanism.step_height", 0, -512, 512).setSyncable(true);
    public static final Attribute SWIM_SPEED = new RangedAttribute("attribute.name.mekanism.swim_speed", 1, 0, 1024).setSyncable(true);

    private MekanismAttributes() {
    }

    public static void register() {
        Registry.register(BuiltInRegistries.ATTRIBUTE, new ResourceLocation("mekanism", "step_height"), STEP_HEIGHT);
        Registry.register(BuiltInRegistries.ATTRIBUTE, new ResourceLocation("mekanism", "swim_speed"), SWIM_SPEED);
    }
}
