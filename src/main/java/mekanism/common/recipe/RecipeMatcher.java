package mekanism.common.recipe;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import org.jetbrains.annotations.Nullable;

public final class RecipeMatcher {

    private RecipeMatcher() {
    }

    @Nullable
    public static <T> int[] findMatches(List<T> inputs, List<? extends Predicate<T>> ingredients) {
        int size = inputs.size();
        if (size != ingredients.size()) {
            return null;
        }
        boolean[][] candidates = new boolean[size][size];
        for (int ingredient = 0; ingredient < size; ingredient++) {
            for (int input = 0; input < size; input++) {
                candidates[ingredient][input] = ingredients.get(ingredient).test(inputs.get(input));
            }
        }
        int[] ingredientByInput = new int[size];
        Arrays.fill(ingredientByInput, -1);
        for (int ingredient = 0; ingredient < size; ingredient++) {
            if (!assign(ingredient, candidates, ingredientByInput, new boolean[size])) {
                return null;
            }
        }
        int[] inputByIngredient = new int[size];
        for (int input = 0; input < size; input++) {
            inputByIngredient[ingredientByInput[input]] = input;
        }
        return inputByIngredient;
    }

    private static boolean assign(int ingredient, boolean[][] candidates, int[] ingredientByInput, boolean[] visited) {
        for (int input = 0; input < ingredientByInput.length; input++) {
            if (!visited[input] && candidates[ingredient][input]) {
                visited[input] = true;
                if (ingredientByInput[input] == -1 || assign(ingredientByInput[input], candidates, ingredientByInput, visited)) {
                    ingredientByInput[input] = ingredient;
                    return true;
                }
            }
        }
        return false;
    }
}
