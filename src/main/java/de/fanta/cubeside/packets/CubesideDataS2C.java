package de.fanta.cubeside.packets;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import de.fanta.cubeside.CubesideClientFabric;
import de.fanta.cubeside.util.ChatInfo;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StrictJsonParser;

public class CubesideDataS2C implements CustomPacketPayload {
    public static final Type<CubesideDataS2C> PACKET_ID = new Type<>(ResourceLocation.fromNamespaceAndPath("cubesidemod", "data"));
    public static final StreamCodec<FriendlyByteBuf, CubesideDataS2C> PACKET_CODEC = StreamCodec.ofMember(CubesideDataS2C::write, CubesideDataS2C::new);

    private ChatInfo chatInfo;
    private ScreenFlashInfo screenFlashInfo;

    public CubesideDataS2C(FriendlyByteBuf packet) {
        int globalChatDataChannelID = 0;
        int challengeFlashScreenDataChannelID = 1;
        try {
            int cubesideDateChannel = packet.readByte();
            int cubesideDateChannelVersion = packet.readByte();
            if (cubesideDateChannel == globalChatDataChannelID && cubesideDateChannelVersion == 0) {
                String currentChannelName = packet.readUtf();
                String currentPrivateChat = packet.readUtf();
                String currentResponsePartner = packet.readUtf();
                MutableComponent currentChannelColor = Component.empty();
                MutableComponent currentPrivateChatPrefix = Component.empty();
                MutableComponent currentResponsePartnerPrefix = Component.empty();
                try {
                    String currentChannelColorString = packet.readUtf();
                    String currentPrivateChatPrefixString = packet.readUtf();
                    String currentResponsePartnerPrefixString = packet.readUtf();

                    RegistryOps<JsonElement> ops = Minecraft.getInstance().level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
                    JsonElement jsonElement = StrictJsonParser.parse(currentChannelColorString);
                    DataResult<Pair<Component, JsonElement>> result = ComponentSerialization.CODEC.decode(ops, jsonElement);
                    if (result.isSuccess()) {
                        currentChannelColor = result.getOrThrow().getFirst().copy();
                    }
                    jsonElement = StrictJsonParser.parse(currentPrivateChatPrefixString);
                    result = ComponentSerialization.CODEC.decode(ops, jsonElement);
                    if (result.isSuccess()) {
                        currentPrivateChatPrefix = result.getOrThrow().getFirst().copy();
                    }
                    jsonElement = StrictJsonParser.parse(currentResponsePartnerPrefixString);
                    result = ComponentSerialization.CODEC.decode(ops, jsonElement);
                    if (result.isSuccess()) {
                        currentResponsePartnerPrefix = result.getOrThrow().getFirst().copy();
                    }
                } catch (IndexOutOfBoundsException ignored) {
                }

                chatInfo = new ChatInfo(currentChannelName, currentPrivateChat, currentResponsePartner, currentChannelColor, currentPrivateChatPrefix, currentResponsePartnerPrefix);
            }

            if (cubesideDateChannel == challengeFlashScreenDataChannelID && cubesideDateChannelVersion == 0) {
                int color = packet.readInt();
                int duration = packet.readInt();
                screenFlashInfo = new ScreenFlashInfo(duration, new Color(color));
            }
        } catch (Exception e) {
            CubesideClientFabric.LOGGER.warn("Unable to read CubesideMod data", e);
        }
    }

    public void write(FriendlyByteBuf buf) {
        // nix write
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return PACKET_ID;
    }

    public ChatInfo getChatInfo() {
        return chatInfo;
    }

    public ScreenFlashInfo getScreenFlashInfo() {
        return screenFlashInfo;
    }

    public record ScreenFlashInfo(int duration, Color color) {}
}
