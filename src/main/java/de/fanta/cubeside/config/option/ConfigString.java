package de.fanta.cubeside.config.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class ConfigString extends ConfigValue<String> {
    private final String defaultValue;
    private String value;

    public ConfigString(String name, String defaultValue, String tooltipKey) {
        super(name, tooltipKey);
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    public String getStringValue() {
        return value;
    }

    public void setValueFromString(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void resetToDefault() {
        value = defaultValue;
    }

    @Override
    public void setValueFromJsonElement(JsonElement element) {
        if (!element.isJsonPrimitive()) {
            throw new IllegalArgumentException("Expected a string");
        }
        value = element.getAsString();
    }

    @Override
    public JsonElement getAsJsonElement() {
        return new JsonPrimitive(value);
    }
}
