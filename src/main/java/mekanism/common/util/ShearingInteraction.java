package mekanism.common.util;

import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.energy.IEnergyContainer;
import mekanism.api.math.FloatingLong;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Shearable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

public final class ShearingInteraction {

    private ShearingInteraction() {
    }

    public static boolean shear(@Nullable IEnergyContainer energyContainer, FloatingLong cost, LivingEntity entity, @Nullable Player player) {
        if (entity.isSpectator() || !(entity instanceof Shearable target) || !target.readyForShearing()) {
            return false;
        }
        if (!cost.isZero() && (energyContainer == null || energyContainer.extract(cost, Action.SIMULATE, AutomationType.MANUAL).smallerThan(cost))) {
            return false;
        }
        Level world = entity.level();
        if (!world.isClientSide) {
            target.shear(player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS);
            world.gameEvent(player, GameEvent.SHEAR, entity.blockPosition());
            if (energyContainer != null) {
                energyContainer.extract(cost, Action.EXECUTE, AutomationType.MANUAL);
            }
        }
        return true;
    }
}
