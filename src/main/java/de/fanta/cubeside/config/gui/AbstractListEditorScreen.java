package de.fanta.cubeside.config.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

abstract class AbstractListEditorScreen<T> extends Screen {
    protected final Screen parent;
    protected final List<T> draft;
    private final List<T> defaults;
    private int scrollOffset;
    private int visibleRows;

    protected AbstractListEditorScreen(Screen parent, Component title, List<T> initial, List<T> defaults) {
        super(title);
        this.parent = parent;
        this.draft = new ArrayList<>(initial);
        this.defaults = List.copyOf(defaults);
    }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    protected final void rebuildWidgets() {
        clearWidgets();
        int contentWidth = Math.min(600, width - 24);
        int x = (width - contentWidth) / 2;
        int top = 38;
        int bottom = height - 62;
        visibleRows = Math.max(1, (bottom - top) / 24);
        scrollOffset = Math.clamp(scrollOffset, 0, Math.max(0, draft.size() - visibleRows));

        int end = Math.min(draft.size(), scrollOffset + visibleRows);
        for (int index = scrollOffset; index < end; index++) {
            int rowY = top + (index - scrollOffset) * 24;
            int controlsWidth = 78;
            AbstractWidget valueWidget = createValueWidget(index, x, rowY, contentWidth - controlsWidth - 4, 20);
            addRenderableWidget(valueWidget);

            int buttonX = x + contentWidth - controlsWidth;
            int currentIndex = index;
            Button up = Button.builder(Component.literal("↑"), ignored -> move(currentIndex, -1))
                    .bounds(buttonX, rowY, 24, 20).build();
            up.active = index > 0;
            addRenderableWidget(up);
            Button down = Button.builder(Component.literal("↓"), ignored -> move(currentIndex, 1))
                    .bounds(buttonX + 27, rowY, 24, 20).build();
            down.active = index + 1 < draft.size();
            addRenderableWidget(down);
            addRenderableWidget(Button.builder(Component.literal("×"), ignored -> remove(currentIndex))
                    .bounds(buttonX + 54, rowY, 24, 20).build());
        }

        int actionY = height - 52;
        addRenderableWidget(Button.builder(Component.translatable("cubeside.settings.list.add"), ignored -> addEntry())
                .bounds(width / 2 - 102, actionY, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("controls.reset"), ignored -> resetDraft())
                .bounds(width / 2 + 2, actionY, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), ignored -> closeWithoutApplying())
                .bounds(width / 2 - 102, height - 27, 100, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("cubeside.settings.apply"), ignored -> applyAndClose())
                .bounds(width / 2 + 2, height - 27, 100, 20).build());
    }

    protected abstract AbstractWidget createValueWidget(int index, int x, int y, int width, int height);

    protected abstract T createNewEntry();

    protected abstract void applyValues(List<T> values);

    private void addEntry() {
        draft.add(createNewEntry());
        scrollOffset = Math.max(0, draft.size() - visibleRows);
        rebuildWidgets();
    }

    private void remove(int index) {
        draft.remove(index);
        rebuildWidgets();
    }

    private void move(int index, int direction) {
        int target = index + direction;
        if (target < 0 || target >= draft.size()) {
            return;
        }
        Collections.swap(draft, index, target);
        if (target < scrollOffset) {
            scrollOffset = target;
        } else if (target >= scrollOffset + visibleRows) {
            scrollOffset = target - visibleRows + 1;
        }
        rebuildWidgets();
    }

    private void resetDraft() {
        draft.clear();
        draft.addAll(defaults);
        scrollOffset = 0;
        rebuildWidgets();
    }

    private void applyAndClose() {
        applyValues(List.copyOf(draft));
        minecraft.setScreenAndShow(parent);
    }

    private void closeWithoutApplying() {
        minecraft.setScreenAndShow(parent);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int oldOffset = scrollOffset;
        if (verticalAmount > 0) {
            scrollOffset--;
        } else if (verticalAmount < 0) {
            scrollOffset++;
        }
        scrollOffset = Math.clamp(scrollOffset, 0, Math.max(0, draft.size() - visibleRows));
        if (oldOffset != scrollOffset) {
            rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void onClose() {
        closeWithoutApplying();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(getFont(), title, width / 2, 16, 0xFFFFFFFF);
        if (draft.isEmpty()) {
            graphics.centeredText(getFont(), Component.translatable("cubeside.settings.list.empty"), width / 2, 54, 0xFFAAAAAA);
        }
        if (draft.size() > visibleRows) {
            graphics.text(getFont(), Component.literal((scrollOffset + 1) + "–" + Math.min(draft.size(), scrollOffset + visibleRows) + " / " + draft.size()),
                    width / 2 + Math.min(600, width - 24) / 2 - 70, 18, 0xFFAAAAAA);
        }
    }
}
