package mekanism.common.recipe.condition;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import mekanism.api.MekanismAPI;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class ModVersionLoadedCondition implements ConditionJsonProvider {

    static final ResourceLocation NAME = new ResourceLocation(MekanismAPI.MEKANISM_MODID, "mod_version_loaded");
    private final String minVersion;
    private final String modid;
    private final VersionPredicate predicate;

    public ModVersionLoadedCondition(String modid, String version) {
        this.modid = modid;
        this.minVersion = version;
        try {
            this.predicate = VersionPredicate.parse(">=" + version);
        } catch (VersionParsingException e) {
            throw new JsonParseException("Invalid minimum mod version: " + version, e);
        }
    }

    @Override
    public ResourceLocation getConditionId() {
        return NAME;
    }

    boolean test(Version installedVersion) {
        //They match or we are ahead of the min version
        return predicate.test(installedVersion);
    }

    static boolean test(JsonObject json) {
        ModVersionLoadedCondition condition = new ModVersionLoadedCondition(GsonHelper.getAsString(json, "modid"),
              GsonHelper.getAsString(json, "minVersion"));
        return FabricLoader.getInstance().getModContainer(condition.modid)
              .filter(container -> condition.test(container.getMetadata().getVersion())).isPresent();
    }

    @Override
    public void writeParameters(JsonObject json) {
        json.addProperty("modid", modid);
        json.addProperty("minVersion", minVersion);
    }

    @Override
    public String toString() {
        return "mod_version_loaded(\"" + modid + "\", \"" + minVersion + "\")";
    }
}
