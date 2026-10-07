package mekanism.common.tag;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;

public final class ConventionalTagData {

    private ConventionalTagData() {
    }

    public static void extend(ResourceKey<? extends Registry<?>> registry, Map<TagKey<?>, TagBuilder> tags, boolean includeVanilla) {
        if (!registry.equals(Registries.ITEM) && !registry.equals(Registries.BLOCK) && !registry.equals(Registries.FLUID)) {
            return;
        }
        Map<ResourceLocation, TagBuilder> legacy = new LinkedHashMap<>();
        tags.forEach((tag, builder) -> {
            if (tag.location().getNamespace().equals("forge")) {
                legacy.put(tag.location(), builder);
            }
        });
        if (includeVanilla) {
            if (registry.equals(Registries.ITEM)) {
                addItems(legacy);
                addBlocks(legacy);
            } else if (registry.equals(Registries.BLOCK)) {
                addBlocks(legacy);
            }
        }
        Map<ResourceLocation, TagBuilder> conventional = new LinkedHashMap<>();
        legacy.forEach((id, builder) -> {
            TagBuilder target = conventional.computeIfAbsent(conventionalId(id), ignored -> TagBuilder.create());
            for (TagEntry entry : builder.build()) {
                List<ResourceLocation> required = new ArrayList<>();
                List<ResourceLocation> optional = new ArrayList<>();
                entry.visitRequiredDependencies(required::add);
                entry.visitOptionalDependencies(optional::add);
                if (!required.isEmpty()) {
                    target.addTag(conventionalId(required.get(0)));
                } else if (!optional.isEmpty()) {
                    target.addOptionalTag(conventionalId(optional.get(0)));
                } else {
                    target.add(entry);
                }
            }
        });
        conventional.forEach((id, builder) -> tags.put(TagKey.create(ResourceKey.createRegistryKey(registry.location()), id), builder));
        legacy.forEach((id, builder) -> {
            if (!id.getPath().equals("tools/bows")) {
                builder.addOptionalTag(conventionalId(id));
            }
            tags.put(TagKey.create(ResourceKey.createRegistryKey(registry.location()), id), builder);
        });
    }

    public static ResourceLocation conventionalId(ResourceLocation legacy) {
        if (!legacy.getNamespace().equals("forge")) {
            return legacy;
        }
        String path = legacy.getPath();
        String[] parts = path.split("/", 2);
        String mapped = switch (parts[0]) {
            case "raw_materials" -> parts.length == 1 ? "raw_ores" : "raw_" + parts[1] + "_ores";
            case "storage_blocks" -> parts.length == 1 ? "storage_blocks" : parts[1] + "_blocks";
            case "ingots", "nuggets", "dusts", "dyes", "rods" -> parts.length == 1 ? path : parts[1] + "_" + parts[0];
            case "ores" -> parts.length == 1 ? path : (parts[1].equals("netherite_scrap") ? "netherite" : parts[1]) + "_ores";
            case "gems" -> parts.length == 1 ? path : switch (parts[1]) {
                case "diamond" -> "diamonds";
                case "emerald" -> "emeralds";
                case "lapis", "quartz", "amethyst", "certus_quartz" -> parts[1];
                default -> parts[1] + "_gems";
            };
            case "glass" -> parts.length == 1 ? "glass_blocks" : parts[1] + "_glass_blocks";
            case "glass_panes", "chests", "barrels", "fences", "fence_gates" -> parts.length == 1 ? path : parts[1] + "_" + parts[0];
            case "tools" -> parts.length == 1 ? path : parts[1].equals("wrench") ? "wrenches" : parts[1];
            default -> path;
        };
        return new ResourceLocation("c", mapped);
    }

