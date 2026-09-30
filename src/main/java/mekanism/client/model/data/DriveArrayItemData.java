package mekanism.client.model.data;

import java.util.List;
import mekanism.api.NBTConstants;
import mekanism.api.inventory.IInventorySlot;
import mekanism.common.content.qio.IQIODriveItem;
import mekanism.common.content.qio.IQIODriveItem.DriveMetadata;
import mekanism.common.item.interfaces.IItemSustainedInventory;
import mekanism.common.lib.frequency.Frequency.FrequencyIdentity;
import mekanism.common.lib.frequency.FrequencyType;
import mekanism.common.lib.render.DriveArrayModelData;
import mekanism.common.recipe.upgrade.ItemRecipeData;
import mekanism.common.tile.qio.TileEntityQIODriveArray.DriveStatus;
import mekanism.common.util.ItemDataUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public final class DriveArrayItemData {

    private DriveArrayItemData() {
    }

    public static DriveArrayModelData read(ItemStack stack) {
        if (!(stack.getItem() instanceof IItemSustainedInventory inventoryItem)) {
            return DriveArrayModelData.EMPTY;
        }
        List<IInventorySlot> inventory = ItemRecipeData.readContents(inventoryItem.getSustainedInventory(stack));
        byte[] statuses = new byte[DriveArrayModelData.SLOTS];
        boolean online = hasFrequency(stack);
        for (int slot = 0; slot < Math.min(inventory.size(), statuses.length); slot++) {
            ItemStack drive = inventory.get(slot).getStack();
            if (!drive.isEmpty() && drive.getItem() instanceof IQIODriveItem item) {
                DriveStatus status;
                if (!online) {
                    status = DriveStatus.OFFLINE;
                } else {
                    DriveMetadata metadata = DriveMetadata.load(drive);
                    long capacity = item.getCountCapacity(drive);
                    if (metadata.count() >= capacity) {
                        status = DriveStatus.FULL;
                    } else if (metadata.types() >= item.getTypeCapacity(drive) || metadata.count() >= capacity * 0.75) {
                        status = DriveStatus.NEAR_FULL;
                    } else {
                        status = DriveStatus.READY;
                    }
                }
                statuses[slot] = status.status();
            }
        }
        return DriveArrayModelData.of(statuses);
    }

    private static boolean hasFrequency(ItemStack stack) {
        if (ItemDataUtils.hasData(stack, NBTConstants.COMPONENT_FREQUENCY, Tag.TAG_COMPOUND)) {
            CompoundTag component = ItemDataUtils.getCompound(stack, NBTConstants.COMPONENT_FREQUENCY);
            if (component.contains(FrequencyType.QIO.getName(), Tag.TAG_COMPOUND)) {
                CompoundTag frequency = component.getCompound(FrequencyType.QIO.getName());
                return frequency.hasUUID(NBTConstants.OWNER_UUID) && FrequencyIdentity.load(FrequencyType.QIO, frequency) != null;
            }
        }
        return false;
    }
}
