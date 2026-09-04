package de.fanta.cubeside.config.gui;

import de.fanta.cubeside.config.ConfigGui;
import de.fanta.cubeside.config.option.ConfigBoolean;
import de.fanta.cubeside.config.option.ConfigColor;
import de.fanta.cubeside.config.option.ConfigColorList;
import de.fanta.cubeside.config.option.ConfigDouble;
import de.fanta.cubeside.config.option.ConfigInteger;
import de.fanta.cubeside.config.option.ConfigString;
import de.fanta.cubeside.config.option.ConfigStringList;
import de.fanta.cubeside.config.option.ConfigValue;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.MutableComponent;
import org.lwjgl.glfw.GLFW;

public final class SettingsListWidget extends ContainerObjectSelectionList<SettingsListWidget.Entry> {
    private static final int OPTION_HEIGHT = 28;
    private static final int HEADER_HEIGHT = 24;
    private final ConfigGui screen;

    public SettingsListWidget(ConfigGui screen, int x, int y, int width, int height, SettingsCategory category) {
        super(Minecraft.getInstance(), width, height, y, OPTION_HEIGHT);
        this.screen = screen;
        this.centerListVertically = false;
        updateSizeAndPosition(width, height, x, y);
        for (SettingsGroup group : category.groups()) {
            addEntry(new GroupEntry(group.title()), HEADER_HEIGHT);
            for (ConfigValue<?> option : group.options()) {
                addEntry(new OptionEntry(option), OPTION_HEIGHT);
            }
        }
    }

    @Override
    public int getRowWidth() {
        return Math.max(120, getWidth() - 10);
    }

    @Override
    protected int scrollBarX() {
        return getX() + getWidth() - 6;
    }

    @Override
    protected boolean entriesCanBeSelected() {
        return false;
    }