    private static void addItems(Map<ResourceLocation, TagBuilder> tags) {
        for (String material : List.of("iron", "gold", "copper", "netherite")) {
            add(tags, "ingots/" + material, material + "_ingot");
            nest(tags, "ingots", "ingots/" + material);
        }
        add(tags, "ingots/brick", "brick");
        add(tags, "ingots/nether_brick", "nether_brick");
        nest(tags, "ingots", "ingots/brick", "ingots/nether_brick");
        for (String material : List.of("iron", "gold")) {
            add(tags, "nuggets/" + material, material + "_nugget");
            nest(tags, "nuggets", "nuggets/" + material);
        }
        for (String material : List.of("iron", "gold", "copper")) {
            add(tags, "raw_materials/" + material, "raw_" + material);
            nest(tags, "raw_materials", "raw_materials/" + material);
        }
        add(tags, "dusts/redstone", "redstone");
        add(tags, "dusts/glowstone", "glowstone_dust");
        add(tags, "dusts/prismarine", "prismarine_shard");
        nest(tags, "dusts", "dusts/redstone", "dusts/glowstone", "dusts/prismarine");
        for (String gem : List.of("diamond", "emerald", "quartz", "lapis", "amethyst")) {
            add(tags, "gems/" + gem, gem.equals("lapis") ? "lapis_lazuli" : gem.equals("amethyst") ? "amethyst_shard" : gem);
            nest(tags, "gems", "gems/" + gem);
        }
        add(tags, "gems/prismarine", "prismarine_crystals");
        nest(tags, "gems", "gems/prismarine");
        for (DyeColor color : DyeColor.values()) {
            add(tags, "dyes/" + color.getName(), color.getName() + "_dye");
            nest(tags, "dyes", "dyes/" + color.getName());
        }
        add(tags, "rods/wooden", "stick");
        add(tags, "rods/blaze", "blaze_rod");
        nest(tags, "rods", "rods/wooden", "rods/blaze");
        add(tags, "string", "string");
        add(tags, "slimeballs", "slime_ball");
        add(tags, "leather", "leather");
        add(tags, "nether_stars", "nether_star");
        add(tags, "gunpowder", "gunpowder");
        add(tags, "ender_pearls", "ender_pearl");
        add(tags, "bones", "bone");
        add(tags, "feathers", "feather");
        add(tags, "mushrooms", "red_mushroom", "brown_mushroom");
        add(tags, "tools/bows", "bow");
        add(tags, "tools/crossbows", "crossbow");
        add(tags, "tools/fishing_rods", "fishing_rod");
        add(tags, "tools/tridents", "trident");
        add(tags, "tools/shields", "shield");
        nest(tags, "tools", "tools/bows", "tools/crossbows", "tools/fishing_rods", "tools/tridents", "tools/shields");
        for (String tool : List.of("swords", "axes", "pickaxes", "shovels", "hoes")) {
            builder(tags, "tools").addTag(new ResourceLocation("minecraft", tool));
        }
        for (String material : List.of("leather", "chainmail", "iron", "golden", "diamond", "netherite")) {
            add(tags, "armors/helmets", material + "_helmet");
            add(tags, "armors/chestplates", material + "_chestplate");
            add(tags, "armors/leggings", material + "_leggings");
            add(tags, "armors/boots", material + "_boots");
        }
        add(tags, "armors/helmets", "turtle_helmet");
        nest(tags, "armors", "armors/helmets", "armors/chestplates", "armors/leggings", "armors/boots");
        builder(tags, "gems/certus_quartz");
        add(tags, "crops/wheat", "wheat");
        add(tags, "crops/carrot", "carrot");
        add(tags, "crops/potato", "potato");
        add(tags, "crops/beetroot", "beetroot");
        add(tags, "crops/nether_wart", "nether_wart");
        nest(tags, "crops", "crops/wheat", "crops/carrot", "crops/potato", "crops/beetroot", "crops/nether_wart");
    }

