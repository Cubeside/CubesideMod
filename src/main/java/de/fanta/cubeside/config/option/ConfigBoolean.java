package de.fanta.cubeside.config.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class ConfigBoolean extends ConfigValue<Boolean> {
    private final boolean defaultValue;
    private boolean value;

    public ConfigBoolean(String name, boolean defaultValue, String tooltipKey) {
        super(name, tooltipKey);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public boolean getBooleanValue() {
        return value;
    }

    public void setBooleanValue(boolean value) {
        this.value = value;
    }

    @Override
    public Boolean getValue() {
        return value;
    }

    @Override
    public Boolean getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void resetToDefault() {
        value = defaultValue;
    }

    @Override
    public void setValueFromJsonElement(JsonElement element) {
        if (!element.isJsonPrimitive()) {
            throw new IllegalArgumentException("Expected a boolean");
        }
        value = element.getAsBoolean();
    }

    @Override
    public JsonElement getAsJsonElement() {
        return new JsonPrimitive(value);
    }
}
