package mekanism.client.render;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface LivingModelRenderCallback {

    Event<LivingModelRenderCallback> EVENT = EventFactory.createArrayBacked(LivingModelRenderCallback.class,
          listeners -> (entity, model, visibility) -> {
              for (LivingModelRenderCallback listener : listeners) {
                  listener.beforeRender(entity, model, visibility);
              }
          });

    void beforeRender(LivingEntity entity, EntityModel<?> model, ModelVisibility visibility);
}
