package mekanism.client.lang;

import io.github.fabricators_of_create.porting_lib.data.LanguageProvider;
import java.util.List;
import mekanism.client.lang.FormatSplitter.Component;
import net.minecraft.data.PackOutput;

public abstract class ConvertibleLanguageProvider extends LanguageProvider {

    public ConvertibleLanguageProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    public abstract void convert(String key, List<Component> splitEnglish);

    @Override
    protected void addTranslations() {
    }
}