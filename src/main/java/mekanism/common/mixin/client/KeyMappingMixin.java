package mekanism.common.mixin.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import java.util.Map;
import mekanism.client.key.KeyModifier;
import mekanism.client.key.MekKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin {

    @Shadow
    @Final
    private static Map<String, KeyMapping> ALL;

    @Inject(method = "set", at = @At("HEAD"), cancellable = true)
    private static void mekanism$updateSharedBindings(InputConstants.Key key, boolean down, CallbackInfo callback) {
        if (key.equals(InputConstants.UNKNOWN)) {
            callback.cancel();
            return;
        }
        List<KeyMapping> mappings = mekanism$sharedBindings(key);
        if (!mappings.isEmpty()) {
            boolean modified = down && mekanism$hasActiveModifier(mappings);
            for (KeyMapping mapping : mappings) {
                mapping.setDown(down && mekanism$isActive(mapping, modified));
            }
            callback.cancel();
        }
    }

    @Inject(method = "click", at = @At("HEAD"), cancellable = true)
    private static void mekanism$clickSharedBindings(InputConstants.Key key, CallbackInfo callback) {
        if (key.equals(InputConstants.UNKNOWN)) {
            callback.cancel();
            return;
        }
        List<KeyMapping> mappings = mekanism$sharedBindings(key);
        if (!mappings.isEmpty()) {
            boolean modified = mekanism$hasActiveModifier(mappings);
            for (KeyMapping mapping : mappings) {
                if (mekanism$isActive(mapping, modified)) {
                    KeyMappingAccessor access = (KeyMappingAccessor) mapping;
                    access.mekanism$setClickCount(access.mekanism$getClickCount() + 1);
                }
            }
            callback.cancel();
        }
    }

    @Inject(method = "same", at = @At("HEAD"), cancellable = true)
    private void mekanism$checkConflict(KeyMapping other, CallbackInfoReturnable<Boolean> callback) {
        if (other instanceof MekKeyBinding mek) {
            callback.setReturnValue(mek.same((KeyMapping) (Object) this));
        }
    }

    @Unique
    private static List<KeyMapping> mekanism$sharedBindings(InputConstants.Key key) {
        if (key.equals(InputConstants.UNKNOWN)) {
            return List.of();
        }
        List<KeyMapping> mappings = ALL.values().stream().filter(mapping -> KeyBindingHelper.getBoundKeyOf(mapping).equals(key)).toList();
        return mappings.stream().anyMatch(MekKeyBinding.class::isInstance) ? mappings : List.of();
    }

    @Unique
    private static boolean mekanism$hasActiveModifier(List<KeyMapping> mappings) {
        return mappings.stream().anyMatch(mapping -> mapping instanceof MekKeyBinding mek && mek.getKeyModifier() != KeyModifier.NONE &&
              mek.isConflictContextAndModifierActive());
    }

    @Unique
    private static boolean mekanism$isActive(KeyMapping mapping, boolean modified) {
        if (mapping instanceof MekKeyBinding mek) {
            return mek.isConflictContextAndModifierActive() && (!modified || mek.getKeyModifier() != KeyModifier.NONE);
        }
        return !modified;
    }
}
