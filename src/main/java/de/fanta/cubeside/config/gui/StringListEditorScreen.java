package de.fanta.cubeside.config.gui;

import de.fanta.cubeside.config.option.ConfigStringList;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;

public final class StringListEditorScreen extends AbstractListEditorScreen<String> {
    private final ConfigStringList option;

    public StringListEditorScreen(Screen parent, ConfigStringList option) {
        super(parent, option.getDisplayName(), option.getStrings(), option.getDefaultStrings());
        this.option = option;
    }

    @Override
    protected AbstractWidget createValueWidget(int index, int x, int y, int width, int height) {
        EditBox field = new EditBox(getFont(), x, y, width, height, option.getDisplayName());
        field.setMaxLength(256);
        field.setValue(draft.get(index));
        field.setResponder(value -> draft.set(index, value));
        return field;
    }

    @Override
    protected String createNewEntry() {
        return "";
    }

    @Override
    protected void applyValues(List<String> values) {
        option.setStrings(values);
    }
}
