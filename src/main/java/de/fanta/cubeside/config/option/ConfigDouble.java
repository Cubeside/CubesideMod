package de.fanta.cubeside.config.option;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class ConfigDouble extends ConfigValue<Double> {
    private final double defaultValue;
    private final double minValue;
    private final double maxValue;
    private final boolean useSlider;
    private double value;

    public ConfigDouble(String name, double defaultValue, String tooltipKey) {
        this(name, defaultValue, -Double.MAX_VALUE, Double.MAX_VALUE, false, tooltipKey);
    }

    public ConfigDouble(String name, double defaultValue, double minValue, double maxValue, boolean useSlider, String tooltipKey) {
        super(name, tooltipKey);
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.useSlider = useSlider;
        this.value = clamp(defaultValue);
    }

    public double getDoubleValue() {
        return value;
    }

    public void setDoubleValue(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Expected a finite double");
        }
        this.value = clamp(value);
    }

    public double getMinDoubleValue() {
        return minValue;
    }

    public double getMaxDoubleValue() {
        return maxValue;
    }

    public boolean shouldUseSlider() {
        return useSlider;
    }

    private double clamp(double candidate) {
        return Math.clamp(candidate, minValue, maxValue);
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public Double getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void resetToDefault() {
        value = clamp(defaultValue);
    }

    @Override
    public void setValueFromJsonElement(JsonElement element) {
        if (!element.isJsonPrimitive()) {
            throw new IllegalArgumentException("Expected a double");
        }
        setDoubleValue(element.getAsDouble());
    }

    @Override
    public JsonElement getAsJsonElement() {
        return new JsonPrimitive(value);
    }
}
