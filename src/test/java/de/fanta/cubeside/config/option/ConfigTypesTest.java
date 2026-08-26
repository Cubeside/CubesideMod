package de.fanta.cubeside.config.option;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConfigTypesTest {
    @Test
    void parsesEveryLegacyColorRepresentation() {
        assertEquals(0xFFAABBCC, ArgbColor.parse("#abc"));
        assertEquals(0xAABBCCDD, ArgbColor.parse("#abcd"));
        assertEquals(0xFF112233, ArgbColor.parse("#112233"));
        assertEquals(0x80112233, ArgbColor.parse("0x80112233"));
        assertEquals(-1, ArgbColor.parse("-1"));
        assertEquals("#80112233", ArgbColor.fromColor(0x80112233).toHexString());
        assertEquals(0x00112233, ArgbColor.fromColor(0x80112233).toVanillaRgb());
        assertEquals(112233, ArgbColor.parse("112233"));
        assertThrows(IllegalArgumentException.class, () -> ArgbColor.parse("not-a-color"));
    }

    @Test
    void primitiveOptionsClampAndRoundTripJsonTypes() {
        ConfigBoolean bool = new ConfigBoolean("Bool", false, "");
        bool.setValueFromJsonElement(new JsonPrimitive(true));
        assertEquals(true, bool.getBooleanValue());

        ConfigInteger integer = new ConfigInteger("Integer", 5, 1, 10, true, "");
        integer.setIntegerValue(100);
        assertEquals(10, integer.getIntegerValue());
        integer.setValueFromJsonElement(new JsonPrimitive(-10));
        assertEquals(1, integer.getIntegerValue());

        ConfigDouble decimal = new ConfigDouble("Double", 0.5, 0.0, 1.0, true, "");
        decimal.setValueFromJsonElement(new JsonPrimitive(2.0));
        assertEquals(1.0, decimal.getDoubleValue());
        assertThrows(IllegalArgumentException.class, () -> decimal.setDoubleValue(Double.NaN));

        ConfigString string = new ConfigString("String", "default", "");
        string.setValueFromJsonElement(new JsonPrimitive("changed"));
        assertEquals("changed", string.getStringValue());
        string.resetToDefault();
        assertEquals("default", string.getStringValue());
    }

    @Test
    void listOptionsPreserveOrderDuplicatesAndEmptyValues() {
        ConfigStringList strings = new ConfigStringList("Strings", List.of("default"), "");
        JsonArray json = new JsonArray();
        json.add("same");
        json.add("");
        json.add("same");
        strings.setValueFromJsonElement(json);
        assertEquals(List.of("same", "", "same"), strings.getStrings());

        ConfigColorList colors = new ConfigColorList("Colors", List.of(ArgbColor.fromColor(0xFFFFFFFF)), "");
        colors.setValueFromJsonElement(com.google.gson.JsonParser.parseString("[\"#00112233\",\"#FF445566\"]"));
        assertEquals(List.of(ArgbColor.fromColor(0xFF112233), ArgbColor.fromColor(0xFF445566)), colors.getColors());

        List<ArgbColor> draft = new ArrayList<>(colors.getColors());
        draft.clear();
        assertEquals(2, colors.getColors().size(), "Editing a draft must not mutate the option before apply");
        colors.setColors(draft);
        assertEquals(List.of(), colors.getColors());
    }

    @Test
    void configColorsAreAlwaysOpaqueRgbValues() {
        ConfigColor color = new ConfigColor("Color", "#80112233", "");
        assertEquals(0xFF112233, color.getIntegerValue());
        assertEquals("#FF112233", color.getStringValue());

        color.setValueFromString("#00445566");
        assertEquals(0xFF445566, color.getIntegerValue());
        assertEquals("#445566", color.getColor().toRgbHexString());
    }
}
