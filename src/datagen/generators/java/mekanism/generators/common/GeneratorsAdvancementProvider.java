package mekanism.generators.common;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import java.util.function.Consumer;
import mekanism.common.advancements.BaseAdvancementProvider;
import mekanism.generators.common.advancements.GeneratorsAdvancements;
import mekanism.generators.common.registries.GeneratorsBlocks;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

public class GeneratorsAdvancementProvider extends BaseAdvancementProvider {

    public GeneratorsAdvancementProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, existingFileHelper, MekanismGenerators.MODID);
    }

    @Override
    protected void registerAdvancements(@NotNull Consumer<Advancement> consumer) {
        advancement(GeneratorsAdvancements.HEAT_GENERATOR)
              .displayAndCriterion(GeneratorsBlocks.HEAT_GENERATOR, FrameType.TASK, true)
              .save(consumer);
        advancement(GeneratorsAdvancements.SOLAR_GENERATOR)
              .displayAndCriterion(GeneratorsBlocks.SOLAR_GENERATOR, FrameType.TASK, false)
              .save(consumer);
        advancement(GeneratorsAdvancements.WIND_GENERATOR)
              .displayAndCriterion(GeneratorsBlocks.WIND_GENERATOR, FrameType.TASK, false)
              .save(consumer);
        advancement(GeneratorsAdvancements.BURN_THE_GAS)
              .displayAndCriterion(GeneratorsBlocks.GAS_BURNING_GENERATOR, FrameType.GOAL, true)
              .save(consumer);
    }
}