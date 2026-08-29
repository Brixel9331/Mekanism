package mekanism.common.lib;

import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class DropCapture {

    private static final ThreadLocal<State> active = new ThreadLocal<>();

    private DropCapture() {
    }

    public static Scope suppress() {
        State previous = active.get();
        return new Scope(previous, new State(true, previous == null ? null : previous.collector));
    }

    public static Scope collect(Predicate<ItemStack> collector) {
        State previous = active.get();
        return new Scope(previous, new State(previous != null && previous.suppress, Objects.requireNonNull(collector)));
    }

    public static boolean capture(Entity entity) {
        State state = active.get();
        if (state == null || entity.isRemoved()) {
            return false;
        }
        if (state.suppress) {
            if (entity instanceof ItemEntity || entity instanceof ExperienceOrb) {
                entity.discard();
                return true;
            }
        } else if (state.collector != null && entity instanceof ItemEntity item && state.collector.test(item.getItem())) {
            //If we have a fallback item collector active and the entity that is being added is an item,
            // try to let our fallback collector handle the item and keep track of it instead of actually adding it to the world
            entity.discard();
            return true;
        }
        return false;
    }

    private record State(boolean suppress, @Nullable Predicate<ItemStack> collector) {
    }

    public static final class Scope implements AutoCloseable {

        @Nullable
        private final State previous;
        private boolean closed;

        private Scope(@Nullable State previous, State current) {
            this.previous = previous;
            active.set(current);
        }

        @Override
        public void close() {
            if (!closed) {
                if (previous == null) {
                    active.remove();
                } else {
                    active.set(previous);
                }
                closed = true;
            }
        }
    }
}
