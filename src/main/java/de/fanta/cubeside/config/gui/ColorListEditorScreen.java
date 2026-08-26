package de.fanta.cubeside.config.gui;

import de.fanta.cubeside.config.option.ArgbColor;
import de.fanta.cubeside.config.option.ConfigColorList;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ColorListEditorScreen extends AbstractListEditorScreen<ArgbColor> {
    private final ConfigColorList option;

    public ColorListEditorScreen(Screen parent, ConfigColorList option) {
        super(parent, option.getDisplayName(), option.getColors(), option.getDefaultColors());
        this.option = option;
    }

    @Override
    protected AbstractWidget createValueWidget(int index, int x, int y, int width, int height) {
        ArgbColor color = draft.get(index).opaque();
        return Button.builder(colorButtonLabel(color), ignored -> {
            ArgbColor defaultColor = index < option.getDefaultColors().size()
                    ? option.getDefaultColors().get(index)
                    : ArgbColor.fromColor(0xFFFFFFFF);
            minecraft.setScreenAndShow(new ColorEditorScreen(this, option.getDisplayName(), draft.get(index), defaultColor,
                    value -> draft.set(index, ArgbColor.fromColor(value))));
        }).bounds(x, y, width, height).build();
    }

    @Override
    protected ArgbColor createNewEntry() {
        return ArgbColor.fromColor(0xFFFFFFFF);
    }

    @Override
    protected void applyValues(List<ArgbColor> values) {
        option.setColors(values);
    }

    private static Component colorButtonLabel(ArgbColor color) {
        return Component.empty()
                .append(Component.literal("■ ").setStyle(net.minecraft.network.chat.Style.EMPTY
                        .withColor(net.minecraft.network.chat.TextColor.fromRgb(color.intValue & 0x00FFFFFF))))
                .append(Component.literal(color.toRgbHexString()));
    }
}
