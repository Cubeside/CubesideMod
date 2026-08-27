package de.fanta.cubeside.config.gui;

import de.fanta.cubeside.config.option.ArgbColor;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class ColorPickerDialog extends AbstractContainerWidget {
    private static final int PANEL_PADDING = 12;
    private static final int LIST_ROW_HEIGHT = 24;
    private static final int VISIBLE_COLOR_ROWS = 5;
    private static final int VALUE_SLIDER_WIDTH = 18;

    private final Font font;
    private final ColorListDraft draft;
    private final ColorPickerModel colorModel;
    private final Consumer<List<ArgbColor>> onApply;
    private final Runnable onClose;
    private final List<AbstractWidget> children = new ArrayList<>();
    private final ColorWheelWidget colorWheel;
    private final ValueSliderWidget valueSlider;
    private final HexEditBox hexField;
    private final Button resetButton;
    private final Button cancelButton;
    private final Button applyButton;
    private final ColorListWidget colorList;
    private final Button addButton;

    private int panelX;
    private int panelY;
    private int panelWidth;
    private int panelHeight;
    private int previewX;
    private int previewY;
    private boolean syncingHex;
    private boolean hexValid = true;

    public static ColorPickerDialog forSingle(int screenWidth, int screenHeight, Font font, Component title,
            ArgbColor initialColor, ArgbColor defaultColor, IntConsumer onApply, Runnable onClose) {
        return new ColorPickerDialog(screenWidth, screenHeight, font, title,
                ColorListDraft.single(initialColor, defaultColor),
                colors -> onApply.accept(colors.getFirst().intValue), onClose);
    }

    public static ColorPickerDialog forList(int screenWidth, int screenHeight, Font font, Component title,
            List<ArgbColor> initialColors, List<ArgbColor> defaultColors,
            Consumer<List<ArgbColor>> onApply, Runnable onClose) {
        return new ColorPickerDialog(screenWidth, screenHeight, font, title,
                ColorListDraft.list(initialColors, defaultColors), onApply, onClose);
    }

    private ColorPickerDialog(int screenWidth, int screenHeight, Font font, Component title,
            ColorListDraft draft, Consumer<List<ArgbColor>> onApply, Runnable onClose) {
        super(0, 0, screenWidth, screenHeight, title);
        this.font = font;
        this.draft = draft;
        this.onApply = onApply;
        this.onClose = onClose;
        this.colorModel = new ColorPickerModel(draft.selectedColor().orElse(ArgbColor.fromColor(0xFFFFFFFF)));

        ColorWheelTexture.ensureRegistered(Minecraft.getInstance());

        colorWheel = new ColorWheelWidget();
        valueSlider = new ValueSliderWidget();
        hexField = new HexEditBox();
        resetButton = Button.builder(Component.translatable("controls.reset"), ignored -> reset()).build();
        cancelButton = Button.builder(Component.translatable("gui.cancel"), ignored -> onClose.run()).build();
        applyButton = Button.builder(Component.translatable("cubeside.settings.apply"), ignored -> apply()).build();

        colorList = draft.listMode() ? new ColorListWidget() : null;
        addButton = draft.listMode()
                ? Button.builder(Component.translatable("cubeside.settings.color.add"), ignored -> addColor()).build()
                : null;

        if (colorList != null) {
            children.add(colorList);
            children.add(addButton);
        }
        children.add(colorWheel);
        children.add(valueSlider);
        children.add(hexField);
        children.add(resetButton);
        children.add(cancelButton);
        children.add(applyButton);

        setScreenSize(screenWidth, screenHeight);
        syncSelectedColor();
    }

    public void setScreenSize(int screenWidth, int screenHeight) {
        setRectangle(screenWidth, screenHeight, 0, 0);
        panelWidth = Math.min(draft.listMode() ? 600 : 400, Math.max(240, screenWidth - 16));
        panelHeight = Math.min(360, Math.max(220, screenHeight - 16));
        panelX = (screenWidth - panelWidth) / 2;
        panelY = (screenHeight - panelHeight) / 2;

        int innerX = panelX + PANEL_PADDING;
        int innerWidth = panelWidth - PANEL_PADDING * 2;
        int buttonsY = panelY + panelHeight - PANEL_PADDING - 20;
        int hexY = buttonsY - 30;
        int pickerTop = panelY + 30;
        int pickerHeight = Math.max(80, hexY - pickerTop - 12);

        int mainX = innerX;
        int mainWidth = innerWidth;
        if (draft.listMode()) {
            int listWidth = Math.clamp(panelWidth / 3, 140, 180);
            int listHeight = LIST_ROW_HEIGHT * VISIBLE_COLOR_ROWS;
            colorList.setRectangle(listWidth, listHeight, innerX, pickerTop);
            draft.setVisibleRows(VISIBLE_COLOR_ROWS);
            addButton.setRectangle(listWidth, 20, innerX, pickerTop + listHeight + 4);
            mainX += listWidth + 10;
            mainWidth -= listWidth + 10;
        }

        int wheelSize = Math.min(220, Math.min(pickerHeight, mainWidth - VALUE_SLIDER_WIDTH - 8));
        wheelSize = Math.max(72, wheelSize);
        int pickerWidth = wheelSize + 8 + VALUE_SLIDER_WIDTH;
        int wheelX = mainX + Math.max(0, (mainWidth - pickerWidth) / 2);
        int wheelY = pickerTop + Math.max(0, (pickerHeight - wheelSize) / 2);
        colorWheel.setRectangle(wheelSize, wheelSize, wheelX, wheelY);
        valueSlider.setRectangle(VALUE_SLIDER_WIDTH, wheelSize, wheelX + wheelSize + 8, wheelY);

        previewX = mainX;
        previewY = hexY;
        int hexX = previewX + 28;
        hexField.setRectangle(Math.max(40, mainWidth - 28), 20, hexX, hexY);

        int buttonGap = 6;
        int buttonWidth = Math.min(100, (innerWidth - buttonGap * 2) / 3);
        int buttonsWidth = buttonWidth * 3 + buttonGap * 2;
        int buttonX = panelX + (panelWidth - buttonsWidth) / 2;
        resetButton.setRectangle(buttonWidth, 20, buttonX, buttonsY);
        cancelButton.setRectangle(buttonWidth, 20, buttonX + buttonWidth + buttonGap, buttonsY);
        applyButton.setRectangle(buttonWidth, 20, buttonX + (buttonWidth + buttonGap) * 2, buttonsY);
    }

    private void addColor() {
        draft.addColor();
        syncSelectedColor();
    }

    private void reset() {
        draft.reset();
        syncSelectedColor();
    }

    private void apply() {
        if (!applyButton.active) {
            return;
        }
        onApply.accept(draft.colors());
        onClose.run();
    }

    private void syncSelectedColor() {
        boolean hasColor = draft.selectedColor().isPresent();
        if (hasColor) {
            colorModel.setColor(draft.selectedColor().orElseThrow());
            syncHex();
        } else {
            syncingHex = true;
            hexField.setValue("");
            syncingHex = false;
            hexField.setTextColor(0xFFAAAAAA);
        }
        hexValid = true;
        colorWheel.active = hasColor;
        valueSlider.active = hasColor;
        hexField.active = hasColor;
        hexField.setEditable(hasColor);
        updateApplyState();
    }

    private void updateColorFromPicker() {
        draft.setSelectedColor(colorModel.color());
        hexValid = true;
        syncHex();
        updateApplyState();
    }

    private void updateFromHex(String text) {
        if (syncingHex) {
            return;
        }
        OptionalInt parsed = ColorPickerModel.parseHex(text);
        hexValid = parsed.isPresent();
        if (hexValid) {
            colorModel.setColor(ArgbColor.fromColor(parsed.getAsInt()));
            draft.setSelectedColor(colorModel.color());
            hexField.setTextColor(0xFFFFFFFF);
        } else {
            hexField.setTextColor(0xFFFF5555);
        }
        updateApplyState();
    }

    private void normalizeHex() {
        if (hexValid && draft.selectedColor().isPresent()) {
            syncHex();
        }
    }

    private void syncHex() {
        syncingHex = true;
        hexField.setValue(colorModel.hex());
        hexField.setTextColor(0xFFFFFFFF);
        syncingHex = false;
    }

    private void updateApplyState() {
        applyButton.active = draft.listMode() && draft.colors().isEmpty() || hexValid;
    }

    @Override
    protected int contentHeight() {
        return getHeight();
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (colorList != null && colorList.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        return false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, 0, getWidth(), getHeight(), 0x99000000);
        graphics.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY + panelHeight + 1, 0xFFAAAAAA);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0xF0202020);
        graphics.centeredText(font, getMessage(), panelX + panelWidth / 2, panelY + 10, 0xFFFFFFFF);

        graphics.text(font, Component.translatable("cubeside.settings.color.hex"), previewX, previewY - 10, 0xFFCCCCCC);
        if (!hexValid) {
            Component invalid = Component.translatable("cubeside.settings.color.invalid");
            graphics.text(font, invalid, panelX + panelWidth - PANEL_PADDING - font.width(invalid), previewY - 10, 0xFFFF5555);
        }

        graphics.fill(previewX - 1, previewY - 1, previewX + 23, previewY + 21, 0xFFFFFFFF);
        graphics.fill(previewX, previewY, previewX + 22, previewY + 20,
                draft.selectedColor().map(color -> color.intValue).orElse(0xFF303030));

        for (AbstractWidget child : children) {
            child.extractRenderState(graphics, mouseX, mouseY, delta);
        }

        if (draft.selectedColor().isEmpty()) {
            graphics.centeredText(font, Component.translatable("cubeside.settings.list.empty"),
                    colorWheel.getX() + colorWheel.getWidth() / 2, colorWheel.getY() + colorWheel.getHeight() / 2 - 4,
                    0xFFAAAAAA);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, getMessage());
    }

    private final class HexEditBox extends EditBox {
        private HexEditBox() {
            super(font, 0, 0, 120, 20, Component.translatable("cubeside.settings.color.hex"));
            setMaxLength(7);
            setResponder(ColorPickerDialog.this::updateFromHex);
        }

        @Override
        public void setFocused(boolean focused) {
            boolean losingFocus = !focused && isFocused();
            super.setFocused(focused);
            if (losingFocus) {
                normalizeHex();
            }
        }

        @Override
        public boolean keyPressed(KeyEvent event) {
            if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_KP_ENTER) {
                normalizeHex();
                if (hexValid) {
                    setFocused(false);
                }
                return true;
            }
            return super.keyPressed(event);
        }
    }

    private final class ColorWheelWidget extends AbstractWidget {
        private ColorWheelWidget() {
            super(0, 0, 100, 100, Component.literal("H/S"));
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            graphics.blit(ColorWheelTexture.ID, getX(), getY(), getRight(), getBottom(), 0.0F, 1.0F, 0.0F, 1.0F);
            if (!active) {
                graphics.fill(getX(), getY(), getRight(), getBottom(), 0x88000000);
                return;
            }

            double angle = colorModel.hue() * Math.PI * 2.0;
            double radius = (getWidth() - 1) / 2.0;
            int markerX = (int) Math.round(getX() + radius + Math.cos(angle) * colorModel.saturation() * radius);
            int markerY = (int) Math.round(getY() + radius + Math.sin(angle) * colorModel.saturation() * radius);
            graphics.fill(markerX - 3, markerY - 3, markerX + 4, markerY + 4, 0xFF000000);
            graphics.outline(markerX - 2, markerY - 2, 5, 5, 0xFFFFFFFF);
            graphics.text(font, "H/S", getX() + 4, getBottom() - 12, 0xFFFFFFFF, true);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            updateFromMouse(event.x(), event.y());
        }

        @Override
        protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
            updateFromMouse(event.x(), event.y());
        }

        private void updateFromMouse(double mouseX, double mouseY) {
            double radius = getWidth() / 2.0;
            double normalizedX = (mouseX - (getX() + radius)) / radius;
            double normalizedY = (mouseY - (getY() + radius)) / radius;
            if (colorModel.setWheelPoint(normalizedX, normalizedY)) {
                updateColorFromPicker();
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private final class ValueSliderWidget extends AbstractWidget {
        private ValueSliderWidget() {
            super(0, 0, VALUE_SLIDER_WIDTH, 100, Component.literal("V"));
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            int topColor = active ? colorModel.fullValueColor().intValue : 0xFF555555;
            graphics.fillGradient(getX(), getY(), getRight(), getBottom(), topColor, 0xFF000000);
            graphics.outline(getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2, 0xFFFFFFFF);
            if (active) {
                int markerY = getY() + (int) Math.round((1.0 - colorModel.value()) * (getHeight() - 1));
                graphics.fill(getX() - 2, markerY - 2, getRight() + 2, markerY + 3, 0xFF000000);
                graphics.fill(getX() - 1, markerY - 1, getRight() + 1, markerY + 2, 0xFFFFFFFF);
            }
            graphics.centeredText(font, "V", getX() + getWidth() / 2, getBottom() - 12, 0xFFFFFFFF);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            updateFromMouse(event.y());
        }

        @Override
        protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
            updateFromMouse(event.y());
        }

        private void updateFromMouse(double mouseY) {
            double normalized = 1.0 - (mouseY - getY()) / Math.max(1, getHeight() - 1);
            colorModel.setValue(normalized);
            updateColorFromPicker();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    private final class ColorListWidget extends AbstractWidget {
        private static final int SCROLLBAR_WIDTH = 5;
        private static final int ACTION_WIDTH = 17;
        private boolean draggingScrollbar;

        private ColorListWidget() {
            super(0, 0, 120, 120, Component.translatable("cubeside.settings.color.list"));
        }

        @Override
        protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            graphics.fill(getX(), getY(), getRight(), getBottom(), 0xFF101010);
            graphics.outline(getX() - 1, getY() - 1, getWidth() + 2, getHeight() + 2, 0xFF777777);

            int contentRight = getRight() - SCROLLBAR_WIDTH - 1;
            graphics.enableScissor(getX() + 1, getY() + 1, contentRight, getBottom() - 1);
            List<ArgbColor> colors = draft.colors();
            int end = Math.min(colors.size(), draft.scrollOffset() + draft.visibleRows());
            for (int index = draft.scrollOffset(); index < end; index++) {
                int rowY = getY() + (index - draft.scrollOffset()) * LIST_ROW_HEIGHT;
                renderRow(graphics, mouseX, mouseY, colors.get(index), index, rowY, contentRight);
            }
            graphics.disableScissor();
            renderScrollbar(graphics);
        }

        private void renderRow(GuiGraphicsExtractor graphics, int mouseX, int mouseY, ArgbColor color,
                int index, int rowY, int contentRight) {
            int rowRight = contentRight - 1;
            int background = index == draft.selectedIndex() ? 0xFF31506F : 0xFF252525;
            if (mouseX >= getX() + 1 && mouseX < rowRight && mouseY >= rowY && mouseY < rowY + LIST_ROW_HEIGHT - 1) {
                background = index == draft.selectedIndex() ? 0xFF3D6388 : 0xFF333333;
            }
            graphics.fill(getX() + 1, rowY, rowRight, rowY + LIST_ROW_HEIGHT - 1, background);

            int actionX = rowRight - ACTION_WIDTH * 3;
            graphics.fill(getX() + 4, rowY + 4, getX() + 19, rowY + 19, color.intValue);
            graphics.outline(getX() + 3, rowY + 3, 17, 17, 0xFFFFFFFF);
            String hex = font.plainSubstrByWidth(color.toRgbHexString(), Math.max(0, actionX - getX() - 25));
            graphics.text(font, hex, getX() + 23, rowY + 8, 0xFFFFFFFF);

            renderAction(graphics, actionX, rowY + 3, "↑", index > 0);
            renderAction(graphics, actionX + ACTION_WIDTH, rowY + 3, "↓", index + 1 < draft.colors().size());
            renderAction(graphics, actionX + ACTION_WIDTH * 2, rowY + 3, "×", true);
        }

        private void renderAction(GuiGraphicsExtractor graphics, int x, int y, String label, boolean enabled) {
            graphics.fill(x, y, x + ACTION_WIDTH - 2, y + 18, enabled ? 0xFF555555 : 0xFF292929);
            graphics.outline(x, y, ACTION_WIDTH - 2, 18, enabled ? 0xFFAAAAAA : 0xFF555555);
            graphics.centeredText(font, label, x + (ACTION_WIDTH - 2) / 2, y + 5, enabled ? 0xFFFFFFFF : 0xFF777777);
        }

        private void renderScrollbar(GuiGraphicsExtractor graphics) {
            int maxOffset = Math.max(0, draft.colors().size() - draft.visibleRows());
            if (maxOffset == 0) {
                return;
            }
            int trackX = getRight() - SCROLLBAR_WIDTH;
            graphics.fill(trackX, getY(), getRight(), getBottom(), 0xFF202020);
            int thumbHeight = Math.max(12, getHeight() * draft.visibleRows() / draft.colors().size());
            int thumbTravel = getHeight() - thumbHeight;
            int thumbY = getY() + (int) Math.round(thumbTravel * (draft.scrollOffset() / (double) maxOffset));
            graphics.fill(trackX + 1, thumbY, getRight() - 1, thumbY + thumbHeight, 0xFFAAAAAA);
        }

        @Override
        public void onClick(MouseButtonEvent event, boolean doubleClick) {
            if (event.x() >= getRight() - SCROLLBAR_WIDTH
                    && draft.colors().size() > draft.visibleRows()) {
                draggingScrollbar = true;
                updateScrollbar(event.y());
                return;
            }

            int visibleIndex = (int) ((event.y() - getY()) / LIST_ROW_HEIGHT);
            int index = draft.scrollOffset() + visibleIndex;
            if (visibleIndex < 0 || visibleIndex >= draft.visibleRows() || index >= draft.colors().size()) {
                return;
            }

            int actionX = getRight() - SCROLLBAR_WIDTH - 2 - ACTION_WIDTH * 3;
            if (event.x() < actionX) {
                draft.select(index);
            } else if (event.x() < actionX + ACTION_WIDTH) {
                draft.move(index, -1);
            } else if (event.x() < actionX + ACTION_WIDTH * 2) {
                draft.move(index, 1);
            } else {
                draft.remove(index);
            }
            syncSelectedColor();
        }

        @Override
        protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
            if (draggingScrollbar) {
                updateScrollbar(event.y());
            }
        }

        @Override
        public void onRelease(MouseButtonEvent event) {
            draggingScrollbar = false;
            super.onRelease(event);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (!active || !visible || !isMouseOver(mouseX, mouseY) || verticalAmount == 0.0) {
                return false;
            }
            int oldOffset = draft.scrollOffset();
            draft.scrollRows(verticalAmount > 0.0 ? -1 : 1);
            return oldOffset != draft.scrollOffset();
        }

        private void updateScrollbar(double mouseY) {
            int maxOffset = Math.max(0, draft.colors().size() - draft.visibleRows());
            if (maxOffset == 0) {
                return;
            }
            int thumbHeight = Math.max(12, getHeight() * draft.visibleRows() / draft.colors().size());
            double position = (mouseY - getY() - thumbHeight / 2.0) / Math.max(1.0, getHeight() - thumbHeight);
            draft.setScrollOffset((int) Math.round(Math.clamp(position, 0.0, 1.0) * maxOffset));
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
