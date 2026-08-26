package de.fanta.cubeside.config.gui;

import de.fanta.cubeside.config.option.ConfigValue;
import java.util.List;
import net.minecraft.network.chat.Component;

public record SettingsGroup(String titleKey, List<ConfigValue<?>> options) {
    public SettingsGroup {
        options = List.copyOf(options);
    }

    public Component title() {
        return Component.translatable(titleKey);
    }
}
