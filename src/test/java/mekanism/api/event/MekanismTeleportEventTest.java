package mekanism.api.event;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MekanismTeleportEventTest {

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void callbacksCanCancelBeforeTeleportAndSkipLaterCallbacks() {
        List<String> calls = new ArrayList<>();
        MekanismTeleportEvent event = new MekanismTeleportEvent(null, 10.5, 64, -12.5);
        MekanismTeleportEvent.BEFORE_TELEPORT.register(received -> {
            if (received == event) {
                calls.add("first");
                received.setCanceled(true);
            }
        });
        MekanismTeleportEvent.BEFORE_TELEPORT.register(received -> {
            if (received == event) {
                calls.add("second");
            }
        });
        assertTrue(event.post());
        assertEquals(List.of("first"), calls);
        assertEquals(new Vec3(10.5, 64, -12.5), event.getTarget());
        assertEquals(10.5, event.getTargetX());
        assertEquals(64, event.getTargetY());
        assertEquals(-12.5, event.getTargetZ());
        assertFalse(new MekanismTeleportEvent(null, 0, 0, 0).post());
    }

    @Test
    void toolEventsPreserveTargetBlockAndToolReferences() {
        BlockHitResult hit = new BlockHitResult(new Vec3(1, 2, 3), Direction.UP, new BlockPos(1, 2, 3), false);
        MekanismTeleportEvent.MekaTool event = new MekanismTeleportEvent.MekaTool(null, 1.5, 3.5, 3.5, ItemStack.EMPTY, hit);
        assertSame(hit, event.getTargetBlock());
        assertSame(ItemStack.EMPTY, event.getMekaTool());
        assertFalse(event.post());
        event.setCanceled(true);
        assertTrue(event.post());
    }
}
