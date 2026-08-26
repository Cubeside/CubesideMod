package de.fanta.cubeside.util;

import de.fanta.cubeside.config.option.ArgbColor;

import java.awt.*;
import java.util.List;

public class ColorUtils {
    private ColorUtils() {
    }

    public static Color getColorGradient(long time, double speed, List<ArgbColor> baseColors) {
        int colorCount = baseColors.size();
        int v = Math.floorMod((int) (time * speed * 21), colorCount * 100);
        int step = v / 100;
        double ratio = (v % 100) * 0.01;
        int c0 = baseColors.get(step).intValue;
        int c1 = baseColors.get((step + 1) % baseColors.size()).intValue;

        int r1 = (c0 >> 16) & 0xff;
        int g1 = (c0 >> 8) & 0xff;
        int b1 = c0 & 0xff;

        int r2 = (c1 >> 16) & 0xff;
        int g2 = (c1 >> 8) & 0xff;
        int b2 = c1 & 0xff;

        double cmax1 = Math.max(r1, g1);
        if (b1 > cmax1) {
            cmax1 = b1;
        }
        double cmin1 = Math.min(r1, g1);
        if (b1 < cmin1) {
            cmin1 = b1;
        }

        double brightness1 = (cmax1) / 255.0;
        double saturation1;
        if (cmax1 != 0) {
            saturation1 = ((cmax1 - cmin1)) / (cmax1);
        } else {
            saturation1 = 0;
        }
        double hue1;
        if (saturation1 == 0) {
            hue1 = 0;
        } else {
            double redc = ((cmax1 - r1)) / ((cmax1 - cmin1));
            double greenc = ((cmax1 - g1)) / ((cmax1 - cmin1));
            double bluec = ((cmax1 - b1)) / ((cmax1 - cmin1));
            if (r1 == cmax1) {
                hue1 = bluec - greenc;
            } else if (g1 == cmax1) {
                hue1 = 2.0 + redc - bluec;
            } else {
                hue1 = 4.0 + greenc - redc;
            }
            hue1 = hue1 / 6.0;
            if (hue1 < 0) {
                hue1 = hue1 + 1.0;
            }
        }

        double cmax2 = Math.max(r2, g2);
        if (b2 > cmax2) {
            cmax2 = b2;
        }
        double cmin2 = Math.min(r2, g2);
        if (b2 < cmin2) {
            cmin2 = b2;
        }

        double brightness2 = (cmax2) / 255.0;
        double saturation2;
        if (cmax2 != 0) {
            saturation2 = ((cmax2 - cmin2)) / (cmax2);
        } else {
            saturation2 = 0;
        }
        double hue2;
        if (saturation2 == 0) {
            hue2 = 0;
        } else {
            double redc = ((cmax2 - r2)) / ((cmax2 - cmin2));
            double greenc = ((cmax2 - g2)) / ((cmax2 - cmin2));
            double bluec = ((cmax2 - b2)) / ((cmax2 - cmin2));
            if (r2 == cmax2) {
                hue2 = bluec - greenc;
            } else if (g2 == cmax2) {
                hue2 = 2.0 + redc - bluec;
            } else {
                hue2 = 4.0 + greenc - redc;
            }
            hue2 = hue2 / 6.0;
            if (hue2 < 0) {
                hue2 = hue2 + 1.0;
            }
        }

        double ratio2 = 1.0 - ratio;
        double brightness = brightness1 * ratio2 + brightness2 * ratio;
        double saturation = saturation1 * ratio2 + saturation2 * ratio;
        if (hue2 - hue1 > 0.5) {
            hue1 += 1;
        } else if (hue1 - hue2 > 0.5) {
            hue2 += 1;
        }
        double hue = hue1 * ratio2 + hue2 * ratio;
        if (hue > 1.0) {
            hue -= 1.0;
        }

        double r = 0.0;
        double g = 0.0;
        double b = 0.0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            double h = (hue - Math.floor(hue)) * 6.0f;
            double f = h - Math.floor(h);
            double p = brightness * (1.0f - saturation);
            double q = brightness * (1.0f - saturation * f);
            double t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0 -> {
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                }
                case 1 -> {
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                }
                case 2 -> {
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                }
                case 3 -> {
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                }
                case 4 -> {
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                }
                case 5 -> {
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                }
            }
        }
        int rFinal = (int) r;
        int gFinal = (int) g;
        int bFinal = (int) b;

        int c = (rFinal << 16) | (gFinal << 8) | (bFinal);

        return new Color(c);
    }
}
