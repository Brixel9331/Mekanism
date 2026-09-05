package mekanism.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public enum KeyModifier {
    NONE,
    SHIFT,
    CONTROL,
    ALT;

    public boolean isActive(IKeyConflictContext context) {
        return switch (this) {
            case NONE -> context.conflicts(KeyConflictContext.IN_GAME) || (!Screen.hasShiftDown() && !Screen.hasControlDown() && !Screen.hasAltDown());
            case SHIFT -> Screen.hasShiftDown();
            case CONTROL -> Screen.hasControlDown();
            case ALT -> Screen.hasAltDown();
        };
    }

    public boolean matches(InputConstants.Key key) {
        if (key.getType() != InputConstants.Type.KEYSYM) {
            return false;
        }
        int code = key.getValue();
        return switch (this) {
            case NONE -> false;
            case SHIFT -> code == GLFW.GLFW_KEY_LEFT_SHIFT || code == GLFW.GLFW_KEY_RIGHT_SHIFT;
            case CONTROL -> Minecraft.ON_OSX ? code == GLFW.GLFW_KEY_LEFT_SUPER || code == GLFW.GLFW_KEY_RIGHT_SUPER :
                  code == GLFW.GLFW_KEY_LEFT_CONTROL || code == GLFW.GLFW_KEY_RIGHT_CONTROL;
            case ALT -> code == GLFW.GLFW_KEY_LEFT_ALT || code == GLFW.GLFW_KEY_RIGHT_ALT;
        };
    }

    public Component combinedName(Component keyName) {
        return switch (this) {
            case NONE -> keyName;
            case SHIFT -> Component.translatable("key.keyboard.left.shift").append(" + ").append(keyName);
            case CONTROL -> Component.translatable(Minecraft.ON_OSX ? "key.keyboard.left.win" : "key.keyboard.left.control").append(" + ").append(keyName);
            case ALT -> Component.translatable("key.keyboard.left.alt").append(" + ").append(keyName);
        };
    }

    public static KeyModifier active() {
        if (Screen.hasShiftDown()) {
            return SHIFT;
        } else if (Screen.hasControlDown()) {
            return CONTROL;
        } else if (Screen.hasAltDown()) {
            return ALT;
        }
        return NONE;
    }

    public static boolean isKeyCodeModifier(InputConstants.Key key) {
        return SHIFT.matches(key) || CONTROL.matches(key) || ALT.matches(key);
    }

    public static KeyModifier parse(String value) {
        try {
            return valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }
}
