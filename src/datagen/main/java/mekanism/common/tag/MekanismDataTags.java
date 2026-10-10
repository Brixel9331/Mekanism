package mekanism.common.tag;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

public final class MekanismDataTags {

    private MekanismDataTags() {
    }

    public static final class Items {

        public static final TagKey<Item> BARRELS_WOODEN = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "barrels/wooden"));
        public static final TagKey<Item> BOOKSHELVES = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "bookshelves"));
        public static final TagKey<Item> CHESTS_WOODEN = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "chests/wooden"));
        public static final TagKey<Item> COBBLESTONE_DEEPSLATE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "cobblestone/deepslate"));
        public static final TagKey<Item> COBBLESTONE_NORMAL = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "cobblestone/normal"));
        public static final TagKey<Item> CROPS_WHEAT = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "crops/wheat"));
        public static final TagKey<Item> DUSTS_GLOWSTONE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "dusts/glowstone"));
        public static final TagKey<Item> DUSTS_REDSTONE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "dusts/redstone"));
        public static final TagKey<Item> DYES_BLACK = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "dyes/black"));
        public static final TagKey<Item> DYES_BLUE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "dyes/blue"));
        public static final TagKey<Item> DYES_GRAY = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "dyes/gray"));
        public static final TagKey<Item> DYES_GREEN = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "dyes/green"));
        public static final TagKey<Item> DYES_ORANGE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "dyes/orange"));
        public static final TagKey<Item> DYES_PINK = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "dyes/pink"));
        public static final TagKey<Item> DYES_PURPLE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "dyes/purple"));
        public static final TagKey<Item> DYES_RED = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "dyes/red"));
        public static final TagKey<Item> DYES_WHITE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "dyes/white"));
        public static final TagKey<Item> DYES_YELLOW = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "dyes/yellow"));
        public static final TagKey<Item> FENCES_WOODEN = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "fences/wooden"));
        public static final TagKey<Item> GEMS_DIAMOND = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "gems/diamond"));
        public static final TagKey<Item> GEMS_EMERALD = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "gems/emerald"));
        public static final TagKey<Item> GEMS_LAPIS = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "gems/lapis"));
        public static final TagKey<Item> GEMS_QUARTZ = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "gems/quartz"));
        public static final TagKey<Item> GLASS_BLACK = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/black"));
        public static final TagKey<Item> GLASS_BLUE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/blue"));
        public static final TagKey<Item> GLASS_BROWN = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/brown"));
        public static final TagKey<Item> GLASS_CYAN = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/cyan"));
        public static final TagKey<Item> GLASS_GRAY = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/gray"));
        public static final TagKey<Item> GLASS_GREEN = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/green"));
        public static final TagKey<Item> GLASS_LIGHT_BLUE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/light_blue"));
        public static final TagKey<Item> GLASS_LIGHT_GRAY = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/light_gray"));
        public static final TagKey<Item> GLASS_LIME = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/lime"));
        public static final TagKey<Item> GLASS_MAGENTA = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/magenta"));
        public static final TagKey<Item> GLASS_ORANGE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/orange"));
        public static final TagKey<Item> GLASS_PANES = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes"));
        public static final TagKey<Item> GLASS_PANES_BLACK = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes/black"));
        public static final TagKey<Item> GLASS_PANES_BLUE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes/blue"));
        public static final TagKey<Item> GLASS_PANES_BROWN = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes/brown"));
        public static final TagKey<Item> GLASS_PANES_CYAN = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes/cyan"));
        public static final TagKey<Item> GLASS_PANES_GRAY = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes/gray"));
        public static final TagKey<Item> GLASS_PANES_GREEN = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes/green"));
        public static final TagKey<Item> GLASS_PANES_LIGHT_BLUE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes/light_blue"));
        public static final TagKey<Item> GLASS_PANES_LIGHT_GRAY = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes/light_gray"));
        public static final TagKey<Item> GLASS_PANES_LIME = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes/lime"));
        public static final TagKey<Item> GLASS_PANES_MAGENTA = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes/magenta"));
        public static final TagKey<Item> GLASS_PANES_ORANGE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes/orange"));
        public static final TagKey<Item> GLASS_PANES_PINK = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes/pink"));
        public static final TagKey<Item> GLASS_PANES_PURPLE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes/purple"));
        public static final TagKey<Item> GLASS_PANES_RED = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes/red"));
        public static final TagKey<Item> GLASS_PANES_WHITE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes/white"));
        public static final TagKey<Item> GLASS_PANES_YELLOW = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass_panes/yellow"));
        public static final TagKey<Item> GLASS_PINK = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/pink"));
        public static final TagKey<Item> GLASS_PURPLE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/purple"));
        public static final TagKey<Item> GLASS_RED = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/red"));
        public static final TagKey<Item> GLASS_SILICA = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/silica"));
        public static final TagKey<Item> GLASS_WHITE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/white"));
        public static final TagKey<Item> GLASS_YELLOW = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "glass/yellow"));
        public static final TagKey<Item> GRAVEL = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "gravel"));
        public static final TagKey<Item> GUNPOWDER = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "gunpowder"));
        public static final TagKey<Item> INGOTS_BRICK = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ingots/brick"));
        public static final TagKey<Item> INGOTS_COPPER = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ingots/copper"));
        public static final TagKey<Item> INGOTS_GOLD = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ingots/gold"));
        public static final TagKey<Item> INGOTS_IRON = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ingots/iron"));
        public static final TagKey<Item> INGOTS_NETHERITE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ingots/netherite"));
        public static final TagKey<Item> LEATHER = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "leather"));
        public static final TagKey<Item> MUSHROOMS = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "mushrooms"));
        public static final TagKey<Item> NETHER_STARS = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "nether_stars"));
        public static final TagKey<Item> OBSIDIAN = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "obsidian"));
        public static final TagKey<Item> ORES_COPPER = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ores/copper"));
        public static final TagKey<Item> ORES_GOLD = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ores/gold"));
        public static final TagKey<Item> ORES_IRON = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ores/iron"));
        public static final TagKey<Item> ORES_COAL = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ores/coal"));
        public static final TagKey<Item> ORES_DIAMOND = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ores/diamond"));
        public static final TagKey<Item> ORES_EMERALD = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ores/emerald"));
        public static final TagKey<Item> ORES_LAPIS = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ores/lapis"));
        public static final TagKey<Item> ORES_NETHERITE_SCRAP = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ores/netherite_scrap"));
        public static final TagKey<Item> ORES_QUARTZ = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ores/quartz"));
        public static final TagKey<Item> ORES_REDSTONE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "ores/redstone"));
        public static final TagKey<Item> RAW_MATERIALS_COPPER = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "raw_materials/copper"));
        public static final TagKey<Item> RAW_MATERIALS_GOLD = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "raw_materials/gold"));
        public static final TagKey<Item> RAW_MATERIALS_IRON = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "raw_materials/iron"));
        public static final TagKey<Item> RODS_WOODEN = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "rods/wooden"));
        public static final TagKey<Item> SAND = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "sand"));
        public static final TagKey<Item> SAND_COLORLESS = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "sand/colorless"));
        public static final TagKey<Item> SLIMEBALLS = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "slimeballs"));
        public static final TagKey<Item> STORAGE_BLOCKS_COAL = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "storage_blocks/coal"));
        public static final TagKey<Item> STORAGE_BLOCKS_DIAMOND = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "storage_blocks/diamond"));
        public static final TagKey<Item> STORAGE_BLOCKS_LAPIS = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "storage_blocks/lapis"));
        public static final TagKey<Item> STORAGE_BLOCKS_QUARTZ = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "storage_blocks/quartz"));
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_COPPER = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "storage_blocks/raw_copper"));
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_GOLD = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "storage_blocks/raw_gold"));
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_IRON = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "storage_blocks/raw_iron"));
        public static final TagKey<Item> STORAGE_BLOCKS_REDSTONE = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "storage_blocks/redstone"));
        public static final TagKey<Item> STRING = TagKey.create(Registries.ITEM, new ResourceLocation("forge", "string"));

        public static TagKey<Item> dye(DyeColor color) {
            return TagKey.create(Registries.ITEM, new ResourceLocation("forge", "dyes/" + color.getName()));
        }

        private Items() {
        }
    }
}
