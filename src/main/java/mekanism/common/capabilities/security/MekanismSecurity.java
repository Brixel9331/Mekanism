package mekanism.common.capabilities.security;

import mekanism.api.security.IOwnerObject;
import mekanism.api.security.ISecurityObject;
import mekanism.api.security.SecurityLookup;
import mekanism.common.block.attribute.Attribute;
import mekanism.common.block.attribute.AttributeSecurity;
import mekanism.common.capabilities.security.item.ItemStackOwnerObject;
import mekanism.common.capabilities.security.item.ItemStackSecurityObject;
import mekanism.common.entity.EntityRobit;
import mekanism.common.item.ItemPortableQIODashboard;
import mekanism.common.item.ItemPortableTeleporter;
import mekanism.common.item.ItemRobit;
import mekanism.common.item.block.ItemBlockMekanism;
import mekanism.common.lib.security.ISecurityTile;
import mekanism.common.tile.TileEntityBoundingBlock;
import mekanism.common.tile.TileEntitySecurityDesk;
import mekanism.common.tile.interfaces.IBoundingBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public final class MekanismSecurity {

    private static boolean registered;

    private MekanismSecurity() {
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        SecurityLookup.OWNER.item().registerFallback((stack, context) -> itemOwner(stack));
        SecurityLookup.SECURITY.item().registerFallback((stack, context) -> itemSecurity(stack));
        SecurityLookup.OWNER.entity().registerFallback((entity, context) -> entity instanceof EntityRobit robit ? robit : null);
        SecurityLookup.SECURITY.entity().registerFallback((entity, context) -> entity instanceof EntityRobit robit ? robit : null);
        SecurityLookup.OWNER.block().registerFallback((level, pos, state, tile, context) -> blockOwner(tile));
        SecurityLookup.SECURITY.block().registerFallback((level, pos, state, tile, context) -> {
            BlockEntity target = mainTile(tile);
            return target instanceof TileEntitySecurityDesk ? null : blockOwner(target);
        });
        registered = true;
    }

    @Nullable
    private static IOwnerObject itemOwner(ItemStack stack) {
        if (stack.getItem() instanceof ItemPortableTeleporter || stack.getItem() instanceof ItemPortableQIODashboard) {
            return new ItemStackOwnerObject(stack);
        }
        return itemSecurity(stack);
    }

    @Nullable
    private static ISecurityObject itemSecurity(ItemStack stack) {
        if (stack.getItem() instanceof ItemRobit || stack.getItem() instanceof ItemBlockMekanism<?> item && Attribute.has(item.getBlock(), AttributeSecurity.class)) {
            return new ItemStackSecurityObject(stack);
        }
        return null;
    }

    @Nullable
    private static ISecurityObject blockOwner(@Nullable BlockEntity tile) {
        return mainTile(tile) instanceof ISecurityTile security && security.hasSecurity() ? security : null;
    }

    @Nullable
    private static BlockEntity mainTile(@Nullable BlockEntity tile) {
        if (tile instanceof TileEntityBoundingBlock bounding) {
            BlockEntity main = bounding.getMainTile();
            return main instanceof IBoundingBlock && !(main instanceof TileEntityBoundingBlock) ? main : null;
        }
        return tile;
    }
}
