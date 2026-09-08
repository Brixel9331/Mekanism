package mekanism.common.item.block;

import mekanism.common.block.basic.BlockResource;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.world.item.Item;

public class ItemBlockResource extends ItemBlockMekanism<BlockResource> {

    public ItemBlockResource(BlockResource block, Item.Properties properties) {
        super(block, properties);
        int burnTime = block.getResourceInfo().getBurnTime();
        if (burnTime > 0) {
            FuelRegistry.INSTANCE.add(this, burnTime);
        }
    }
}
