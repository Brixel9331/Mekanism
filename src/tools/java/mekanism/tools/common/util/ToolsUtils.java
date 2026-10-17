package mekanism.tools.common.util;

import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.mininglevel.v1.MiningLevelManager;
import mekanism.tools.common.material.BaseMekanismMaterial;
import mekanism.tools.common.ToolsLang;
import mekanism.tools.common.config.MekanismToolsConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ToolsUtils {

    public static int getRequiredMiningLevel(BlockState state) {
        int level = MiningLevelManager.getRequiredMiningLevel(state);
        for (BaseMekanismMaterial material : new BaseMekanismMaterial[]{MekanismToolsConfig.tools.bronze, MekanismToolsConfig.tools.lapisLazuli,
              MekanismToolsConfig.tools.osmium, MekanismToolsConfig.tools.steel, MekanismToolsConfig.tools.refinedGlowstone, MekanismToolsConfig.tools.refinedObsidian}) {
            if (material.getTag() != null && state.is(material.getTag())) {
                level = Math.max(level, material.getLevel());
            }
        }
        return level;
    }

    /**
     * Adds durability to the tooltip if enabled in the config
     *
     * @apiNote Only call on client
     */
    public static void addDurability(@NotNull List<Component> tooltip, @NotNull ItemStack stack) {
        if (MekanismToolsConfig.toolsClient.displayDurabilityTooltips.get()) {
            tooltip.add(ToolsLang.HP.translate(stack.getMaxDamage() - stack.getDamageValue()));
        }
    }
}