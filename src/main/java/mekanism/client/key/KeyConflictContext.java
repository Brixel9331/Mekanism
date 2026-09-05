package mekanism.client.key;

import net.minecraft.client.Minecraft;

public enum KeyConflictContext implements IKeyConflictContext {
    UNIVERSAL,
    IN_GAME,
    GUI;

    @Override
    public boolean isActive() {
        return switch (this) {
            case UNIVERSAL -> true;
            case IN_GAME -> Minecraft.getInstance().screen == null;
            case GUI -> Minecraft.getInstance().screen != null;
        };
    }
}
