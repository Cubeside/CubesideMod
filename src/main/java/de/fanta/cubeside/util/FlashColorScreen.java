package de.fanta.cubeside.util;

import java.awt.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class FlashColorScreen {
    private static boolean isRunning = false;
    private static int duration;
    private static Color color;
    private static int counter = 0;

    public static void flashColoredScreen(int durationInTicks, Color newColor) {
        if (!isRunning) {
            isRunning = true;
            duration = durationInTicks;
            color = newColor;
        }
    }

    public static void onClientTick(GuiGraphics drawContext) {
        if (isRunning && counter < duration) {
            Minecraft mc = Minecraft.getInstance();
            int width = mc.getWindow().getGuiScaledWidth();
            int height = mc.getWindow().getGuiScaledHeight();
            Color newColor;
            if (counter < duration / 2) {
                newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (100 * (counter / (duration / 2f))));
            } else {
                newColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (100 * (2 - (counter / (duration / 2f)))));
            }
            drawContext.fill(0, 0, width, height, newColor.getRGB());
            counter++;
        } else {
            isRunning = false;
            counter = 0;
        }
    }
}
