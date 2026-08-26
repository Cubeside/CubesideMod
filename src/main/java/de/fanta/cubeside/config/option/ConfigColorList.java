package de.fanta.cubeside.config.option;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;

public final class ConfigColorList extends ConfigValue<List<ArgbColor>> {
    private final List<ArgbColor> defaultValue;
    private final List<ArgbColor> value;

    public ConfigColorList(String name, List<ArgbColor> defaultValue, String tooltipKey) {
        super(name, tooltipKey);
        this.defaultValue = defaultValue.stream().map(ArgbColor::opaque).toList();
        this.value = new ArrayList<>(this.defaultValue);
    }

    public List<ArgbColor> getColors() {
        return value;
    }

    public List<ArgbColor> getDefaultColors() {
        return defaultValue;
    }

    public void setColors(List<ArgbColor> colors) {
        List<ArgbColor> normalized = colors.stream().map(ArgbColor::opaque).toList();
        value.clear();
        value.addAll(normalized);
    }

    @Override
    public List<ArgbColor> getValue() {
        return value;
    }

    @Override
    public List<ArgbColor> getDefaultValue() {
        return defaultValue;
    }

    @Override
    public void resetToDefault() {
        setColors(defaultValue);
    }

    @Override
    public void setValueFromJsonElement(JsonElement element) {
        if (!element.isJsonArray()) {
            throw new IllegalArgumentException("Expected a color array");
        }
        List<ArgbColor> parsed = new ArrayList<>();
        for (JsonElement entry : element.getAsJsonArray()) {
            if (!entry.isJsonPrimitive()) {
                throw new IllegalArgumentException("Expected a color string in array");
            }
            parsed.add(ArgbColor.fromString(entry.getAsString()).opaque());
        }
        setColors(parsed);
    }

    @Override
    public JsonElement getAsJsonElement() {
        JsonArray array = new JsonArray();
        value.forEach(color -> array.add(color.toHexString()));
        return array;
    }
}
