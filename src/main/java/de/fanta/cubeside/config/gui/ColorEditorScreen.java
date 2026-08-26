package de.fanta.cubeside.config.gui;

import de.fanta.cubeside.config.option.ArgbColor;
import java.awt.Color;
import java.util.Locale;
import java.util.function.IntConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ColorEditorScreen extends Screen {
    private final Screen parent;
    private final ArgbColor defaultColor;
    private final IntConsumer onApply;
    private int draftColor;
    private float hue;
    private float saturation;
    private float brightness;
    private EditBox hexField;
    private boolean syncingHex;
    private ColorSlider hueSlider;
    private ColorSlider saturationSlider;
    private ColorSlider brightnessSlider;

    public ColorEditorScreen(Screen parent, Component title, ArgbColor initialColor, ArgbColor defaultColor, IntConsumer onApply) {
        super(title);
        this.parent = parent;
        this.defaultColor = defaultColor;
        this.onApply = onApply;
        setDraftColor(initialColor.intValue);
    }

    @Override
    protected void init() {
        clearWidgets();
        int panelWidth = Math.min(420, width - 24);
        int panelX = (width - panelWidth) / 2;
        int sliderWidth = panelWidth - 40;
        int x = panelX + 20;
        int y = Math.max(42, height / 2 - 72);

        hueSlider = new ColorSlider(x, y, sliderWidth, "H", Channel.HUE);
        saturationSlider = new ColorSlider(x, y + 24, sliderWidth, "S", Channel.SATURATION);
        brightnessSlider = new ColorSlider(x, y + 48, sliderWidth, "V", Channel.BRIGHTNESS);
        addRenderableWidget(hueSlider);
        addRenderableWidget(saturationSlider);
        addRenderableWidget(brightnessSlider);

        hexField = new EditBox(getFont(), x + 56, y + 78, sliderWidth - 90, 20, Component.translatable("cubeside.settings.color.hex"));
        hexField.setMaxLength(9);
        syncHex();
        hexField.setResponder(this::updateFromHex);
        addRenderableWidget(hexField);

        int buttonY = Math.min(height - 27, y + 108);
        addRenderableWidget(Button.builder(Component.translatable("controls.reset"), ignored -> setDraftColor(defaultColor.intValue))
                .bounds(width / 2 - 155, buttonY, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), ignored -> closeWithoutApplying())
                .bounds(width / 2 - 50, buttonY, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("cubeside.settings.apply"), ignored -> applyAndClose())
                .bounds(width / 2 + 55, buttonY, 100, 20).build());
    }

    private void updateFromHex(String value) {
        if (syncingHex) {
            return;
        }
        try {
            int parsed = 0xFF000000 | ArgbColor.parse(value) & 0x00FFFFFF;
            hexField.setTextColor(0xFFFFFFFF);
            setDraftColor(parsed, false);
        } catch (IllegalArgumentException exception) {
            hexField.setTextColor(0xFFFF5555);
        }
    }

    private void setDraftColor(int color) {
        setDraftColor(color, true);
    }

    private void setDraftColor(int color, boolean updateHex) {
        draftColor = 0xFF000000 | color & 0x00FFFFFF;
        float[] hsv = Color.RGBtoHSB(color >>> 16 & 0xFF, color >>> 8 & 0xFF, color & 0xFF, null);
        hue = hsv[0];
        saturation = hsv[1];
        brightness = hsv[2];
        syncSliders();
        if (updateHex && hexField != null) {
            syncHex();
        }
    }

    private void syncSliders() {
        if (hueSlider != null) {
            hueSlider.sync();
            saturationSlider.sync();
            brightnessSlider.sync();
        }
    }

    private void updateDraftFromChannels() {
        int rgb = Color.HSBtoRGB(hue, saturation, brightness) & 0x00FFFFFF;
        draftColor = 0xFF000000 | rgb;
        syncHex();
    }

    private void syncHex() {
        syncingHex = true;
        hexField.setValue(ArgbColor.fromColor(draftColor).toRgbHexString());
        hexField.setTextColor(0xFFFFFFFF);
        syncingHex = false;
    }

    private void applyAndClose() {
        onApply.accept(draftColor);
        minecraft.setScreenAndShow(parent);
    }

    private void closeWithoutApplying() {
        minecraft.setScreenAndShow(parent);
    }

    @Override
    public void onClose() {
        closeWithoutApplying();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(getFont(), title, width / 2, 16, 0xFFFFFFFF);
        int y = Math.max(42, height / 2 - 72) + 80;
        int x = width / 2 - Math.min(420, width - 24) / 2 + 20;
        graphics.text(getFont(), Component.translatable("cubeside.settings.color.hex"), x, y + 6, 0xFFFFFFFF);
        int previewX = x + Math.min(420, width - 24) - 62;
        graphics.fill(previewX - 1, y - 1, previewX + 22, y + 21, 0xFFFFFFFF);
        graphics.fill(previewX, y, previewX + 21, y + 20, 0xFF303030);
        graphics.fill(previewX, y, previewX + 21, y + 20, draftColor);
    }

    private enum Channel {
        HUE, SATURATION, BRIGHTNESS
    }

    private final class ColorSlider extends AbstractSliderButton {
        private final String label;
        private final Channel channel;

        private ColorSlider(int x, int y, int width, String label, Channel channel) {
            super(x, y, width, 20, Component.empty(), channelValue(channel));
            this.label = label;
            this.channel = channel;
            updateMessage();
        }

        private void sync() {
            value = channelValue(channel);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal(label + ": " + String.format(Locale.ROOT, "%3d%%", Math.round(value * 100.0))));
        }

        @Override
        protected void applyValue() {
            float newValue = (float) value;
            switch (channel) {
                case HUE -> hue = newValue;
                case SATURATION -> saturation = newValue;
                case BRIGHTNESS -> brightness = newValue;
            }
            updateDraftFromChannels();
            updateMessage();
        }
    }

    private double channelValue(Channel channel) {
        return switch (channel) {
            case HUE -> hue;
            case SATURATION -> saturation;
            case BRIGHTNESS -> brightness;
        };
    }
}
