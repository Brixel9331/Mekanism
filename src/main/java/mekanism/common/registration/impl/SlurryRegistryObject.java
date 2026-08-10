package mekanism.common.registration.impl;

import mekanism.api.chemical.slurry.Slurry;
import mekanism.common.registration.INamedEntry;
import mekanism.common.registration.RegistryObject;
import org.jetbrains.annotations.NotNull;

public class SlurryRegistryObject<DIRTY extends Slurry, CLEAN extends Slurry> implements INamedEntry {

    private final RegistryObject<DIRTY> dirty;
    private final RegistryObject<CLEAN> clean;

    public SlurryRegistryObject(RegistryObject<DIRTY> dirtyRO, RegistryObject<CLEAN> cleanRO) {
        this.dirty = dirtyRO;
        this.clean = cleanRO;
    }

    @NotNull
    public DIRTY getDirtySlurry() {
        return getPrimary();
    }

    @NotNull
    public CLEAN getCleanSlurry() {
        return getSecondary();
    }

    public DIRTY getPrimary() {
        return dirty.get();
    }

    public CLEAN getSecondary() {
        return clean.get();
    }

    @Override
    public String getInternalRegistryName() {
        return dirty.getInternalRegistryName();
    }
}
