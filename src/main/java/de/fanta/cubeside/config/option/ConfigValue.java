package de.fanta.cubeside.config.option;

import com.google.gson.JsonElement;
import net.minecraft.network.chat.Component;

public abstract class ConfigValue<T> {
    private final String name;
    private final String tooltipKey;

    protected ConfigValue(String name, String tooltipKey) {
        this.name = name;
        this.tooltipKey = tooltipKey;
    }

    public final String getName() {
        return name;
    }

    public final Component getDisplayName() {
        return Component.translatable("cubeside.settings.option." + name);
    }

    public final Component getTooltip() {
        return tooltipKey == null || tooltipKey.isBlank() ? Component.empty() : Component.translatable(tooltipKey);
    }

    public final String getTooltipKey() {
        return tooltipKey;
    }

    public abstract T getValue();

    public abstract T getDefaultValue();

    public abstract void resetToDefault();

    public abstract void setValueFromJsonElement(JsonElement element);

    public abstract JsonElement getAsJsonElement();
}
