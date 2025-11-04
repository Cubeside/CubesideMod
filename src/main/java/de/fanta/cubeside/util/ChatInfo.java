package de.fanta.cubeside.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ChatInfo {
    private final String currentChannelName;
    private final String currentPrivateChat;
    private final String currentResponsePartner;
    private final MutableComponent currentChannelColor;
    private final MutableComponent currentPrivateChatPrefix;
    private final MutableComponent currentResponsePartnerPrefix;

    public ChatInfo(String currentChannelName, String currentPrivateChat, String currentResponsePartner, MutableComponent currentChannelColor, MutableComponent currentPrivateChatPrefix, MutableComponent currentResponsePartnerPrefix) {
        this.currentChannelName = currentChannelName;
        this.currentPrivateChat = currentPrivateChat;
        this.currentResponsePartner = currentResponsePartner;
        this.currentChannelColor = currentChannelColor;
        this.currentPrivateChatPrefix = currentPrivateChatPrefix;
        this.currentResponsePartnerPrefix = currentResponsePartnerPrefix;
    }

    public String getCurrentChannelName() {
        return currentChannelName;
    }

    public String getCurrentPrivateChat() {
        return currentPrivateChat;
    }

    public String getCurrentResponsePartner() {
        return currentResponsePartner;
    }

    public Component getCurrentChannelColor() {
        return currentChannelColor;
    }

    public Component getCurrentPrivateChatPrefix() {
        return currentPrivateChatPrefix;
    }

    public Component getCurrentResponsePartnerPrefix() {
        return currentResponsePartnerPrefix;
    }

    public boolean isPrivatChat() {
        return !currentPrivateChat.equals("");
    }

    public MutableComponent getColoredChannelText() {
        return currentChannelColor.getSiblings().isEmpty() ? currentChannelColor.copy().append(currentChannelName) : currentChannelColor.getSiblings().get(currentChannelColor.getSiblings().size() - 1).copy().append(currentChannelName);
    }

    public MutableComponent getColoredPrivatText() {
        return currentPrivateChatPrefix.getSiblings().isEmpty() ? currentPrivateChatPrefix.copy().append(currentPrivateChat) : currentPrivateChatPrefix.getSiblings().get(currentPrivateChatPrefix.getSiblings().size() - 1).copy().append(currentPrivateChat);
    }

    public boolean hasResponsePlayer() {
        return !currentResponsePartner.equals("");
    }

    public MutableComponent getColoredResponseText() {
        return currentResponsePartnerPrefix.getSiblings().isEmpty() ? currentResponsePartnerPrefix.copy().append(currentResponsePartner) : currentResponsePartnerPrefix.getSiblings().get(currentResponsePartnerPrefix.getSiblings().size() - 1).copy().append(currentResponsePartner);
    }
}
