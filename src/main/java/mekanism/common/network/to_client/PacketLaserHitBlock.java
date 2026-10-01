package mekanism.common.network.to_client;

import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.BlockHitResult;

public class PacketLaserHitBlock implements IMekanismPacket {

    private final BlockHitResult result;

    public PacketLaserHitBlock(BlockHitResult result) {
        this.result = result;
    }

    @Override
    public void handle(PacketContext context) {
        if (Minecraft.getInstance().level != null) {
            Minecraft.getInstance().particleEngine.crack(result.getBlockPos(), result.getDirection());
        }
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockHitResult(result);
    }

    public static PacketLaserHitBlock decode(FriendlyByteBuf buffer) {
        return new PacketLaserHitBlock(buffer.readBlockHitResult());
    }
}
