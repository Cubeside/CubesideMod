package de.fanta.cubeside.config.option;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;

public final class ConfigStringList extends ConfigValue<List<String>> {
    private final List<String> defaultValue;
    private final List<String> value;

    public ConfigStringList(String name, List<String> defaultValue, String tooltipKey) {
        super(name, tooltipKey);
        this.defaultValue = List.copyOf(defaultValue);
        this.value = new ArrayList<>(defaultValue);
    }

    public List<String> getStrings() {
        return value;
    }

    public List<String> getDefaultStrings() {
        return defaultValue;
    }

    public void setStrings(List<String> strings) {
        value.clear();
        value.addAll(strings);
    }

    @Override
    public List<String> getValue() {
        return value;
    }

    @Override
    public List<String> getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void resetToDefault() {
        setStrings(defaultValue);
    }

    @Override
    public void setValueFromJsonElement(JsonElement element) {
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException("Expected a string array");
        }
        List<String> parsed = new ArrayList<>();
        for (JsonElement entry : element.getAsJsonArray()) {
            if (!entry.isJsonPrimitive()) {
                throw new IllegalArgumentException("Expected a string in array");
            }
            parsed.add(entry.getAsString());
        }
        setStrings(parsed);
    }

    @Override
    public JsonElement getAsJsonElement() {
        JsonArray array = new JsonArray();
        value.forEach(array::add);
        return array;
    }
}
