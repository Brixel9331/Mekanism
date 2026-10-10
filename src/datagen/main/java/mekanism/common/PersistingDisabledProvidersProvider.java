package mekanism.common;

import com.google.common.hash.Hashing;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;

/**
 * Used for helping to persist specific integrations we have that aren't updated yet.
 */
public class PersistingDisabledProvidersProvider implements DataProvider {

    private static final Set<String> PATHS_TO_SKIP = Set.of(
          //"/scripts/",//CraftTweaker script files
          "/pe_custom_conversions/"//ProjectE custom conversion files
    );

    private final Set<String> compatRecipesToSkip;
    private final Path baseOutputPath;

    public PersistingDisabledProvidersProvider(PackOutput output, Set<String> disabledCompats) {
        baseOutputPath = output.getOutputFolder();
        compatRecipesToSkip = disabledCompats.stream().map(compat -> compat + "/").collect(Collectors.toSet());
    }

    @NotNull
    @Override
    public CompletableFuture<?> run(@NotNull CachedOutput cache) {
        return CompletableFuture.runAsync(() -> {
            if (!Files.isDirectory(baseOutputPath)) {
                return;
            }
            try (var paths = Files.walk(baseOutputPath)) {
                for (Path path : paths.filter(Files::isRegularFile).filter(this::shouldPersist).toList()) {
                    byte[] contents = Files.readAllBytes(path);
                    cache.writeIfNeeded(path, contents, Hashing.sha1().hashBytes(contents));
                }
            } catch (IOException error) {
                throw new CompletionException(error);
            }
        });
    }

    private boolean shouldPersist(Path path) {
        //Get the string representation of the path and sanitize it
        String stringPath = path.toString().replace('\\', '/');
        //Mekanism.logger.info("Evaluating path: {}", stringPath);
        if (PATHS_TO_SKIP.stream().anyMatch(stringPath::contains)) {
            return true;
        }
        int compatIndex = stringPath.indexOf("/recipes/compat/");
        if (compatIndex != -1) {
            //Compat recipes
            String compatPath = stringPath.substring(compatIndex + "/recipes/compat/".length());
            //Mekanism.logger.info("Evaluating compat path: {}", compatPath);
            return compatRecipesToSkip.stream().anyMatch(compatPath::startsWith);
        }
        return false;
    }

    @NotNull
    @Override
    public String getName() {
        return "Persisting disabled provider";
    }

}
