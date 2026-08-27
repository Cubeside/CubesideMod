package de.fanta.cubeside.config.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.fanta.cubeside.config.Configs;
import de.fanta.cubeside.config.option.ConfigValue;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class SettingsModelTest {
    @Test
    void everyVisibleOptionAppearsExactlyOnceAndHiddenOptionsStayHidden() {
        List<ConfigValue<?>> expected = Configs.categories().stream().flatMap(category -> category.visible().stream()).toList();
        List<ConfigValue<?>> actual = CubesideSettings.create(permission -> true).stream()
                .flatMap(category -> category.groups().stream())
                .flatMap(group -> group.options().stream())
                .toList();

        assertEquals(expected.size(), actual.size());
        assertEquals(expected.size(), new HashSet<>(actual).size());
        assertEquals(new HashSet<>(expected), new HashSet<>(actual));
        assertFalse(actual.contains(Configs.HitBox.ShowHitBox));
        assertFalse(actual.contains(Configs.MiningAssistent.MiningAssistentStartX));
    }

    @Test
    void englishAndGermanCoverLabelsTooltipsGroupsAndDialogText() {
        for (String language : List.of("en_us", "de_de")) {
            JsonObject translations = load(language);
            for (SettingsCategory category : CubesideSettings.create(permission -> true)) {
                assertTrue(translations.has(category.titleKey()), language + ": " + category.titleKey());
                for (SettingsGroup group : category.groups()) {
                    assertTrue(translations.has(group.titleKey()), language + ": " + group.titleKey());
                    for (ConfigValue<?> option : group.options()) {
                        String label = "cubeside.settings.option." + option.getName();
                        assertTrue(translations.has(label), language + ": " + label);
                        assertTrue(translations.has(option.getTooltipKey()), language + ": " + option.getTooltipKey());
                    }
                }
            }
            for (String common : List.of("cubeside.settings.entries", "cubeside.settings.noEntries", "cubeside.settings.apply",
                    "cubeside.settings.color.hex", "cubeside.settings.list.add", "cubeside.settings.list.empty")) {
                assertTrue(translations.has(common), language + ": " + common);
            }
        }
    }

    @Test
    void permissionSettingsAreHiddenWithoutPermissions() {
        List<SettingsCategory> categories = CubesideSettings.create(permission -> false);

        assertTrue(categories.stream().noneMatch(category -> category.id().equals("permissionsettings")));
        assertEquals(nonPermissionCategories(CubesideSettings.create(permission -> true)), categories);
        assertEquals(List.of(Configs.PermissionSettings.AutoChat, Configs.PermissionSettings.AutoChatAntwort,
                Configs.PermissionSettings.AdminList), Configs.PermissionSettings.OPTIONS);
    }

    @Test
    void autoChatPermissionShowsOnlyAutoChatSettings() {
        SettingsCategory category = permissionCategory(CubesideSettings.create("cubeside.autochat"::equals));

        assertEquals(1, category.groups().size());
        assertEquals("cubeside.settings.group.autoChat", category.groups().getFirst().titleKey());
        assertEquals(List.of(Configs.PermissionSettings.AutoChat, Configs.PermissionSettings.AutoChatAntwort),
                category.groups().getFirst().options());
    }

    @Test
    void afkCheckPermissionShowsOnlyAdminList() {
        SettingsCategory category = permissionCategory(CubesideSettings.create("cubeside.afkcheck"::equals));

        assertEquals(1, category.groups().size());
        assertEquals("cubeside.settings.group.admins", category.groups().getFirst().titleKey());
        assertEquals(List.of(Configs.PermissionSettings.AdminList), category.groups().getFirst().options());
    }

    @Test
    void bothPermissionsShowAllPermissionSettings() {
        SettingsCategory category = permissionCategory(CubesideSettings.create(permission -> true));

        assertEquals(2, category.groups().size());
        assertEquals(List.of(Configs.PermissionSettings.AutoChat, Configs.PermissionSettings.AutoChatAntwort,
                        Configs.PermissionSettings.AdminList),
                category.groups().stream().flatMap(group -> group.options().stream()).toList());
    }

    @Test
    void miningAssistantUsesTheConfiguredSliderRanges() {
        assertTrue(Configs.MiningAssistent.MiningAssistentDistance.shouldUseSlider());
        assertEquals(1, Configs.MiningAssistent.MiningAssistentDistance.getMinIntegerValue());
        assertEquals(100, Configs.MiningAssistent.MiningAssistentDistance.getMaxIntegerValue());

        assertTrue(Configs.MiningAssistent.MiningAssistentCircles.shouldUseSlider());
        assertEquals(1, Configs.MiningAssistent.MiningAssistentCircles.getMinIntegerValue());
        assertEquals(50, Configs.MiningAssistent.MiningAssistentCircles.getMaxIntegerValue());
    }

    private static JsonObject load(String language) {
        String path = "/assets/cubeside/lang/" + language + ".json";
        return JsonParser.parseReader(new InputStreamReader(
                SettingsModelTest.class.getResourceAsStream(path), StandardCharsets.UTF_8)).getAsJsonObject();
    }

    private static SettingsCategory permissionCategory(List<SettingsCategory> categories) {
        return categories.stream()
                .filter(category -> category.id().equals("permissionsettings"))
                .findFirst()
                .orElseThrow();
    }

    private static List<SettingsCategory> nonPermissionCategories(List<SettingsCategory> categories) {
        return categories.stream()
                .filter(category -> !category.id().equals("permissionsettings"))
                .toList();
    }
}
