package mekanism.common.base;

import java.util.function.Predicate;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class MekanismPermissions {

    public static final String BYPASS_SECURITY = "mekanism.bypass_security";

    //Commands
    public static final CommandPermissionNode COMMAND = new CommandPermissionNode("mekanism.command", Commands.LEVEL_ALL);

    public static final CommandPermissionNode COMMAND_BUILD = nodeOpCommand("build");
    public static final CommandPermissionNode COMMAND_BUILD_REMOVE = nodeSubCommand(COMMAND_BUILD, "remove");

    public static final CommandPermissionNode COMMAND_CHUNK = nodeOpCommand("chunk");
    public static final CommandPermissionNode COMMAND_CHUNK_CLEAR = nodeSubCommand(COMMAND_CHUNK, "clear");
    public static final CommandPermissionNode COMMAND_CHUNK_FLUSH = nodeSubCommand(COMMAND_CHUNK, "flush");
    public static final CommandPermissionNode COMMAND_CHUNK_UNWATCH = nodeSubCommand(COMMAND_CHUNK, "unwatch");
    public static final CommandPermissionNode COMMAND_CHUNK_WATCH = nodeSubCommand(COMMAND_CHUNK, "watch");

    public static final CommandPermissionNode COMMAND_DEBUG = nodeOpCommand("debug");
    public static final CommandPermissionNode COMMAND_FORCE_RETROGEN = nodeOpCommand("force_retrogen");

    public static final CommandPermissionNode COMMAND_RADIATION = nodeOpCommand("radiation");
    public static final CommandPermissionNode COMMAND_RADIATION_ADD = nodeSubCommand(COMMAND_RADIATION, "add");
    public static final CommandPermissionNode COMMAND_RADIATION_ADD_ENTITY = nodeSubCommand(COMMAND_RADIATION, "add_entity");
    public static final CommandPermissionNode COMMAND_RADIATION_ADD_ENTITY_OTHERS = nodeSubCommand(COMMAND_RADIATION_ADD_ENTITY, "others");
    public static final CommandPermissionNode COMMAND_RADIATION_GET = nodeSubCommand(COMMAND_RADIATION, "get");
    public static final CommandPermissionNode COMMAND_RADIATION_HEAL = nodeSubCommand(COMMAND_RADIATION, "heal");
    public static final CommandPermissionNode COMMAND_RADIATION_HEAL_OTHERS = nodeSubCommand(COMMAND_RADIATION_HEAL, "others");
    public static final CommandPermissionNode COMMAND_RADIATION_REDUCE = nodeSubCommand(COMMAND_RADIATION, "reduce");
    public static final CommandPermissionNode COMMAND_RADIATION_REDUCE_OTHERS = nodeSubCommand(COMMAND_RADIATION_REDUCE, "others");
    public static final CommandPermissionNode COMMAND_RADIATION_REMOVE_ALL = nodeSubCommand(COMMAND_RADIATION, "remove.all");

    public static final CommandPermissionNode COMMAND_TEST_RULES = nodeOpCommand("test_rules");
    public static final CommandPermissionNode COMMAND_TP = nodeOpCommand("tp");
    public static final CommandPermissionNode COMMAND_TP_POP = nodeOpCommand("tp_pop");

    private static CommandPermissionNode nodeOpCommand(String nodeName) {
        return new CommandPermissionNode("mekanism.command." + nodeName, Commands.LEVEL_GAMEMASTERS);
    }

    private static CommandPermissionNode nodeSubCommand(CommandPermissionNode parent, String nodeName) {
        return new CommandPermissionNode(parent.node + "." + nodeName, parent.fallbackLevel, true);
    }

    public static boolean canBypassSecurity(ServerPlayer player) {
        return Permissions.check(player, BYPASS_SECURITY, player.server.getPlayerList().isOp(player.getGameProfile()));
    }

    public record CommandPermissionNode(String node, int fallbackLevel, boolean child) implements Predicate<CommandSourceStack> {

        public CommandPermissionNode(String node, int fallbackLevel) {
            this(node, fallbackLevel, false);
        }

        @Override
        public boolean test(CommandSourceStack source) {
            return source.hasPermission(fallbackLevel) || source.getEntity() instanceof ServerPlayer && Permissions.check(source, node, child);
        }
    }
}
