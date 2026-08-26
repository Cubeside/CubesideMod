package de.fanta.cubeside.config.gui;

import de.fanta.cubeside.config.Configs;
import java.util.List;

public final class CubesideSettings {
    private CubesideSettings() {
    }

    public static List<SettingsCategory> create() {
        return List.of(
                new SettingsCategory("generic", "cubeside.gui.button.config_gui.generic", List.of(
                        group("cubeside.settings.group.gameplay",
                                Configs.Generic.ThirdPersonElytra, Configs.Generic.ElytraAlarm,
                                Configs.Generic.ShowInvisibleArmorstands, Configs.Generic.ShowInvisibleEntitiesInSpectator,
                                Configs.Generic.GamemodeSwitcher, Configs.Generic.ActionBarShadow,
                                Configs.Generic.ShowAdditionalRepairCosts, Configs.Generic.AFKPling),
                        group("cubeside.settings.group.interaction",
                                Configs.Generic.WoodStriping, Configs.Generic.CreateGrassPath, Configs.Generic.SignEdit),
                        group("cubeside.settings.group.misc",
                                Configs.Fun.DisableChristmasChest, Configs.Fixes.SimpleSignGlow),
                        group("cubeside.settings.group.teleport",
                                Configs.Generic.ClickableTpaMessage, Configs.Generic.TpaSound),
                        group("cubeside.settings.group.fastJoin",
                                Configs.Generic.FastJoinButtonText, Configs.Generic.FastJoinButtonIP,
                                Configs.Generic.FastJoinButtonPort))),
                new SettingsCategory("chat", "cubeside.gui.button.config_gui.chat", List.of(
                        group("cubeside.settings.group.chatDisplay",
                                Configs.Chat.ChatTimeStamps, Configs.Chat.TimeStampColor,
                                Configs.Chat.DisplayChatInfo, Configs.Chat.CountDuplicateMessages,
                                Configs.Chat.CountDuplicateMessagesFormat, Configs.Chat.CountDuplicateMessagesColor),
                        group("cubeside.settings.group.chatStorage",
                                Configs.Chat.SaveMessagesToDatabase, Configs.Chat.DaysTheMessagesAreStored,
                                Configs.Chat.ChatMessageLimit))),
                new SettingsCategory("chunkloading", "cubeside.gui.button.config_gui.chunkloading", List.of(
                        group("cubeside.settings.group.chunkLoading", Configs.ChunkLoading.OPTIONS))),
                new SettingsCategory("hitbox", "cubeside.gui.button.config_gui.hitbox", List.of(
                        group("cubeside.settings.group.entityHitbox",
                                Configs.HitBox.ModifiedEntityHitBox, Configs.HitBox.RainbowEntityHitBox,
                                Configs.HitBox.RainbowEntityHitBoxColorList, Configs.HitBox.RainbowEntityHitBoxSpeed,
                                Configs.HitBox.EntityHitBoxColor, Configs.HitBox.EntityHitBoxVisibility,
                                Configs.HitBox.EntityHitBoxDirection),
                        group("cubeside.settings.group.blockHitbox",
                                Configs.HitBox.ModifiedBlockHitBox, Configs.HitBox.RainbowBlockHitBox,
                                Configs.HitBox.RainbowBlockHitBoxColorList, Configs.HitBox.RainbowBlockHitBoxSpeed,
                                Configs.HitBox.BlockHitBoxColor, Configs.HitBox.BlockHitBoxVisibility))),
                new SettingsCategory("miningassistent", "cubeside.gui.button.config_gui.miningassistent", List.of(
                        group("cubeside.settings.group.mining", Configs.MiningAssistent.OPTIONS))),
                new SettingsCategory("permissionsettings", "cubeside.gui.button.config_gui.permissionsettings", List.of(
                        group("cubeside.settings.group.autoChat", Configs.PermissionSettings.AutoChat, Configs.PermissionSettings.AutoChatAntwort),
                        group("cubeside.settings.group.admins", Configs.PermissionSettings.AdminList))));
    }

    private static SettingsGroup group(String titleKey, de.fanta.cubeside.config.option.ConfigValue<?>... values) {
        return new SettingsGroup(titleKey, List.of(values));
    }

    private static SettingsGroup group(String titleKey, List<de.fanta.cubeside.config.option.ConfigValue<?>> values) {
        return new SettingsGroup(titleKey, values);
    }
}
