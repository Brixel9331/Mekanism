package mekanism.common.mixin.client;

import mekanism.common.recipe.RecipeLifecycle;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientRecipesMixin {

    @Inject(method = "handleUpdateRecipes", at = @At("RETURN"))
    private void mekanism$recipesUpdated(ClientboundUpdateRecipesPacket packet, CallbackInfo ci) {
        RecipeLifecycle.CLIENT_UPDATED.invoker().run();
    }
}
