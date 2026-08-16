package mekanism.common.network;

import io.netty.handler.codec.DecoderException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import mekanism.api.functions.TriConsumer;
import mekanism.common.Mekanism;
import mekanism.common.config.MekanismConfig;
import mekanism.common.lib.ServerLifecycle;
import mekanism.common.lib.Version;
import mekanism.common.lib.math.Range3D;
import mekanism.common.lib.transmitter.DynamicBufferedNetwork;
import mekanism.common.network.FabricPacketChannel.Direction;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public abstract class BasePacketHandler {

    protected static FabricPacketChannel createChannel(ResourceLocation name, Version version) {
        return new FabricPacketChannel(name, version.toString());
    }

    /**
     * Helper for reading strings to make sure we don't accidentally call {@link FriendlyByteBuf#readUtf()} on the server
     */
    public static String readString(FriendlyByteBuf buffer) {
        //TODO - 1.18: Evaluate usages and potentially move some things to more strict string length checks
        return buffer.readUtf(Short.MAX_VALUE);
    }

    public static Vec3 readVector3d(FriendlyByteBuf buffer) {
        return new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    public static void writeVector3d(FriendlyByteBuf buffer, Vec3 vector) {
        buffer.writeDouble(vector.x());
        buffer.writeDouble(vector.y());
        buffer.writeDouble(vector.z());
    }

    //Like FriendlyByteBuf#writeOptional but with nullable things instead
    public static <TYPE> void writeOptional(FriendlyByteBuf buffer, @Nullable TYPE value, BiConsumer<FriendlyByteBuf, TYPE> writer) {
        if (value == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            writer.accept(buffer, value);
        }
    }

    //Like FriendlyByteBuf#writeOptional but with nullable things instead
    @Nullable
    public static <TYPE> TYPE readOptional(FriendlyByteBuf buffer, Function<FriendlyByteBuf, TYPE> reader) {
        return buffer.readBoolean() ? reader.apply(buffer) : null;
    }

    public static <TYPE> void writeArray(FriendlyByteBuf buffer, TYPE[] array, BiConsumer<TYPE, FriendlyByteBuf> writer) {
        buffer.writeVarInt(array.length);
        for (TYPE element : array) {
            writer.accept(element, buffer);
        }
    }

    public static <TYPE> TYPE[] readArray(FriendlyByteBuf buffer, IntFunction<TYPE[]> arrayFactory, Function<FriendlyByteBuf, TYPE> reader) {
        TYPE[] array = arrayFactory.apply(readCollectionSize(buffer));
        for (int element = 0; element < array.length; element++) {
            array[element] = reader.apply(buffer);
        }
        return array;
    }

    public static <KEY, VALUE> void writeMap(FriendlyByteBuf buffer, Map<KEY, VALUE> map, TriConsumer<KEY, VALUE, FriendlyByteBuf> writer) {
        buffer.writeVarInt(map.size());
        map.forEach((key, value) -> writer.accept(key, value, buffer));
    }

    public static <KEY, VALUE, MAP extends Map<KEY, VALUE>> MAP readMap(FriendlyByteBuf buffer, IntFunction<MAP> mapFactory, Function<FriendlyByteBuf, KEY> keyReader,
          Function<FriendlyByteBuf, VALUE> valueReader) {
        int elements = readCollectionSize(buffer);
        MAP map = mapFactory.apply(elements);
        for (int element = 0; element < elements; element++) {
            map.put(keyReader.apply(buffer), valueReader.apply(buffer));
        }
        return map;
    }

    private static int readCollectionSize(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > buffer.readableBytes()) {
            throw new DecoderException("Invalid collection length: " + size);
        }
        return size;
    }

    public static void log(String logFormat, Object... params) {
        //TODO: Add more logging for packets using this
        if (MekanismConfig.general.logPackets.get()) {
            Mekanism.logger.info(logFormat, params);
        }
    }

    protected abstract FabricPacketChannel getChannel();

    public abstract void initialize();

    protected <MSG extends IMekanismPacket> void registerClientToServer(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder) {
        getChannel().register(type, decoder, Direction.SERVERBOUND);
    }

    protected <MSG extends IMekanismPacket> void registerServerToClient(Class<MSG> type, Function<FriendlyByteBuf, MSG> decoder) {
        getChannel().register(type, decoder, Direction.CLIENTBOUND);
    }

    /**
     * Send this message to the specified player.
     *
     * @param message - the message to send
     * @param player  - the player to send it to
     */
    public <MSG extends IMekanismPacket> void sendTo(MSG message, ServerPlayer player) {
        //Validate it is not a fake player, even though none of our code should call this with a fake player
        getChannel().sendTo(message, player);
    }

    /**
     * Send this message to everyone connected to the server.
     *
     * @param message - message to send
     */
    public <MSG extends IMekanismPacket> void sendToAll(MSG message) {
        MinecraftServer server = ServerLifecycle.getCurrentServer();
        if (server != null) {
            PlayerLookup.all(server).forEach(player -> sendTo(message, player));
        }
    }

    /**
     * Send this message to everyone connected to the server if the server has loaded.
     *
     * @param message - message to send
     *
     * @apiNote This is useful for reload listeners
     */
    public <MSG extends IMekanismPacket> void sendToAllIfLoaded(MSG message) {
        if (ServerLifecycle.getCurrentServer() != null) {
            //If the server has loaded, send to all players
            sendToAll(message);
        }
    }

    /**
     * Send this message to everyone within the supplied dimension.
     *
     * @param message   - the message to send
     * @param dimension - the dimension to target
     */
    public <MSG extends IMekanismPacket> void sendToDimension(MSG message, ResourceKey<Level> dimension) {
        MinecraftServer server = ServerLifecycle.getCurrentServer();
        if (server != null) {
            ServerLevel level = server.getLevel(dimension);
            if (level != null) {
                PlayerLookup.world(level).forEach(player -> sendTo(message, player));
            }
        }
    }

    /**
     * Send this message to the server.
     *
     * @param message - the message to send
     */
    public <MSG extends IMekanismPacket> void sendToServer(MSG message) {
        getChannel().sendToServer(message);
    }

    public <MSG extends IMekanismPacket> void sendToAllTracking(MSG message, Entity entity) {
        PlayerLookup.tracking(entity).forEach(player -> sendTo(message, player));
    }

    public <MSG extends IMekanismPacket> void sendToAllTrackingAndSelf(MSG message, Entity entity) {
        Set<ServerPlayer> players = new HashSet<>(PlayerLookup.tracking(entity));
        if (entity instanceof ServerPlayer player) {
            players.add(player);
        }
        players.forEach(player -> sendTo(message, player));
    }

    public <MSG extends IMekanismPacket> void sendToAllTracking(MSG message, BlockEntity tile) {
        sendToAllTracking(message, tile.getLevel(), tile.getBlockPos());
    }

    public <MSG extends IMekanismPacket> void sendToAllTracking(MSG message, Level world, BlockPos pos) {
        if (world instanceof ServerLevel level) {
            //If we have a ServerWorld just directly figure out the ChunkPos to not require looking up the chunk
            // This provides a decent performance boost over using the packet distributor
            PlayerLookup.tracking(level, pos).forEach(player -> sendTo(message, player));
        } else {
            //Otherwise, fallback to entities tracking the chunk if some mod did something odd and our world is not a ServerWorld
            throw new IllegalArgumentException("Tracking packets require a server level");
        }
    }

    public <MSG extends IMekanismPacket> void sendToReceivers(MSG message, DynamicBufferedNetwork<?, ?, ?, ?> network) {
        //TODO: Figure out why we have a try catch and remove the need for it
        try {
            //TODO: Create a method in DynamicNetwork to get all players that are "tracking" the network
            // Also evaluate moving various network packet things over to using this at that point
            MinecraftServer server = ServerLifecycle.getCurrentServer();
            if (server != null) {
                Range3D range = network.getPacketRange();
                PlayerList playerList = server.getPlayerList();
                //Ignore height for partial Cubic chunks support as range comparison gets used ignoring player height normally anyway
                int radius = playerList.getViewDistance() * 16;
                for (ServerPlayer player : playerList.getPlayers()) {
                    if (range.dimension() == player.level().dimension()) {
                        BlockPos playerPosition = player.blockPosition();
                        int playerX = playerPosition.getX();
                        int playerZ = playerPosition.getZ();
                        //playerX/Z + radius is the max, so to stay in line with how it was before, it has an extra + 1 added to it
                        if (playerX + radius + 1.99999 > range.xMin() && range.xMax() + 0.99999 > playerX - radius &&
                            playerZ + radius + 1.99999 > range.zMin() && range.zMax() + 0.99999 > playerZ - radius) {
                            sendTo(message, player);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}