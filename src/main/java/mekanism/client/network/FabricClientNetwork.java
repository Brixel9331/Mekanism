package mekanism.client.network;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import mekanism.common.network.FabricPacketChannel.Direction;
import mekanism.common.network.FabricPacketChannel;
import mekanism.common.network.IMekanismPacket;
import mekanism.common.network.PacketContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class FabricClientNetwork {

    private FabricClientNetwork() {
    }

    public static void register(FabricPacketChannel channel) {
        AtomicBoolean accepted = new AtomicBoolean();
        ClientLoginConnectionEvents.INIT.register((handler, client) -> accepted.set(false));
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (!accepted.get()) {
                handler.getConnection().disconnect(Component.literal("Server requires a matching Mekanism installation: " + channel.id()));
            }
        });
        if (!ClientPlayNetworking.registerGlobalReceiver(channel.id(), (client, handler, buffer, responseSender) -> {
            IMekanismPacket packet;
            try {
                packet = channel.decode(buffer, Direction.CLIENTBOUND);
            } catch (RuntimeException exception) {
                handler.getConnection().disconnect(Component.literal("Invalid Mekanism packet on " + channel.id()));
                return;
            }
            client.execute(() -> {
                if (client.player != null && client.getConnection() == handler) {
                    packet.handle(new PacketContext(null));
                }
            });
        })) {
            throw new IllegalStateException("Packet receiver already registered: " + channel.id());
        }
        if (!ClientLoginNetworking.registerGlobalReceiver(channel.loginId(), (client, handler, buffer, listenerAdder) -> {
            String protocol = channel.protocol();
            boolean matches;
            try {
                matches = protocol.equals(buffer.readUtf(64)) && !buffer.isReadable();
            } catch (RuntimeException exception) {
                matches = false;
            }
            accepted.set(matches);
            FriendlyByteBuf response = PacketByteBufs.create().writeUtf(matches ? protocol : "");
            return CompletableFuture.completedFuture(response);
        })) {
            throw new IllegalStateException("Login receiver already registered: " + channel.loginId());
        }
    }

    public static void send(FabricPacketChannel channel, IMekanismPacket packet) {
        if (!ClientPlayNetworking.canSend(channel.id())) {
            throw new IllegalStateException("Server does not accept Mekanism packets: " + channel.id());
        }
        ClientPlayNetworking.send(channel.id(), channel.encode(packet, Direction.SERVERBOUND));
    }
}
