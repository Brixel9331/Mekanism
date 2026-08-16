package mekanism.common.network.to_client;

import mekanism.client.render.hud.MekanismStatusOverlay;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketContext;
import net.minecraft.network.FriendlyByteBuf;

public class PacketShowModeChange implements IMekanismPacket {

    public static final PacketShowModeChange INSTANCE = new PacketShowModeChange();

    private PacketShowModeChange() {
    }

    @Override
    public void handle(PacketContext context) {
        //TODO - 1.20: Test on server
        MekanismStatusOverlay.INSTANCE.setTimer();
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
    }

    public static PacketShowModeChange decode(FriendlyByteBuf buffer) {
        return INSTANCE;
    }
}
