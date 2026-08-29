package mekanism.common;

import mekanism.common.recipe.MekanismRecipeType;
import mekanism.common.recipe.RecipeLifecycle;

public class ReloadListener {

    private boolean registered;

    public void register() {
        if (!registered) {
            RecipeLifecycle.SERVER_UPDATED.register((server, initial) -> {
                CommonWorldTickHandler.flushTagAndRecipeCaches = true;
                MekanismRecipeType.clearCache();
            });
            RecipeLifecycle.CLIENT_UPDATED.register(this::clientRecipesUpdated);
            registered = true;
        }
    }

    private void clientRecipesUpdated() {
        //Note: Dedicated servers first connection the server sends recipes then tags, and on reload sends tags then recipes.
        // We ignore this fact and only clear the cache in the recipes updated event however, as the cache should already be
        // empty on our initial connection, and even if it isn't the client has no way to query the recipes and cause the
        // caches to be initialized before the tags are then received as we lazily initialize our recipe caches.
        MekanismRecipeType.clearCache();
    }
}
