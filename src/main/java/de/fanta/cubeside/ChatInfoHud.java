package de.fanta.cubeside;

import de.fanta.cubeside.config.Configs;
import de.fanta.cubeside.util.ChatInfo;
import java.awt.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ChatInfoHud {
    private final Minecraft minecraft;
    private final Font fontRenderer;

    public ChatInfoHud() {
        this.minecraft = Minecraft.getInstance();
        this.fontRenderer = minecraft.font;
    }

    private static class RenderSize {
        int width;
        int height;

        RenderSize(int w, int h) {
            this.width = w;
            this.height = h;
        }
    }

    public void onRenderChatInfoHud(GuiGraphics context) {
        if (Configs.Chat.DisplayChatInfo.getBooleanValue() && CubesideClientFabric.getChatInfo() != null) {
            renderChatInfoHud(context, CubesideClientFabric.getChatInfo());
        }
    }

    private void renderChatInfoHud(GuiGraphics context, ChatInfo chatInfo) {
        MutableComponent currentChannelText = Component.literal("Aktueller Chat: ");
        MutableComponent currentChannelColorText = chatInfo.isPrivatChat() ? chatInfo.getColoredPrivatText() : chatInfo.getColoredChannelText();
        currentChannelText.append(currentChannelColorText);

        MutableComponent currentResponseText = Component.literal(chatInfo.hasResponsePlayer() ? "Antwort an: " : "");
        MutableComponent currentResponseColorText = chatInfo.hasResponsePlayer() ? chatInfo.getColoredResponseText() : Component.empty();
        currentResponseText.append(currentResponseColorText);

        RenderSize result = new RenderSize(0, 0);
        int distance = 10;
        int height = (int) (minecraft.getWindow().getGuiScaledHeight() - (result.height + 70 / 2f));

        context.fill(2, height - 2, getWith(getWith(0, currentResponseText.getString()), currentChannelText.getString()) + 8, !chatInfo.hasResponsePlayer() ? height + 10 : height + 20, minecraft.options.getBackgroundColor(Integer.MIN_VALUE));

        result.width = getWith(result.width, currentChannelText.getString());
        context.drawString(this.fontRenderer, currentChannelText, 5, (minecraft.getWindow().getGuiScaledHeight() - (result.height + 70 / 2)), Color.WHITE.getRGB(), true);
        result.height -= distance;
        context.drawString(this.fontRenderer, currentResponseText, 5, (minecraft.getWindow().getGuiScaledHeight() - (result.height + 70 / 2)), Color.WHITE.getRGB(), true);

        if (result.width != 0) {
            result.width += 20;
        }
    }

    private int getWith(int resultWidth, String text) {
        int width = this.fontRenderer.width(text);
        return Math.max(width, resultWidth);
    }
}