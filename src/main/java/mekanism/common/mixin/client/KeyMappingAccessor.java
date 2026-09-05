package mekanism.common.mixin.client;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {

    @Accessor("clickCount")
    int mekanism$getClickCount();

    @Accessor("clickCount")
    void mekanism$setClickCount(int count);
}
