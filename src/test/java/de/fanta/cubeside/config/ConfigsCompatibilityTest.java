package de.fanta.cubeside.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.fanta.cubeside.config.option.ArgbColor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConfigsCompatibilityTest {
    @TempDir
    Path temporaryDirectory;

    @AfterEach
    void restoreDefaults() {
        Configs.resetToDefaults();
    }

    @Test
    void completeLegacyShapeRoundTripsWithoutStructuralChanges() throws Exception {
        Configs.Generic.ThirdPersonElytra.setBooleanValue(true);
        Configs.Generic.FastJoinButtonText.setValueFromString("Test Join");
        Configs.Generic.FastJoinButtonPort.setIntegerValue(24444);
        Configs.Chat.TimeStampColor.setIntegerValue(0x80112233);
        Configs.Chat.ChatMessageLimit.setIntegerValue(54321);
        Configs.HitBox.RainbowEntityHitBoxColorList.setColors(List.of(
                ArgbColor.fromColor(0x00FF0004), ArgbColor.fromColor(0xFF112233)));
        Configs.HitBox.RainbowEntityHitBoxSpeed.setDoubleValue(0.37);
        Configs.HitBox.ShowHitBox.setBooleanValue(true);
        Configs.MiningAssistent.MiningAssistentStartX.setIntegerValue(-12345);
        Configs.PermissionSettings.AdminList.setStrings(List.of("Same", "", "Same"));

        JsonObject expected = Configs.createRoot().deepCopy();
        Path input = temporaryDirectory.resolve("input.json");
        Files.writeString(input, new GsonBuilder().setPrettyPrinting().create().toJson(expected));

        Configs.resetToDefaults();
        Configs.loadFromFile(input, null);
        Path output = temporaryDirectory.resolve("output.json");
        Configs.saveToFile(output);

        JsonObject actual = JsonParser.parseString(Files.readString(output)).getAsJsonObject();
        assertEquals(expected, actual);
        assertEquals(1, actual.get("config_version").getAsInt());
        assertTrue(actual.getAsJsonObject("Hitbox").has("ShowHitBox"));
        assertTrue(actual.getAsJsonObject("MiningAssistent").has("MiningAssistentStartX"));
    }

    @Test
    void migratesLegacyRootConfigOnlyWhenTargetIsAbsent() throws Exception {
        Path configRoot = temporaryDirectory.resolve("config");
        Path legacy = configRoot.resolve("cubeside.json");
        Path target = configRoot.resolve("CubesideMod").resolve("cubeside.json");
        Files.createDirectories(configRoot);
        Files.writeString(legacy, Configs.createRoot().toString());

        Configs.loadFromFile(target, legacy);

        assertTrue(Files.isRegularFile(target));
        assertFalse(Files.exists(legacy));
    }

    @Test
    void invalidIndividualValuesFallBackWithoutDiscardingValidValues() throws Exception {
        JsonObject root = Configs.createRoot();
        root.getAsJsonObject("Generic").addProperty("ThirdPersonElytra", "not-a-boolean");
        root.getAsJsonObject("Generic").addProperty("AFKPling", true);
        root.getAsJsonObject("Chat").addProperty("TimeStampColor", "invalid");
        root.addProperty("unknown_root_value", 42);
        Path file = temporaryDirectory.resolve("broken.json");
        Files.writeString(file, root.toString());

        Configs.loadFromFile(file, null);

        assertFalse(Configs.Generic.ThirdPersonElytra.getBooleanValue());
        assertTrue(Configs.Generic.AFKPling.getBooleanValue());
        assertEquals(0xFFFFFFFF, Configs.Chat.TimeStampColor.getIntegerValue());
    }
}
