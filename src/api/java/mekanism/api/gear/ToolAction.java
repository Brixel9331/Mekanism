package mekanism.api.gear;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ToolAction {

    private static final Map<String, ToolAction> ACTIONS = new ConcurrentHashMap<>();

    public static final ToolAction AXE_DIG = get("axe_dig");
    public static final ToolAction PICKAXE_DIG = get("pickaxe_dig");
    public static final ToolAction SHOVEL_DIG = get("shovel_dig");
    public static final ToolAction HOE_DIG = get("hoe_dig");
    public static final ToolAction SWORD_DIG = get("sword_dig");
    public static final ToolAction SHEARS_DIG = get("shears_dig");
    public static final ToolAction AXE_STRIP = get("axe_strip");
    public static final ToolAction AXE_SCRAPE = get("axe_scrape");
    public static final ToolAction AXE_WAX_OFF = get("axe_wax_off");
    public static final ToolAction SHOVEL_FLATTEN = get("shovel_flatten");
    public static final ToolAction HOE_TILL = get("till");
    public static final ToolAction SHEARS_HARVEST = get("shears_harvest");
    public static final ToolAction SHEARS_CARVE = get("shears_carve");
    public static final ToolAction SHEARS_DISARM = get("shears_disarm");
    public static final ToolAction SHIELD_BLOCK = get("shield_block");

    public static final Set<ToolAction> DEFAULT_AXE_ACTIONS = Set.of(AXE_DIG, AXE_STRIP, AXE_SCRAPE, AXE_WAX_OFF);
    public static final Set<ToolAction> DEFAULT_PICKAXE_ACTIONS = Set.of(PICKAXE_DIG);
    public static final Set<ToolAction> DEFAULT_SHOVEL_ACTIONS = Set.of(SHOVEL_DIG, SHOVEL_FLATTEN);
    public static final Set<ToolAction> DEFAULT_HOE_ACTIONS = Set.of(HOE_DIG, HOE_TILL);
    public static final Set<ToolAction> DEFAULT_SHEARS_ACTIONS = Set.of(SHEARS_DIG, SHEARS_HARVEST, SHEARS_CARVE, SHEARS_DISARM);

    private final String name;

    private ToolAction(String name) {
        this.name = name;
    }

    public static ToolAction get(String name) {
        Objects.requireNonNull(name);
        if (name.isBlank()) {
            throw new IllegalArgumentException("Tool action name must not be blank");
        }
        return ACTIONS.computeIfAbsent(name, ToolAction::new);
    }

    public String name() {
        return name;
    }
}
