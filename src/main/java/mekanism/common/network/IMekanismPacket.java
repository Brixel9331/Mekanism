package mekanism.common.network;

import net.minecraft.network.FriendlyByteBuf;

public interface IMekanismPacket {

    void handle(PacketContext context);

    void encode(FriendlyByteBuf buffer);
}
