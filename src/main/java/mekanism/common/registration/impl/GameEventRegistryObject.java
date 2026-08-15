package mekanism.common.registration.impl;

import mekanism.common.registration.RegistryObject;
import net.minecraft.world.level.gameevent.GameEvent;

public class GameEventRegistryObject<GAME_EVENT extends GameEvent> extends RegistryObject<GAME_EVENT> {

    public GameEventRegistryObject(RegistryObject<GAME_EVENT> registryObject) {
        super(registryObject);
    }
}