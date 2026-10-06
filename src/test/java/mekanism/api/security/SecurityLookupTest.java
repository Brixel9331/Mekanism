package mekanism.api.security;

import java.util.UUID;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityLookupTest {

    private static final OwnedObject OWNER_ONLY = new OwnedObject();
    private static final OwnedObject SECURED = new OwnedObject();

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        SecurityLookup.OWNER.item().registerForItems((stack, context) -> OWNER_ONLY, Items.STICK);
        SecurityLookup.OWNER.item().registerForItems((stack, context) -> SECURED, Items.DIAMOND);
        SecurityLookup.SECURITY.item().registerForItems((stack, context) -> SECURED, Items.DIAMOND);
        SecurityLookup.OWNER.entity().registerForTypes((entity, context) -> OWNER_ONLY, EntityType.ARMOR_STAND);
    }

    @Test
    void ownerOnlyExposureDoesNotInferSecurityFromItsJavaType() {
        ItemStack stack = new ItemStack(Items.STICK);
        assertSame(OWNER_ONLY, SecurityLookup.OWNER.find(stack).orElseThrow());
        assertTrue(SecurityLookup.SECURITY.find(stack).isEmpty());
        UUID owner = UUID.randomUUID();
        SecurityLookup.OWNER.find(stack).orElseThrow().setOwnerUUID(owner);
        assertEquals(owner, OWNER_ONLY.getOwnerUUID());
    }

    @Test
    void securityAndOwnershipUseTheirRespectiveLookups() {
        ItemStack stack = new ItemStack(Items.DIAMOND);
        assertSame(SECURED, SecurityLookup.SECURITY.find(stack).orElseThrow());
        assertSame(SECURED, SecurityLookup.OWNER.find(stack).orElseThrow());
        assertTrue(SecurityLookup.OWNER.find(new ItemStack(Items.DIRT)).isEmpty());
        assertTrue(SecurityLookup.SECURITY.find(new ItemStack(Items.DIRT)).isEmpty());
    }

    @Test
    void entityLookupPreservesOwnerOnlyExposure() {
        ArmorStand entity = new ArmorStand(EntityType.ARMOR_STAND, null);
        assertSame(OWNER_ONLY, SecurityLookup.OWNER.find(entity).orElseThrow());
        assertTrue(SecurityLookup.SECURITY.find(entity).isEmpty());
    }

    @Test
    void emptyAndMissingTargetsHaveNoExposedObjects() {
        assertTrue(SecurityLookup.OWNER.find(null).isEmpty());
        assertTrue(SecurityLookup.SECURITY.find(ItemStack.EMPTY).isEmpty());
        assertTrue(SecurityLookup.OWNER.find(new ItemStack(Items.STICK, 0)).isEmpty());
    }

    @Test
    void invalidTargetsCannotSilentlyLoseTheirProtection() {
        assertThrows(IllegalArgumentException.class, () -> SecurityLookup.OWNER.find(OWNER_ONLY));
        ChestBlockEntity detached = new ChestBlockEntity(BlockPos.ZERO, Blocks.CHEST.defaultBlockState());
        assertThrows(NullPointerException.class, () -> SecurityLookup.SECURITY.find(detached));
    }

    private static final class OwnedObject implements ISecurityObject {

        private UUID owner;
        private SecurityMode mode = SecurityMode.PRIVATE;

        @Override
        public UUID getOwnerUUID() {
            return owner;
        }

        @Override
        public String getOwnerName() {
            return null;
        }

        @Override
        public void setOwnerUUID(UUID owner) {
            this.owner = owner;
        }

        @Override
        public SecurityMode getSecurityMode() {
            return mode;
        }

        @Override
        public void setSecurityMode(SecurityMode mode) {
            this.mode = mode;
        }

        @Override
        public void onSecurityChanged(SecurityMode old, SecurityMode mode) {
        }
    }
}
