package mekanism.common.network;

import io.netty.handler.codec.DecoderException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import mekanism.client.network.FabricClientNetwork;
import mekanism.common.lib.ServerLifecycle;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class FabricPacketChannel {

    private final ResourceLocation id;
    private final ResourceLocation loginId;
    private final String version;
    private final List<PacketType<?>> types = new ArrayList<>();
    private final Map<Class<?>, PacketType<?>> byClass = new HashMap<>();
    private boolean registered;

    public FabricPacketChannel(ResourceLocation id, String version) {
        this.id = Objects.requireNonNull(id);
        this.loginId = new ResourceLocation(id.getNamespace(), id.getPath() + "/protocol");
        this.version = Objects.requireNonNull(version);
    }

    public <MSG extends IMekanismPacket> void register(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder, Direction direction) {
        if (registered) {
            throw new IllegalStateException("Packet channel is already registered: " + id);
        } else if (byClass.containsKey(type)) {
            throw new IllegalArgumentException("Duplicate packet type: " + type.getName());
        }
        PacketType<MSG> entry = new PacketType<>(types.size(), Objects.requireNonNull(type), Objects.requireNonNull(decoder), Objects.requireNonNull(direction));
        types.add(entry);
        byClass.put(type, entry);
    }

    public ResourceLocation id() {
        return id;
    }

    public ResourceLocation loginId() {
        return loginId;
    }

    public String protocol() {
        StringBuilder descriptor = new StringBuilder(version);
        for (PacketType<?> type : types) {
            descriptor.append(';').append(type.direction).append(':').append(type.type.getName());
        }
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(descriptor.toString().getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public FriendlyByteBuf encode(IMekanismPacket packet, Direction direction) {
        PacketType<?> type = byClass.get(packet.getClass());
        if (type == null || type.direction != direction) {
            throw new IllegalArgumentException("Packet is not registered for " + direction + ": " + packet.getClass().getName());
        }
        FriendlyByteBuf buffer = PacketByteBufs.create();
        try {
            buffer.writeVarInt(type.index);
            packet.encode(buffer);
            return buffer;
        } catch (RuntimeException | Error exception) {
            buffer.release();
            throw exception;
        }
    }

    public IMekanismPacket decode(FriendlyByteBuf buffer, Direction direction) {
        int index = buffer.readVarInt();
        if (index < 0 || index >= types.size()) {
            throw new DecoderException("Unknown packet index for " + id + ": " + index);
        }
        PacketType<?> type = types.get(index);
        if (type.direction != direction) {
            throw new DecoderException("Invalid packet direction for " + type.type.getName());
        }
        IMekanismPacket packet = Objects.requireNonNull(type.decoder.apply(buffer), "Packet decoder returned null");
        if (buffer.isReadable()) {
            throw new DecoderException("Unread packet data for " + type.type.getName());
        }
        return packet;
    }

    public void registerReceivers() {
        if (registered) {
            throw new IllegalStateException("Packet channel is already registered: " + id);
        }
        registered = true;
        ServerLifecycle.register();
        String protocol = protocol();
        if (!ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler, buffer, responseSender) -> {
            IMekanismPacket packet;
            try {
                packet = decode(buffer, Direction.SERVERBOUND);
            } catch (RuntimeException exception) {
                handler.disconnect(Component.literal("Invalid Mekanism packet on " + id));
                return;
            }
            server.execute(() -> {
                if (player.connection == handler && !player.isRemoved()) {
                    packet.handle(new PacketContext(player));
                }
            });
        })) {
            throw new IllegalStateException("Packet receiver already registered: " + id);
        }
        if (!ServerLoginNetworking.registerGlobalReceiver(loginId, (server, handler, understood, buffer, synchronizer, responseSender) -> {
            boolean matches = false;
            if (understood) {
                try {
                    matches = protocol.equals(buffer.readUtf(64)) && !buffer.isReadable();
                } catch (RuntimeException ignored) {
                }
            }
            if (!matches) {
                handler.disconnect(Component.literal("Mekanism version or packet protocol mismatch: " + id));
            }
        })) {
            throw new IllegalStateException("Login receiver already registered: " + loginId);
        }
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> sender.sendPacket(loginId, PacketByteBufs.create().writeUtf(protocol)));
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            FabricClientNetwork.register(this);
        }
    }

    public void sendTo(IMekanismPacket packet, ServerPlayer player) {
        if (player.connection != null && !(player instanceof FakePlayer)) {
            ServerPlayNetworking.send(player, id, encode(packet, Direction.CLIENTBOUND));
        }
    }

    public void sendToServer(IMekanismPacket packet) {
        FabricClientNetwork.send(this, packet);
    }

    public enum Direction {
        SERVERBOUND,
        CLIENTBOUND
    }

    private record PacketType<MSG extends IMekanismPacket>(int index, Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder, Direction direction) {
    }
}