    private static void addBlocks(Map<ResourceLocation, TagBuilder> tags) {
        for (String material : List.of("iron", "gold", "copper", "redstone", "lapis", "coal", "diamond", "emerald")) {
            builder(tags, "ores/" + material).addTag(new ResourceLocation("minecraft", material + "_ores"));
            nest(tags, "ores", "ores/" + material);
            add(tags, "ores_in_ground/stone", material + "_ore");
            add(tags, "ores_in_ground/deepslate", "deepslate_" + material + "_ore");
            String rate = switch (material) {
                case "copper", "redstone", "lapis" -> "dense";
                default -> "singular";
            };
            add(tags, "ore_rates/" + rate, material + "_ore", "deepslate_" + material + "_ore");
        }
        add(tags, "ores/quartz", "nether_quartz_ore");
        add(tags, "ores/netherite_scrap", "ancient_debris");
        nest(tags, "ores", "ores/quartz", "ores/netherite_scrap");
        add(tags, "ore_rates/singular", "nether_quartz_ore", "ancient_debris");
        add(tags, "ore_rates/sparse", "nether_gold_ore");
        add(tags, "ores_in_ground/netherrack", "nether_quartz_ore", "nether_gold_ore");
        for (String material : List.of("iron", "gold", "copper", "netherite", "redstone", "lapis", "coal", "diamond", "emerald", "quartz", "amethyst",
              "raw_iron", "raw_gold", "raw_copper")) {
            add(tags, "storage_blocks/" + material, material + "_block");
            nest(tags, "storage_blocks", "storage_blocks/" + material);
        }
        add(tags, "cobblestone/normal", "cobblestone");
        add(tags, "cobblestone/deepslate", "cobbled_deepslate");
        add(tags, "cobblestone/mossy", "mossy_cobblestone");
        add(tags, "cobblestone/infested", "infested_cobblestone");
        nest(tags, "cobblestone", "cobblestone/normal", "cobblestone/deepslate", "cobblestone/mossy", "cobblestone/infested");
        add(tags, "sand/colorless", "sand");
        add(tags, "sand/red", "red_sand");
        nest(tags, "sand", "sand/colorless", "sand/red");
        add(tags, "gravel", "gravel");
        add(tags, "obsidian", "obsidian");
        add(tags, "stone", "stone", "granite", "diorite", "andesite", "deepslate", "tuff", "polished_granite", "polished_diorite",
              "polished_andesite", "polished_deepslate", "infested_stone", "infested_deepslate");
        add(tags, "netherrack", "netherrack");
        add(tags, "end_stones", "end_stone");
        add(tags, "glass/colorless", "glass");
        add(tags, "glass/tinted", "tinted_glass");
        add(tags, "glass_panes/colorless", "glass_pane");
        nest(tags, "glass/silica", "glass/colorless");
        nest(tags, "glass", "glass/silica", "glass/tinted");
        nest(tags, "glass_panes", "glass_panes/colorless");
        for (DyeColor color : DyeColor.values()) {
            add(tags, "glass/" + color.getName(), color.getName() + "_stained_glass");
            nest(tags, "glass/silica", "glass/" + color.getName());
            add(tags, "glass_panes/" + color.getName(), color.getName() + "_stained_glass_pane");
            nest(tags, "glass_panes", "glass_panes/" + color.getName());
        }
        add(tags, "bookshelves", "bookshelf");
        add(tags, "barrels/wooden", "barrel");
        nest(tags, "barrels", "barrels/wooden");
        add(tags, "chests/wooden", "chest", "trapped_chest");
        add(tags, "chests/trapped", "trapped_chest");
        add(tags, "chests/ender", "ender_chest");
        nest(tags, "chests", "chests/wooden", "chests/ender");
        builder(tags, "fences/wooden").addTag(new ResourceLocation("minecraft", "wooden_fences"));
        add(tags, "fences/nether_brick", "nether_brick_fence");
        nest(tags, "fences", "fences/wooden", "fences/nether_brick");
        for (String wood : List.of("oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "mangrove", "cherry", "bamboo", "crimson", "warped")) {
            add(tags, "fence_gates/wooden", wood + "_fence_gate");
        }
        nest(tags, "fence_gates", "fence_gates/wooden");
    }

    private static TagBuilder builder(Map<ResourceLocation, TagBuilder> tags, String path) {
        return tags.computeIfAbsent(new ResourceLocation("forge", path), ignored -> TagBuilder.create());
    }

    private static void add(Map<ResourceLocation, TagBuilder> tags, String path, String... elements) {
        TagBuilder builder = builder(tags, path);
        for (String element : elements) {
            builder.addElement(new ResourceLocation("minecraft", element));
        }
    }

    private static void nest(Map<ResourceLocation, TagBuilder> tags, String path, String... children) {
        TagBuilder builder = builder(tags, path);
        for (String child : children) {
            builder.addTag(new ResourceLocation("forge", child));
        }
    }
}
