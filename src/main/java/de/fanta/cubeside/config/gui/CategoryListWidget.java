package de.fanta.cubeside.config.gui;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import de.fanta.cubeside.config.ConfigGui;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class CategoryListWidget extends AbstractSelectionList<CategoryListWidget.Entry> {
    private final ConfigGui screen;
    private final List<SettingsCategory> categories;

    public CategoryListWidget(ConfigGui screen, int x, int y, int width, int height, List<SettingsCategory> categories) {
        super(Minecraft.getInstance(), width, height, y, 24);
        this.screen = screen;
        this.categories = categories;
        this.centerListVertically = false;
        updateSizeAndPosition(width, height, x, y);
        for (int i = 0; i < categories.size(); i++) {
            addEntry(new Entry(i));
        }
    }

    @Override
    public int getRowWidth() {
        return Math.max(80, getWidth() - 8);
    }

    @Override
    protected int scrollBarX() {
        return getX() + getWidth() - 5;
    }

    @Override
    protected boolean entriesCanBeSelected() {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }

    public final class Entry extends AbstractSelectionList.Entry<Entry> {
        private final int index;
        private final Button button;

        private Entry(int index) {
            this.index = index;
            this.button = new CategoryButton(0, 0, 100, 20, Component.empty(), ignored -> screen.selectCategory(index));
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float delta) {
            button.setRectangle(getWidth(), 20, getX(), getY() + 2);
            boolean selected = screen.selectedCategory() == index;
            button.active = !selected;
            button.setMessage(selected
                    ? Component.literal("◆ ").withStyle(ChatFormatting.AQUA).append(categories.get(index).title())
                    : categories.get(index).title());
            button.extractRenderState(graphics, mouseX, mouseY, delta);
        }

        @Override
        public void visitWidgets(Consumer<AbstractWidget> consumer) {
            consumer.accept(button);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            return button.mouseClicked(event, doubleClick);
        }
    }

    private static final class CategoryButton extends Button.Plain {
        private CategoryButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void handleCursor(GuiGraphicsExtractor graphics) {
            if (isHovered() && !isActive()) {
                graphics.requestCursor(CursorTypes.ARROW);
            } else {
                super.handleCursor(graphics);
            }
        }
    }
}
