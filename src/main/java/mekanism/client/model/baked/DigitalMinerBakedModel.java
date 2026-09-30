package mekanism.client.model.baked;

import mekanism.api.annotations.NothingNullByDefault;
import mekanism.client.render.lib.QuadTransformation;
import mekanism.client.render.lib.QuadTransformation.TextureFilteredTransformation;
import mekanism.common.Mekanism;
import mekanism.common.base.HolidayManager;
import mekanism.common.config.MekanismConfig;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import mekanism.common.lib.Lazy;
import org.jetbrains.annotations.Nullable;

@NothingNullByDefault
public class DigitalMinerBakedModel extends ExtensionBakedModel {

    @Nullable
    private static TextureAtlasSprite AFD_SAD, AFD_TEXT, MAY_4TH;

    public static void onStitch(TextureAtlas atlas) {
        AFD_SAD = atlas.getSprite(Mekanism.rl("block/models/digital_miner_screen_afd_sad"));
        AFD_TEXT = atlas.getSprite(Mekanism.rl("block/models/digital_miner_screen_afd_text"));
        MAY_4TH = atlas.getSprite(Mekanism.rl("block/models/digital_miner_screen_may4th"));
    }

    private final Lazy<QuadTransformation> APRIL_FOOLS_TRANSFORM = Lazy.of(() -> QuadTransformation.list(
          TextureFilteredTransformation.of(QuadTransformation.texture(AFD_SAD), s -> s.getPath().contains("screen_hello") || s.getPath().contains("screen_cmd")),
          TextureFilteredTransformation.of(QuadTransformation.texture(AFD_TEXT), s -> s.getPath().contains("screen_blank"))
    ));
    private final Lazy<QuadTransformation> MAY_4TH_TRANSFORM = Lazy.of(() -> TextureFilteredTransformation.of(QuadTransformation.texture(MAY_4TH),
          s -> s.getPath().contains("screen_hello")));

    public DigitalMinerBakedModel(BakedModel original) {
        super(original);
    }

    @Nullable
    @Override
    protected QuadTransformation getTransformation() {
        if (MekanismConfig.client.holidays.get()) {
            if (HolidayManager.MAY_4.isToday()) {
                return MAY_4TH_TRANSFORM.get();
            } else if (HolidayManager.APRIL_FOOLS.isToday()) {
                return APRIL_FOOLS_TRANSFORM.get();
            }
        }
        return null;
    }

}
