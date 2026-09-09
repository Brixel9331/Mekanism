package mekanism.client.render.hud;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;

@FunctionalInterface
public interface HudRenderCallback {

    Event<HudRenderCallback> BACKGROUND = create();
    Event<HudRenderCallback> HOTBAR = create();
    Event<HudRenderCallback> STATUS_BARS = create();
    Event<HudRenderCallback> SELECTED_ITEM = create();
    Event<Crosshair> CROSSHAIR = EventFactory.createArrayBacked(Crosshair.class, listeners -> minecraft -> {
        for (Crosshair listener : listeners) {
            if (!listener.shouldRender(minecraft)) {
                return false;
            }
        }
        return true;
    });

    void render(Gui gui, GuiGraphics graphics, float partialTick, int width, int height, HudLayout layout);

    private static Event<HudRenderCallback> create() {
        return EventFactory.createArrayBacked(HudRenderCallback.class, listeners -> (gui, graphics, partialTick, width, height, layout) -> {
            for (HudRenderCallback listener : listeners) {
                listener.render(gui, graphics, partialTick, width, height, layout);
            }
        });
    }

    @FunctionalInterface
    interface Crosshair {

        boolean shouldRender(Minecraft minecraft);
    }
}
