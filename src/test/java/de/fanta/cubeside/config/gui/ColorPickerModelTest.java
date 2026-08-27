package de.fanta.cubeside.config.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fanta.cubeside.config.option.ArgbColor;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

class ColorPickerModelTest {
    @Test
    void colorRoundTripsThroughHsv() {
        ColorPickerModel model = new ColorPickerModel(ArgbColor.fromColor(0xFF2A7FC1));

        assertEquals(0xFF2A7FC1, model.color().intValue);
        assertEquals("#2A7FC1", model.hex());

        model.setColor(ArgbColor.fromColor(0xFF000000));
        assertEquals(0xFF000000, model.color().intValue);
        model.setColor(ArgbColor.fromColor(0xFFFFFFFF));
        assertEquals(0xFFFFFFFF, model.color().intValue);
        model.setColor(ArgbColor.fromColor(0xFF808080));
        assertEquals(0xFF808080, model.color().intValue);
    }

    @Test
    void wheelUsesAngleForHueAndRadiusForSaturation() {
        ColorPickerModel model = new ColorPickerModel(ArgbColor.fromColor(0xFFFFFFFF));

        assertTrue(model.setWheelPoint(1.0, 0.0));
        assertEquals(0xFFFF0000, model.color().intValue);

        assertTrue(model.setWheelPoint(-0.5, Math.sqrt(3.0) / 2.0));
        assertColorClose(0xFF00FF00, model.color().intValue);

        assertTrue(model.setWheelPoint(0.0, 0.0));
        assertEquals(0xFFFFFFFF, model.color().intValue);
    }

    @Test
    void valueIsClampedAndKeepsHueAndSaturation() {
        ColorPickerModel model = new ColorPickerModel(ArgbColor.fromColor(0xFFFF0000));

        model.setValue(-1.0);
        assertEquals(0xFF000000, model.color().intValue);
        model.setValue(2.0);
        assertEquals(0xFFFF0000, model.color().intValue);
    }

    @Test
    void pointsOutsideTheWheelDoNotChangeTheColor() {
        ColorPickerModel model = new ColorPickerModel(ArgbColor.fromColor(0xFF123456));

        assertFalse(model.setWheelPoint(1.0, 1.0));
        assertEquals(0xFF123456, model.color().intValue);
    }

    @Test
    void parsesAndNormalizesSixDigitRgbHex() {
        assertEquals(OptionalInt.of(0xFFAABBCC), ColorPickerModel.parseHex("#aabbcc"));
        assertEquals(OptionalInt.of(0xFF112233), ColorPickerModel.parseHex("112233"));
        assertEquals("#AABBCC", ColorPickerModel.normalizeHex(0x12AABBCC));

        assertTrue(ColorPickerModel.parseHex("#abc").isEmpty());
        assertTrue(ColorPickerModel.parseHex("#GG1122").isEmpty());
        assertTrue(ColorPickerModel.parseHex("").isEmpty());
        assertTrue(ColorPickerModel.parseHex(null).isEmpty());
    }

    private static void assertColorClose(int expected, int actual) {
        assertEquals(expected >>> 24 & 0xFF, actual >>> 24 & 0xFF);
        assertEquals(expected >>> 16 & 0xFF, actual >>> 16 & 0xFF, 1);
        assertEquals(expected >>> 8 & 0xFF, actual >>> 8 & 0xFF, 1);
        assertEquals(expected & 0xFF, actual & 0xFF, 1);
    }
}
