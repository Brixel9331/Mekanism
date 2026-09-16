package mekanism.common.util;

import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class EntityTeleportation {

    private static final ThreadLocal<Entity> DIMENSION_QUERY = new ThreadLocal<>();

    private EntityTeleportation() {
    }

    public static boolean canChangeDimensions(Entity entity) {
        Entity previous = DIMENSION_QUERY.get();
        DIMENSION_QUERY.set(entity);
        try {
            return entity.canChangeDimensions();
        } finally {
            if (previous == null) {
                DIMENSION_QUERY.remove();
            } else {
                DIMENSION_QUERY.set(previous);
            }
        }
    }

    public static boolean isCheckingDimension(Entity entity) {
        return DIMENSION_QUERY.get() == entity;
    }

    @Nullable
    public static Entity teleport(Entity entity, ServerLevel destination, Vec3 position) {
        return teleport(entity, destination, position, entity.getDeltaMovement());
    }

    @Nullable
    public static Entity teleport(Entity entity, ServerLevel destination, Vec3 position, Vec3 movement) {
        if (!(entity.level() instanceof ServerLevel origin) || origin.getServer() != destination.getServer() || entity.isRemoved()) {
            return null;
        }
        if (!origin.getServer().isSameThread()) {
            throw new IllegalStateException("Entities must be teleported from the server thread");
        }
        if (origin == destination) {
            entity.teleportTo(position.x, position.y, position.z);
            entity.setDeltaMovement(movement);
            synchronizePassengers(entity);
            return entity;
        }
        Entity teleported = teleportTree(entity, destination, position, movement, null);
        if (teleported != null) {
            synchronizePassengers(teleported);
        }
        return teleported;
    }

    @Nullable
    private static Entity teleportTree(Entity entity, ServerLevel destination, Vec3 position, Vec3 movement, @Nullable Entity vehicle) {
        if (entity.isRemoved() || !canChangeDimensions(entity)) {
            return null;
        }
        List<Entity> passengers = entity.getPassengers();
        boolean invulnerable = entity.isInvulnerable();
        Entity teleported = null;
        try {
            if (vehicle != null) {
                entity.setInvulnerable(true);
            }
            if (entity instanceof ServerPlayer player) {
                if (player.teleportTo(destination, position.x, position.y, position.z, Set.of(), player.getYRot(), player.getXRot())) {
                    teleported = player;
                }
            } else {
                teleported = FabricDimensions.teleport(entity, destination, new PortalInfo(position, movement, entity.getYRot(), entity.getXRot()));
            }
            if (teleported != null && vehicle != null) {
                teleported.startRiding(vehicle, true);
            }
        } finally {
            if (vehicle != null) {
                entity.setInvulnerable(invulnerable);
                if (teleported != null) {
                    teleported.setInvulnerable(invulnerable);
                }
            }
        }
        if (teleported != null) {
            for (Entity passenger : passengers) {
                teleportTree(passenger, destination, position, passenger.getDeltaMovement(), teleported);
            }
        }
        return teleported;
    }

    private static void synchronizePassengers(Entity entity) {
        if (!entity.getPassengers().isEmpty()) {
            ((ServerLevel) entity.level()).getChunkSource().broadcast(entity, new ClientboundSetPassengersPacket(entity));
            if (entity.getControllingPassenger() instanceof ServerPlayer player && !(player instanceof FakePlayer) && player.connection != null) {
                player.connection.send(new ClientboundMoveVehiclePacket(entity));
            }
            for (Entity passenger : entity.getPassengers()) {
                synchronizePassengers(passenger);
            }
        }
    }
}
