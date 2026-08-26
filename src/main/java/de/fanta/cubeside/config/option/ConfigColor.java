package de.fanta.cubeside.config.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class ConfigColor extends ConfigValue<ArgbColor> {
    private final ArgbColor defaultValue;
    private ArgbColor value;

    public ConfigColor(String name, String defaultValue, String tooltipKey) {
        super(name, tooltipKey);
        this.defaultValue = ArgbColor.fromString(defaultValue).opaque();
        this.value = this.defaultValue;
    }

    public ArgbColor getColor() {
        return value;
    }

    public int getIntegerValue() {
        return value.intValue;
    }

    public String getStringValue() {
        return value.toHexString();
    }

    public void setIntegerValue(int value) {
        this.value = ArgbColor.fromColor(value).opaque();
    }

    public void setValueFromString(String value) {
        this.value = ArgbColor.fromString(value).opaque();
    }

    @Override
    public ArgbColor getValue() {
        return value;
    }

    @Override
    public ArgbColor getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void resetToDefault() {
        value = defaultValue;
    }

    @Override
    public void setValueFromJsonElement(JsonElement element) {
        if (!element.isJsonPrimitive()) {
            throw new IllegalArgumentException("Expected a color string");
        }
        setValueFromString(element.getAsString());
    }

    @Override
    public JsonElement getAsJsonElement() {
        return new JsonPrimitive(value.toHexString());
    }
}
