package de.fanta.cubeside.event;

import de.fanta.cubeside.CubesideClientFabric;
import de.fanta.cubeside.packets.CubesideDataS2C;
import de.fanta.cubeside.packets.CubesideDataS2C.ScreenFlashInfo;
import de.fanta.cubeside.util.FlashColorScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class CubesideModChannelHandler implements ClientPlayNetworking.PlayPayloadHandler<CubesideDataS2C>, ClientConfigurationNetworking.ConfigurationPayloadHandler<CubesideDataS2C> {

    public CubesideModChannelHandler() {
        PayloadTypeRegistry.clientboundPlay().register(CubesideDataS2C.PACKET_ID, CubesideDataS2C.PACKET_CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(CubesideDataS2C.PACKET_ID, CubesideDataS2C.PACKET_CODEC);

        ClientPlayNetworking.registerGlobalReceiver(CubesideDataS2C.PACKET_ID, this);
        ClientConfigurationNetworking.registerGlobalReceiver(CubesideDataS2C.PACKET_ID, this);
    }

    @Override
    public void receive(CubesideDataS2C payload, ClientConfigurationNetworking.Context context) {
        receive(payload);
    }

    @Override
    public void receive(CubesideDataS2C payload, ClientPlayNetworking.Context context) {
        receive(payload);
    }

    public void receive(CubesideDataS2C data) {
        if (data.getChatInfo() != null) {
            CubesideClientFabric.setChatInfo(data.getChatInfo());
        }
        if (data.getScreenFlashInfo() != null) {
            ScreenFlashInfo screenFlash = data.getScreenFlashInfo();
            FlashColorScreen.flashColoredScreen(screenFlash.duration(), screenFlash.color());
        }
    }
}
