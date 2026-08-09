package mekanism.api.chemical.slurry;

import java.util.Optional;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.NotNull;

public final class EmptySlurry extends Slurry {

    public EmptySlurry() {
        super(SlurryBuilder.clean().hidden());
    }

    @NotNull
    @Override
    protected Optional<Holder.Reference<Slurry>> getReverseTag() {
        //Empty slurry is in no tags
        return Optional.empty();
    }
}