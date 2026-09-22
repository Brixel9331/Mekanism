package mekanism.common.network;

import mekanism.api.fluid.FluidStack;
import net.minecraft.network.FriendlyByteBuf;

public final class FluidStackPacketCodec {

    private FluidStackPacketCodec() {
    }

    public static void write(FriendlyByteBuf buffer, FluidStack stack) {
        boolean present = !stack.isEmpty();
        buffer.writeBoolean(present);
        if (present) {
            stack.writeToPacket(buffer);
        }
    }

    public static FluidStack read(FriendlyByteBuf buffer) {
        return buffer.readBoolean() ? FluidStack.readFromPacket(buffer) : FluidStack.EMPTY;
    }
}
