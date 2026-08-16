package mekanism.common.network;

import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public record PacketContext(@Nullable ServerPlayer sender) {

    @Nullable
    public ServerPlayer getSender() {
        return sender;
    }
}
