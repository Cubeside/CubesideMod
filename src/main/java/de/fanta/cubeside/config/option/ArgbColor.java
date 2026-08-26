package de.fanta.cubeside.config.option;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ArgbColor {
    private static final Pattern HEX = Pattern.compile("(?:0x|#)([a-fA-F0-9]+)");

    public final int intValue;

    private ArgbColor(int value) {
        this.intValue = value;
    }

    public static ArgbColor fromColor(int value) {
        return new ArgbColor(value);
    }

    public static ArgbColor fromString(String value) {
        return new ArgbColor(parse(value));
    }

    public static int parse(String value) {
        Matcher matcher = HEX.matcher(value);
        if (matcher.matches()) {
            String hex = matcher.group(1);
            return switch (hex.length()) {
                case 8 -> (int) Long.parseLong(hex, 16);
                case 6 -> 0xFF000000 | Integer.parseInt(hex, 16);
                case 4 -> expandFourDigit(hex);
                case 3 -> 0xFF000000 | expandThreeDigit(hex);
                default -> throw new IllegalArgumentException("Unsupported color format: " + value);
            };
        }
        try {
            return Integer.parseInt(value, 10);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Unsupported color format: " + value, exception);
        }
    }

    private static int expandFourDigit(String value) {
        int original = Integer.parseInt(value, 16);
        int alpha = ((original >>> 12) & 0xF) * 17;
        int red = ((original >>> 8) & 0xF) * 17;
        int green = ((original >>> 4) & 0xF) * 17;
        int blue = (original & 0xF) * 17;
        return alpha << 24 | red << 16 | green << 8 | blue;
    }

    private static int expandThreeDigit(String value) {
        int original = Integer.parseInt(value, 16);
        int red = ((original >>> 8) & 0xF) * 17;
        int green = ((original >>> 4) & 0xF) * 17;
        int blue = (original & 0xF) * 17;
        return red << 16 | green << 8 | blue;
    }

    public int getIntValue() {
        return intValue;
    }

    public int toVanillaArgb() {
        return intValue;
    }

    public int toVanillaRgb() {
        return intValue & 0x00FFFFFF;
    }

    public int alpha() {
        return intValue >>> 24 & 0xFF;
    }

    public int red() {
        return intValue >>> 16 & 0xFF;
    }

    public int green() {
        return intValue >>> 8 & 0xFF;
    }

    public int blue() {
        return intValue & 0xFF;
    }

    public ArgbColor opaque() {
        return new ArgbColor(0xFF000000 | intValue & 0x00FFFFFF);
    }

    public String toRgbHexString() {
        return String.format(Locale.ROOT, "#%06X", intValue & 0x00FFFFFF);
    }

    public String toHexString() {
        return String.format(Locale.ROOT, "#%08X", intValue);
    }

    @Override
    public String toString() {
        return toHexString();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ArgbColor color && color.intValue == intValue;
    }

    @Override
    public int hashCode() {
        return Objects.hash(intValue);
    }
}
