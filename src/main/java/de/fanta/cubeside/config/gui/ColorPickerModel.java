package de.fanta.cubeside.config.gui;

import de.fanta.cubeside.config.option.ArgbColor;
import java.awt.Color;
import java.util.Locale;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ColorPickerModel {
    private static final Pattern RGB_HEX = Pattern.compile("#?([0-9a-fA-F]{6})");
    private static final double FULL_CIRCLE = Math.PI * 2.0;

    private float hue;
    private float saturation;
    private float value;

    ColorPickerModel(ArgbColor color) {
        setColor(color);
    }

    void setColor(ArgbColor color) {
        float[] hsv = Color.RGBtoHSB(color.red(), color.green(), color.blue(), null);
        hue = hsv[0];
        saturation = hsv[1];
        value = hsv[2];
    }

    ArgbColor color() {
        return ArgbColor.fromColor(0xFF000000 | Color.HSBtoRGB(hue, saturation, value) & 0x00FFFFFF);
    }

    ArgbColor fullValueColor() {
        return ArgbColor.fromColor(0xFF000000 | Color.HSBtoRGB(hue, saturation, 1.0F) & 0x00FFFFFF);
    }

    float hue() {
        return hue;
    }

    float saturation() {
        return saturation;
    }

    float value() {
        return value;
    }

    boolean setWheelPoint(double normalizedX, double normalizedY) {
        double distance = Math.hypot(normalizedX, normalizedY);
        if (distance > 1.0) {
            return false;
        }
        double angle = Math.atan2(normalizedY, normalizedX) / FULL_CIRCLE;
        if (angle < 0.0) {
            angle += 1.0;
        }
        hue = (float) angle;
        saturation = (float) Math.clamp(distance, 0.0, 1.0);
        return true;
    }

    void setValue(double newValue) {
        value = (float) Math.clamp(newValue, 0.0, 1.0);
    }

    String hex() {
        return color().toRgbHexString();
    }

    static OptionalInt parseHex(String text) {
        if (text == null) {
            return OptionalInt.empty();
        }
        Matcher matcher = RGB_HEX.matcher(text.trim());
        if (!matcher.matches()) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(0xFF000000 | Integer.parseInt(matcher.group(1), 16));
    }

    static String normalizeHex(int color) {
        return String.format(Locale.ROOT, "#%06X", color & 0x00FFFFFF);
    }
}
