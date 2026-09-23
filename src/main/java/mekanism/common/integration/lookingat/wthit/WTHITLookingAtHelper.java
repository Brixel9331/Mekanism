package mekanism.common.integration.lookingat.wthit;

import java.util.ArrayList;
import java.util.List;
import mcp.mobius.waila.api.IData;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.ChemicalType;
import mekanism.api.chemical.ChemicalUtils;
import mekanism.api.fluid.FluidStack;
import mekanism.api.math.FloatingLong;
import mekanism.common.integration.lookingat.LookingAtHelper;
import mekanism.common.network.FluidStackPacketCodec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class WTHITLookingAtHelper implements LookingAtHelper, IData {

    public static final IData.Serializer<WTHITLookingAtHelper> SERIALIZER = buffer -> {
        WTHITLookingAtHelper helper = new WTHITLookingAtHelper();
        int count = buffer.readVarInt();
        for (int i = 0; i < count; i++) {
            LookingAtTypes type = buffer.readEnum(LookingAtTypes.class);
            Object element = switch (type) {
                case UNKNOWN -> null;
                case ENERGY -> new EnergyData(FloatingLong.readFromBuffer(buffer), FloatingLong.readFromBuffer(buffer));
                case FLUID -> new FluidData(FluidStackPacketCodec.read(buffer), buffer.readVarInt());
                case GAS -> new ChemicalData(ChemicalUtils.readGasStack(buffer), buffer.readVarLong());
                case INFUSION -> new ChemicalData(ChemicalUtils.readInfusionStack(buffer), buffer.readVarLong());
                case PIGMENT -> new ChemicalData(ChemicalUtils.readPigmentStack(buffer), buffer.readVarLong());
                case SLURRY -> new ChemicalData(ChemicalUtils.readSlurryStack(buffer), buffer.readVarLong());
                case COMPONENT -> buffer.readComponent();
            };
            if (element != null) {
                helper.elements.add(element);
            }
        }
        return helper;
    };

    final List<Object> elements = new ArrayList<>();

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeCollection(elements, (buf, object) -> {
            LookingAtTypes type = LookingAtTypes.getType(object);
            buf.writeEnum(type);
            switch (type) {
                case ENERGY -> {
                    EnergyData energy = (EnergyData) object;
                    energy.stored().writeToBuffer(buf);
                    energy.capacity().writeToBuffer(buf);
                }
                case FLUID -> {
                    FluidData fluid = (FluidData) object;
                    FluidStackPacketCodec.write(buf, fluid.stored());
                    buf.writeVarInt(fluid.capacity());
                }
                case GAS, INFUSION, PIGMENT, SLURRY -> {
                    ChemicalData chemical = (ChemicalData) object;
                    ChemicalUtils.writeChemicalStack(buf, chemical.stored());
                    buf.writeVarLong(chemical.capacity());
                }
                case COMPONENT -> buf.writeComponent((Component) object);
            }
        });
    }

    @Override
    public void addText(Component text) {
        elements.add(text);
    }

    @Override
    public void addEnergyElement(FloatingLong energy, FloatingLong maxEnergy) {
        elements.add(new EnergyData(energy.copyAsConst(), maxEnergy.copyAsConst()));
    }

    @Override
    public void addFluidElement(FluidStack stored, int capacity) {
        elements.add(new FluidData(stored.copy(), capacity));
    }

    @Override
    public void addChemicalElement(ChemicalStack<?> stored, long capacity) {
        elements.add(new ChemicalData(stored.copy(), capacity));
    }

    record EnergyData(FloatingLong stored, FloatingLong capacity) {
    }

    record FluidData(FluidStack stored, int capacity) {
    }

    record ChemicalData(ChemicalStack<?> stored, long capacity) {
    }

    private enum LookingAtTypes {
        UNKNOWN,
        ENERGY,
        FLUID,
        GAS, INFUSION, PIGMENT, SLURRY,
        COMPONENT;

        public static LookingAtTypes getType(Object element) {
            if (element instanceof Component) {
                return COMPONENT;
            } else if (element instanceof EnergyData) {
                return ENERGY;
            } else if (element instanceof FluidData) {
                return FLUID;
            } else if (element instanceof ChemicalData chemical) {
                return switch (ChemicalType.getTypeFor(chemical.stored())) {
                    case GAS -> GAS;
                    case INFUSION -> INFUSION;
                    case PIGMENT -> PIGMENT;
                    case SLURRY -> SLURRY;
                };
            }
            return UNKNOWN;
        }
    }
}