    public void commitPendingText() {
        for (Entry entry : children()) {
            if (entry instanceof OptionEntry optionEntry && optionEntry.control instanceof CommitEditBox editBox) {
                editBox.commit();
            }
        }
    }

    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
    }

    private final class GroupEntry extends Entry {
        private final Component title;

        private GroupEntry(Component title) {
            this.title = title;
        }

        @Override
        public List<AbstractWidget> children() {
            return List.of();
        }

        @Override
        public List<AbstractWidget> narratables() {
            return List.of();
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
            graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight() - 2, 0xAA202020);
            graphics.text(screen.getFont(), title.copy().withStyle(ChatFormatting.BOLD), getX() + 8, getY() + 7, 0xFFFFFFFF);
        }
    }

    private final class OptionEntry extends Entry {
        private final ConfigValue<?> option;
        private final AbstractWidget control;

        private OptionEntry(ConfigValue<?> option) {
            this.option = option;
            this.control = createControl(option);
        }

        @Override
        public List<AbstractWidget> children() {
            return List.of(control);
        }

        @Override
        public List<AbstractWidget> narratables() {
            return List.of(control);
        }

        private AbstractWidget createControl(ConfigValue<?> value) {
            if (value instanceof ConfigBoolean option) {
                return Button.builder(Component.empty(), ignored -> option.setBooleanValue(!option.getBooleanValue()))
                        .bounds(0, 0, 110, 20).build();
            }
            if (value instanceof ConfigInteger option && option.shouldUseSlider()) {
                return new OptionSlider(option);
            }
            if (value instanceof ConfigDouble option && option.shouldUseSlider()) {
                return new OptionSlider(option);
            }
            if (value instanceof ConfigString option) {
                return new CommitEditBox(option);
            }
            if (value instanceof ConfigInteger option) {
                return new CommitEditBox(option);
            }
            if (value instanceof ConfigDouble option) {
                return new CommitEditBox(option);
            }
            return Button.builder(Component.empty(), ignored -> screen.openEditor(value))
                    .bounds(0, 0, 140, 20).build();
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
            int controlWidth = Math.clamp(getWidth() / 2, 105, 190);
            control.setRectangle(controlWidth, 20, getX() + getWidth() - controlWidth - 8, getY() + 4);

            if (control instanceof Button) {
                control.setMessage(formatOption(option));
            } else if (control instanceof OptionSlider slider && !slider.isFocused()) {
                slider.sync();
            } else if (control instanceof CommitEditBox editBox) {
                editBox.syncIfIdle();
            }
            Component tooltip = option.getTooltip();
            control.setTooltip(tooltip.getString().isEmpty() ? null : Tooltip.create(tooltip));

            int labelMaxWidth = control.getX() - getX() - 20;
            String label = screen.getFont().plainSubstrByWidth(option.getDisplayName().getString(), Math.max(20, labelMaxWidth));
            graphics.text(screen.getFont(), label, getX() + 10, getY() + 10, 0xFFFFFFFF);
            control.extractRenderState(graphics, mouseX, mouseY, delta);
        }

        @Override
        public void visitWidgets(Consumer<AbstractWidget> consumer) {
            consumer.accept(control);
        }

    }

    private static Component formatOption(ConfigValue<?> option) {
        if (option instanceof ConfigBoolean value) {
            return Component.translatable(value.getBooleanValue() ? "options.on" : "options.off");
        }
        if (option instanceof ConfigColor value) {
            return colorButtonLabel(value.getColor());
        }
        if (option instanceof ConfigColorList value) {
            if (value.getColors().isEmpty()) {
                return Component.translatable("cubeside.settings.noEntries");
            }
            MutableComponent label = Component.empty();
            for (de.fanta.cubeside.config.option.ArgbColor color : value.getColors()) {
                label.append(coloredSquare(color));
            }
            return label;
        }
        if (option instanceof ConfigStringList value) {
            return Component.translatable("cubeside.settings.entries", value.getStrings().size());
        }
        return Component.literal(String.valueOf(option.getValue()));
    }

    private static Component colorButtonLabel(de.fanta.cubeside.config.option.ArgbColor color) {
        return Component.empty().append(coloredSquare(color)).append(Component.literal(color.toRgbHexString()));
    }

    private static Component coloredSquare(de.fanta.cubeside.config.option.ArgbColor color) {
        return Component.literal("■ ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color.intValue & 0x00FFFFFF)));
    }

    private final class OptionSlider extends AbstractSliderButton {
        private final ConfigValue<?> option;
        private final double minimum;
        private final double maximum;
        private final double step;

        private OptionSlider(ConfigInteger option) {
            this(option, option.getMinIntegerValue(), option.getMaxIntegerValue(), 1.0);
        }

        private OptionSlider(ConfigDouble option) {
            this(option, option.getMinDoubleValue(), option.getMaxDoubleValue(), 0.01);
        }

        private OptionSlider(ConfigValue<?> option, double minimum, double maximum, double step) {
            super(0, 0, 140, 20, Component.empty(), normalize(option, minimum, maximum));
            this.option = option;
            this.minimum = minimum;
            this.maximum = maximum;
            this.step = step;
            updateMessage();
        }

        private void sync() {
            value = normalize(option, minimum, maximum);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            double denormalized = denormalize(value, minimum, maximum, step);
            setMessage(option instanceof ConfigInteger
                    ? Component.literal(Integer.toString((int) Math.round(denormalized)))
                    : Component.literal(formatDecimal(denormalized)));
        }

        @Override
        protected void applyValue() {
            double denormalized = denormalize(value, minimum, maximum, step);
            if (option instanceof ConfigInteger integer) {
                integer.setIntegerValue((int) Math.round(denormalized));
            } else if (option instanceof ConfigDouble decimal) {
                decimal.setDoubleValue(denormalized);
            }
            updateMessage();
        }
    }

    private final class CommitEditBox extends EditBox {
        private final ConfigValue<?> option;
        private boolean dirty;
        private boolean syncing;

        private CommitEditBox(ConfigValue<?> option) {
            super(screen.getFont(), 0, 0, 140, 20, option.getDisplayName());
            this.option = option;
            setMaxLength(512);
            setFromOption();
            setResponder(ignored -> {
                if (!syncing) {
                    dirty = true;
                }
            });
        }

        private void syncIfIdle() {
            if (!isFocused() && !dirty && !getValue().equals(currentText())) {
                setFromOption();
            }
        }

        private String currentText() {
            if (option instanceof ConfigString string) {
                return string.getStringValue();
            }
            if (option instanceof ConfigInteger integer) {
                return Integer.toString(integer.getIntegerValue());
            }
            return Double.toString(((ConfigDouble) option).getDoubleValue());
        }

        private void setFromOption() {
            syncing = true;
            setValue(currentText());
            setTextColor(0xFFFFFFFF);
            syncing = false;
        }

        private void commit() {
            if (!dirty) {
                return;
            }
            try {
                if (option instanceof ConfigString string) {
                    string.setValueFromString(getValue());
                } else if (option instanceof ConfigInteger integer) {
                    integer.setIntegerValue(Integer.parseInt(getValue()));
                } else if (option instanceof ConfigDouble decimal) {
                    decimal.setDoubleValue(Double.parseDouble(getValue()));
                }
                dirty = false;
                setFromOption();
            } catch (IllegalArgumentException exception) {
                setTextColor(0xFFFF5555);
            }
        }

        @Override
        public void setFocused(boolean focused) {
            if (!focused && isFocused()) {
                commit();
            }
            super.setFocused(focused);
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
                commit();
                if (!dirty) {
                    setFocused(false);
                }
                return true;
            }
            return super.keyPressed(event);
        }
    }

    private static double normalize(ConfigValue<?> option, double minimum, double maximum) {
        double current = option instanceof ConfigInteger integer ? integer.getIntegerValue() : ((ConfigDouble) option).getDoubleValue();
        return maximum == minimum ? 0.0 : (current - minimum) / (maximum - minimum);
    }

    private static double denormalize(double normalized, double minimum, double maximum, double step) {
        double raw = minimum + normalized * (maximum - minimum);
        double stepped = Math.round((raw - minimum) / step) * step + minimum;
        return Math.clamp(stepped, minimum, maximum);
    }

    private static String formatDecimal(double value) {
        String formatted = String.format(Locale.ROOT, "%.2f", value);
        return formatted.replaceAll("0+$", "").replaceAll("\\.$", "");
    }
}
