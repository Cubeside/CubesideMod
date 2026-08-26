package de.fanta.cubeside.config;

import de.fanta.cubeside.config.gui.CategoryListWidget;
import de.fanta.cubeside.config.gui.ColorEditorScreen;
import de.fanta.cubeside.config.gui.ColorListEditorScreen;
import de.fanta.cubeside.config.gui.CubesideSettings;
import de.fanta.cubeside.config.gui.SettingsCategory;
import de.fanta.cubeside.config.gui.SettingsListWidget;
import de.fanta.cubeside.config.gui.StringListEditorScreen;
import de.fanta.cubeside.config.option.ConfigColor;
import de.fanta.cubeside.config.option.ConfigColorList;
import de.fanta.cubeside.config.option.ConfigStringList;
import de.fanta.cubeside.config.option.ConfigValue;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ConfigGui extends Screen {
    private static final int HEADER_HEIGHT = 32;
    private static final int FOOTER_HEIGHT = 32;
    private static final int CATEGORY_GAP = 4;

    private final Screen parent;
    private final List<SettingsCategory> categories = CubesideSettings.create();
    private int selectedCategory;
    private int contentX;
    private int contentY;
    private int contentWidth;
    private int contentHeight;
    private int categoryWidth;
    private CategoryListWidget categoryList;
    private SettingsListWidget optionList;

    public ConfigGui(Screen parent) {
        super(Component.translatable("cubeside.gui.title.configs"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (optionList != null) {
            optionList.commitPendingText();
        }
        clearWidgets();
        categoryList = null;
        optionList = null;
        contentWidth = Math.max(180, Math.min(width - 16, 760));
        contentHeight = Math.max(80, height - HEADER_HEIGHT - FOOTER_HEIGHT);
        contentX = (width - contentWidth) / 2;
        contentY = HEADER_HEIGHT;
        categoryWidth = Math.clamp(contentWidth / 5, 96, 138);

        categoryList = new CategoryListWidget(this, contentX, contentY, categoryWidth, contentHeight, categories);
        addRenderableWidget(categoryList);
        rebuildOptions();

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), ignored -> onClose())
                .bounds(width / 2 - 100, height - 27, 200, 20).build());
    }

    public int selectedCategory() {
        return selectedCategory;
    }

    public void selectCategory(int index) {
        if (index == selectedCategory) {
            return;
        }
        if (optionList != null) {
            optionList.commitPendingText();
        }
        selectedCategory = index;
        rebuildOptions();
    }

    private void rebuildOptions() {
        if (optionList != null) {
            removeWidget(optionList);
        }
        int listX = contentX + categoryWidth + CATEGORY_GAP;
        int listWidth = contentWidth - categoryWidth - CATEGORY_GAP;
        optionList = new SettingsListWidget(this, listX, contentY, listWidth, contentHeight, categories.get(selectedCategory));
        addRenderableWidget(optionList);
    }

    public void openEditor(ConfigValue<?> option) {
        if (optionList != null) {
            optionList.commitPendingText();
        }
        if (option instanceof ConfigColor color) {
            minecraft.setScreenAndShow(new ColorEditorScreen(this, color.getDisplayName(), color.getColor(), color.getDefaultValue(), color::setIntegerValue));
        } else if (option instanceof ConfigColorList colors) {
            minecraft.setScreenAndShow(new ColorListEditorScreen(this, colors));
        } else if (option instanceof ConfigStringList strings) {
            minecraft.setScreenAndShow(new StringListEditorScreen(this, strings));
        }
    }

    @Override
    public void onClose() {
        if (optionList != null) {
            optionList.commitPendingText();
        }
        minecraft.setScreenAndShow(parent);
    }

    @Override
    public void removed() {
        if (optionList != null) {
            optionList.commitPendingText();
        }
        Configs.saveToFile();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(getFont(), title, width / 2, 12, 0xFFFFFFFF);
    }

    @Override
    public void extractMenuBackground(GuiGraphicsExtractor graphics) {
        super.extractMenuBackground(graphics);
        graphics.fill(contentX - 4, contentY - 4, contentX + contentWidth + 4, contentY + contentHeight + 4, 0x66000000);
        graphics.fill(contentX + categoryWidth + 1, contentY, contentX + categoryWidth + 2, contentY + contentHeight, 0x88707070);
    }

    @Override
    public Font getFont() {
        return super.getFont();
    }
}
