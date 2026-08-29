package mekanism.common.recipe;

import java.util.List;
import mekanism.api.text.EnumColor;
import mekanism.common.MekanismLang;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class IncompleteRecipeScanner {

    private static final Component RECIPE_WARNING = MekanismLang.LOG_FORMAT.translateColored(EnumColor.RED, MekanismLang.MEKANISM/*, EnumColor.GRAY*/, MekanismLang.RECIPE_WARNING.translate());
    private static boolean foundIncompleteRecipes = false;
    private static boolean registered;

    public static void register() {
        if (!registered) {
            RecipeLifecycle.SERVER_UPDATED.register(IncompleteRecipeScanner::recipes);
            ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
                //player is logging in
                if (joined && foundIncompleteRecipes) {
                    sendMessageToPlayer(player);
                }
            });
            ServerLifecycleEvents.SERVER_STOPPED.register(server -> foundIncompleteRecipes = false);
            registered = true;
        }
    }

    private static void recipes(MinecraftServer server, boolean initial) {
        //run the scan
        foundIncompleteRecipes = MekanismRecipeType.checkIncompleteRecipes(server);

        //if broken, message any players online
        if (foundIncompleteRecipes && !initial) {
            List<ServerPlayer> players = server.getPlayerList().getPlayers();
            if (!players.isEmpty()) {
                players.forEach(IncompleteRecipeScanner::sendMessageToPlayer);
            }
        }
    }

    private static void sendMessageToPlayer(ServerPlayer player) {
        player.sendSystemMessage(RECIPE_WARNING);
    }
}
