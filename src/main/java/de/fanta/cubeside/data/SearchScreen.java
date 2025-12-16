package de.fanta.cubeside.data;

import de.fanta.cubeside.CubesideClientFabric;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.lwjgl.glfw.GLFW;

public class SearchScreen extends Screen {
    private final Screen parent;
    private EditBox searchField;
    private final List<Component> allEntries;
    private final List<Component> filteredEntries;
    private int scrollOffset = 0;
    private static final int ENTRIES_PER_PAGE = 15;
    private static final int ENTRY_HEIGHT = 20;
    private static final int PADDING = 5;
    private static final int TEXT_MARGIN = 10;
    private static final Collection<Button> buttonCache = new ArrayList<>();

    public SearchScreen(Screen parent, RegistryAccess manager) {
        super(Component.literal("Chat Log (" + CubesideClientFabric.getChatDatabase().getServer() + ")"));
        this.parent = parent;
        allEntries = CubesideClientFabric.getChatDatabase().loadMessages();
        filteredEntries = new ArrayList<>(allEntries);
    }

    @Override
    protected void init() {
        searchField = new EditBox(font, 10, 35, 300, 20, Component.literal("Suche..."));
        this.searchField.setCanLoseFocus(false);
        this.searchField.setTextColor(-1);
        this.searchField.setTextColorUneditable(-1);
        this.searchField.setMaxLength(100);
        this.searchField.setValue("");
        addRenderableWidget(searchField);

        addRenderableWidget(Button.builder(Component.literal("Suchen"), button -> {
            String keyword = searchField.getValue();
            filterEntries(keyword);
        }).bounds(320, 35, 100, 20).build());

        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> this.minecraft.setScreen(this.parent)).bounds(width / 2 - 50, this.height - 28, 150, 20).build());

        this.addRenderableWidget(new StringWidget(0, 10, this.width, 9, this.title, this.font));
        resetScrollOffset();
    }

    private void filterEntries(String keyword) {
        this.searchField.setFocused(false);
        this.searchField.setCanLoseFocus(false);
        this.filteredEntries.clear();
        for (Component entry : this.allEntries) {
            if (entry.getString().toLowerCase().contains(keyword.toLowerCase())) {
                this.filteredEntries.add(entry);
            }
        }
        this.scrollOffset = filteredEntries.size();
        resetScrollOffset();
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderBlurredBackground(context);
        this.renderMenuBackground(context);

        buttonCache.forEach(this::removeWidget);
        buttonCache.clear();

        int startY = 60;
        int availableWidth = this.width - TEXT_MARGIN - 75;

        for (int i = scrollOffset; i < Math.min(scrollOffset + ENTRIES_PER_PAGE, filteredEntries.size()); i++) {
            int y = startY + (i - scrollOffset) * (ENTRY_HEIGHT + PADDING);
            if (y > this.height - 60) {
                break;
            }

            int entryBackgroundColor = (i % 2 == 0) ? new Color(128, 128, 128, 100).getRGB() : new Color(166, 166, 166, 100).getRGB();
            context.fill(TEXT_MARGIN, y, this.width - TEXT_MARGIN - 65, y + ENTRY_HEIGHT, entryBackgroundColor);

            Component entry = filteredEntries.get(i);
            List<FormattedCharSequence> lines = font.split(entry, availableWidth);

            int textY = y + 5;
            for (FormattedCharSequence line : lines) {
                context.drawString(font, line, TEXT_MARGIN + 5, textY, Color.white.getRGB(), true);
                textY += this.font.lineHeight;
            }

            Button buttonWidget = Button.builder(Component.literal("Copy"), button -> Minecraft.getInstance().keyboardHandler.setClipboard(entry.getString())).bounds(this.width - 70, y, 60, 20).build();
            addRenderableWidget(buttonWidget);
            buttonCache.add(buttonWidget);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float delta) {
        // NOTHING :>
    }

    @Override
    public void tick() {
        super.tick();
    }

    private void resetScrollOffset() {
        int number = filteredEntries.size() - ENTRIES_PER_PAGE;
        if (number < 0) {
            number = 0;
        }
        scrollOffset = number;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int i = scrollOffset;
        if (verticalAmount > 0) {
            i = Math.max(0, i - 1);
        } else if (verticalAmount < 0) {
            i = Math.min(filteredEntries.size() - ENTRIES_PER_PAGE, i + 1);
        }
        if (i < 0) {
            i = 0;
        }
        scrollOffset = i;
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        int keyCode = input.input();
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && this.searchField.isFocused()) {
            filterEntries(this.searchField.getValue());
        }
        return super.keyPressed(input);
    }
}
