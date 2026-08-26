package de.fanta.cubeside.config.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class ConfigInteger extends ConfigValue<Integer> {
    private final int defaultValue;
    private final int minValue;
    private final int maxValue;
    private final boolean useSlider;
    private int value;

    public ConfigInteger(String name, int defaultValue, String tooltipKey) {
        this(name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, false, tooltipKey);
    }

    public ConfigInteger(String name, int defaultValue, int minValue, int maxValue, String tooltipKey) {
        this(name, defaultValue, minValue, maxValue, false, tooltipKey);
    }

    public ConfigInteger(String name, int defaultValue, int minValue, int maxValue, boolean useSlider, String tooltipKey) {
        super(name, tooltipKey);
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.useSlider = useSlider;
        this.value = clamp(defaultValue);
    }

    public int getIntegerValue() {
        return value;
    }

    public void setIntegerValue(int value) {
        this.value = clamp(value);
    }

    public int getMinIntegerValue() {
        return minValue;
    }

    public int getMaxIntegerValue() {
        return maxValue;
    }

    public boolean shouldUseSlider() {
        return useSlider;
    }

    private int clamp(int candidate) {
        return Math.clamp(candidate, minValue, maxValue);
    }

    @Override
    public Integer getValue() {
        return value;
    }

    @Override
    public Integer getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void resetToDefault() {
        value = clamp(defaultValue);
    }

    @Override
    public void setValueFromJsonElement(JsonElement element) {
        if (!element.isJsonPrimitive()) {
            throw new IllegalArgumentException("Expected an integer");
        }
        setIntegerValue(element.getAsInt());
    }

    @Override
    public JsonElement getAsJsonElement() {
        return new JsonPrimitive(value);
    }
}
