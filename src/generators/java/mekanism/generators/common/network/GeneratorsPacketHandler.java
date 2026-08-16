package mekanism.generators.common.network;

import mekanism.common.network.BasePacketHandler;
import mekanism.common.network.FabricPacketChannel;
import mekanism.generators.common.MekanismGenerators;
import mekanism.generators.common.network.to_server.PacketGeneratorsGuiButtonPress;
import mekanism.generators.common.network.to_server.PacketGeneratorsGuiInteract;

public class GeneratorsPacketHandler extends BasePacketHandler {

    private final FabricPacketChannel netHandler = createChannel(MekanismGenerators.rl(MekanismGenerators.MODID), MekanismGenerators.instance.versionNumber);

    @Override
    protected FabricPacketChannel getChannel() {
        return netHandler;
    }

    @Override
    public void initialize() {
        //Client to server messages
        registerClientToServer(PacketGeneratorsGuiButtonPress.class, PacketGeneratorsGuiButtonPress::decode);
        registerClientToServer(PacketGeneratorsGuiInteract.class, PacketGeneratorsGuiInteract::decode);
        //Server to client messages
        getChannel().registerReceivers();
    }
}