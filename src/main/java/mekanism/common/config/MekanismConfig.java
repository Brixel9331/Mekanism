package mekanism.common.config;


public class MekanismConfig {

    private MekanismConfig() {
    }

    public static final ClientConfig client = new ClientConfig();
    public static final CommonConfig common = new CommonConfig();
    public static final GeneralConfig general = new GeneralConfig();
    public static final GearConfig gear = new GearConfig();
    public static final StorageConfig storage = new StorageConfig();
    public static final TierConfig tiers = new TierConfig();
    public static final UsageConfig usage = new UsageConfig();
    public static final WorldConfig world = new WorldConfig();

    public static void registerConfigs() {
        MekanismConfigHelper.registerConfig("mekanism", client);
        MekanismConfigHelper.registerConfig("mekanism", common);
        MekanismConfigHelper.registerConfig("mekanism", general);
        MekanismConfigHelper.registerConfig("mekanism", gear);
        MekanismConfigHelper.registerConfig("mekanism", storage);
        MekanismConfigHelper.registerConfig("mekanism", tiers);
        MekanismConfigHelper.registerConfig("mekanism", usage);
        MekanismConfigHelper.registerConfig("mekanism", world);
    }
}