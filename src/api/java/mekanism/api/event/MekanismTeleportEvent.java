package mekanism.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Base Mekanism extension of the {@link EntityTeleportEvent}.
 *
 * @since 10.3.9
 */
public class MekanismTeleportEvent {

    public static final Event<Callback> BEFORE_TELEPORT = EventFactory.createArrayBacked(Callback.class, callbacks -> event -> {
        for (Callback callback : callbacks) {
            if (event.isCanceled()) {
                break;
            }
            callback.onTeleport(event);
        }
    });

    private final Entity entity;
    private final Vec3 target;
    private boolean canceled;

    /**
     * @param entity  Entity teleporting.
     * @param targetX Destination x position.
     * @param targetY Destination y position.
     * @param targetZ Destination z position.
     */
    protected MekanismTeleportEvent(Entity entity, double targetX, double targetY, double targetZ) {
        this.entity = entity;
        this.target = new Vec3(targetX, targetY, targetZ);
    }

    public Entity getEntity() {
        return entity;
    }

    public Vec3 getTarget() {
        return target;
    }

    public double getTargetX() {
        return target.x;
    }

    public double getTargetY() {
        return target.y;
    }

    public double getTargetZ() {
        return target.z;
    }

    public Vec3 getPrev() {
        return entity.position();
    }

    public double getPrevX() {
        return entity.getX();
    }

    public double getPrevY() {
        return entity.getY();
    }

    public double getPrevZ() {
        return entity.getZ();
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }

    public boolean post() {
        BEFORE_TELEPORT.invoker().onTeleport(this);
        return canceled;
    }

    @FunctionalInterface
    public interface Callback {

        void onTeleport(MekanismTeleportEvent event);
    }

    /**
     * This event is fired before a player teleports using the Meka-Tool's Teleportation Unit.
     * <br>
     * This event is {@link Cancelable}.
     * <br>
     * If the event is not canceled, the entity will be teleported.
     * <br>
     * This event <strong>does not</strong> allow changing the target position.
     * <br>
     * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
     * <br>
     * This event is only fired on the {@link LogicalSide#SERVER} side.
     */
    public static class MekaTool extends MekanismTeleportEvent {

        private final BlockHitResult targetBlock;
        private final ItemStack mekaTool;


        /**
         * @param player      Player teleporting using the Meka-Tool.
         * @param targetX     Destination x position.
         * @param targetY     Destination y position.
         * @param targetZ     Destination z position.
         * @param mekaTool    Meka-Tool used for teleportation.
         * @param targetBlock The hit result representing the target block.
         */
        public MekaTool(Player player, double targetX, double targetY, double targetZ, ItemStack mekaTool, BlockHitResult targetBlock) {
            super(player, targetX, targetY, targetZ);
            this.mekaTool = mekaTool;
            this.targetBlock = targetBlock;
        }

        @Override
        public Player getEntity() {
            return (Player) super.getEntity();
        }

        /**
         * @return The ItemStack for the Meka-Tool the player is using to teleport.
         */
        public ItemStack getMekaTool() {
            return mekaTool;
        }

        /**
         * Gets the hit result representing the targeted block. This result will have different values than the values returned by {@link #getTarget()} as that method
         * represents the adjusted position that the player is being teleported to, rather than the block that was targeted.
         *
         * @return The hit result representing the target block for this event.
         */
        public BlockHitResult getTargetBlock() {
            return targetBlock;
        }
    }
}
