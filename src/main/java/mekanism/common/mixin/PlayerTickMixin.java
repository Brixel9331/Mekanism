package mekanism.common.mixin;

import mekanism.common.lib.PlayerTickCallback;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerTickMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void mekanism$playerTick(CallbackInfo ci) {
        PlayerTickCallback.EVENT.invoker().onTick((Player) (Object) this);
    }
}
