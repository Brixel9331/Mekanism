package mekanism.client.render.hud;

import net.minecraft.util.Mth;

public final class HudLayout {

    private int leftHeight = 39;
    private int rightHeight = 39;

    public void reset() {
        leftHeight = 39;
        rightHeight = 39;
    }

    public void recordHealth(int bottomOffset, float maximumHealth, int absorption, int rowHeight) {
        int rows = Mth.ceil((maximumHealth + absorption) / 20F);
        leftHeight = Math.max(leftHeight, bottomOffset + (rows - 1) * rowHeight + 20);
    }

    public void recordRightSide(int vehicleRows, boolean survival, boolean airVisible) {
        int rows = survival ? Math.max(1, vehicleRows) : vehicleRows;
        rightHeight = Math.max(rightHeight, 39 + rows * 10 + (survival && airVisible ? 10 : 0));
    }

    public int leftHeight() {
        return leftHeight;
    }

    public int rightHeight() {
        return rightHeight;
    }

    public void addLeftHeight(int height) {
        leftHeight += height;
    }

    public int statusOffset(boolean survival, boolean hasMessage) {
        int offset = Math.max(59, Math.max(leftHeight, rightHeight));
        if (!survival) {
            //Same shift as done in Gui#renderSelectedItemName
            offset -= 14;
        } else if (hasMessage) {
            //If we are in survival though that means our thing will end up intersecting the subtitle text if there is any,
            // so we need to check if there is, and if so shift our target further
            offset += 14;
        }
        //Shift the rendering to be above the previous line
        return offset + 13;
    }
}
