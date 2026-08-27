package de.fanta.cubeside.config.gui;

import com.mojang.blaze3d.platform.NativeImage;
import de.fanta.cubeside.CubesideClientFabric;
import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

final class ColorWheelTexture {
    static final int SIZE = 256;
    static final Identifier ID = Identifier.fromNamespaceAndPath(CubesideClientFabric.MODID, "dynamic/color_wheel");
    private static boolean registered;

    private ColorWheelTexture() {
    }

    static void ensureRegistered(Minecraft minecraft) {
        if (registered) {
            return;
        }

        NativeImage image = new NativeImage(SIZE, SIZE, true);
        double center = (SIZE - 1) / 2.0;
        double radius = center;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                double normalizedX = (x - center) / radius;
                double normalizedY = (y - center) / radius;
                double saturation = Math.hypot(normalizedX, normalizedY);
                if (saturation > 1.0) {
                    image.setPixel(x, y, 0x00000000);
                    continue;
                }

                double hue = Math.atan2(normalizedY, normalizedX) / (Math.PI * 2.0);
                if (hue < 0.0) {
                    hue += 1.0;
                }
                image.setPixel(x, y, Color.HSBtoRGB((float) hue, (float) saturation, 1.0F));
            }
        }

        minecraft.getTextureManager().register(ID, new DynamicTexture(() -> "Cubeside color wheel", image));
        registered = true;
    }
}
