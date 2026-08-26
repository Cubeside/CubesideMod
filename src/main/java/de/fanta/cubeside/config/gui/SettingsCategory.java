package de.fanta.cubeside.config.gui;

import java.util.List;
import net.minecraft.network.chat.Component;

public record SettingsCategory(String id, String titleKey, List<SettingsGroup> groups) {
    public SettingsCategory {
        groups = List.copyOf(groups);
    }

    public Component title() {
        return Component.translatable(titleKey);
    }
}
