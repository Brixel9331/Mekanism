package mekanism.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import mekanism.api.annotations.ParametersAreNotNullByDefault;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

@ParametersAreNotNullByDefault
public class MekKeyBinding extends KeyMapping {

    @Nullable
    private final BiConsumer<KeyMapping, Boolean> onKeyDown;
    @Nullable
    private final Consumer<KeyMapping> onKeyUp;
    @Nullable
    private final BooleanSupplier toggleable;
    private final boolean repeating;
    private boolean lastState;
    private boolean physicalDown;
    private final IKeyConflictContext keyConflictContext;
    private final KeyModifier defaultKeyModifier;
    private KeyModifier keyModifier;

    MekKeyBinding(String description, IKeyConflictContext keyConflictContext, KeyModifier keyModifier, InputConstants.Key key, String category,
          @Nullable BiConsumer<KeyMapping, Boolean> onKeyDown, @Nullable Consumer<KeyMapping> onKeyUp, @Nullable BooleanSupplier toggleable, boolean repeating) {
        super(description, key.getType(), key.getValue(), category);
        this.keyConflictContext = keyConflictContext;
        this.defaultKeyModifier = keyModifier.matches(key) ? KeyModifier.NONE : keyModifier;
        this.keyModifier = defaultKeyModifier;
        this.onKeyDown = onKeyDown;
        this.onKeyUp = onKeyUp;
        this.toggleable = toggleable;
        this.repeating = repeating;
    }

    private boolean isToggleable() {
        return toggleable != null && toggleable.getAsBoolean();
    }

    @Override
    public void setDown(boolean value) {
        if (isToggleable()) {
            //If it is a toggleable keybinding mimic the behavior of vanilla's toggleable keybinding
            if (value && !physicalDown && isConflictContextAndModifierActive()) {
                super.setDown(!super.isDown());
            }
        } else {
            super.setDown(value);
        }
        physicalDown = value;
        //Note: We check the state based on isDown instead of value, as the value may be wrong depending on the conflict context
        boolean state = isDown();
        if (state != lastState || (state && repeating)) {
            if (state) {
                if (onKeyDown != null) {
                    onKeyDown.accept(this, lastState);
                }
            } else if (onKeyUp != null) {
                onKeyUp.accept(this);
            }
            lastState = state;
        }
    }

    @Override
    public boolean isDown() {
        return super.isDown() && (isConflictContextAndModifierActive() || isToggleable());
    }

    public IKeyConflictContext getKeyConflictContext() {
        return keyConflictContext;
    }

    public KeyModifier getKeyModifier() {
        return keyModifier;
    }

    public InputConstants.Key getKey() {
        return KeyBindingHelper.getBoundKeyOf(this);
    }

    public boolean isConflictContextAndModifierActive() {
        return keyConflictContext.isActive() && keyModifier.isActive(keyConflictContext);
    }

    public void setKeyModifier(KeyModifier modifier) {
        keyModifier = getKey().equals(InputConstants.UNKNOWN) || modifier.matches(getKey()) ? KeyModifier.NONE : modifier;
    }

    public void setToDefault() {
        setKey(getDefaultKey());
        setKeyModifier(defaultKeyModifier);
    }

    @Override
    public boolean isDefault() {
        return super.isDefault() && keyModifier == defaultKeyModifier;
    }

    @Override
    public Component getTranslatedKeyMessage() {
        return keyModifier.combinedName(super.getTranslatedKeyMessage());
    }

    @Override
    public String saveString() {
        return super.saveString() + (keyModifier == KeyModifier.NONE ? "" : ":" + keyModifier.name());
    }

    @Override
    public boolean same(KeyMapping other) {
        IKeyConflictContext otherContext = other instanceof MekKeyBinding mek ? mek.keyConflictContext : KeyConflictContext.UNIVERSAL;
        KeyModifier otherModifier = other instanceof MekKeyBinding mek ? mek.keyModifier : KeyModifier.NONE;
        InputConstants.Key otherKey = KeyBindingHelper.getBoundKeyOf(other);
        if (!keyConflictContext.conflicts(otherContext) && !otherContext.conflicts(keyConflictContext)) {
            return false;
        }
        if (keyModifier.matches(otherKey) || otherModifier.matches(getKey())) {
            return true;
        }
        return getKey().equals(otherKey) && (keyModifier == otherModifier ||
              ((keyConflictContext.conflicts(KeyConflictContext.IN_GAME) || otherContext.conflicts(KeyConflictContext.IN_GAME)) &&
                    (keyModifier == KeyModifier.NONE || otherModifier == KeyModifier.NONE)));
    }

    public static SavedBinding parseSavedBinding(String saved) {
        int separator = saved.indexOf(':');
        if (separator < 0) {
            return new SavedBinding(InputConstants.getKey(saved), KeyModifier.NONE);
        }
        return new SavedBinding(InputConstants.getKey(saved.substring(0, separator)), KeyModifier.parse(saved.substring(separator + 1)));
    }

    public record SavedBinding(InputConstants.Key key, KeyModifier modifier) {
    }
}
