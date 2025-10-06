package de.fanta.cubeside.data;

import de.fanta.cubeside.CubesideClientFabric;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class SearchScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget searchField;
    private final List<Text> allEntries;
    private final List<Text> filteredEntries;
    private int scrollOffset = 0;
    private static final int ENTRIES_PER_PAGE = 15;
    private static final int ENTRY_HEIGHT = 20;
    private static final int PADDING = 5;
    private static final int TEXT_MARGIN = 10;
    private static final Collection<ButtonWidget> buttonCache = new ArrayList<>();

    public SearchScreen(Screen parent, DynamicRegistryManager manager) {
        super(Text.literal("Chat Log (" + CubesideClientFabric.getChatDatabase().getServer() + ")"));
        this.parent = parent;
        allEntries = CubesideClientFabric.getChatDatabase().loadMessages();
        filteredEntries = new ArrayList<>(allEntries);
    }

    @Override
    protected void init() {
        searchField = new TextFieldWidget(textRenderer, 10, 35, 300, 20, Text.literal("Suche..."));
        this.searchField.setFocusUnlocked(false);
        this.searchField.setEditableColor(-1);
        this.searchField.setUneditableColor(-1);
        this.searchField.setMaxLength(100);
        this.searchField.setText("");
        addDrawableChild(searchField);

        addDrawableChild(ButtonWidget.builder(Text.literal("Suchen"), button -> {
            String keyword = searchField.getText();
            filterEntries(keyword);
        }).dimensions(320, 35, 100, 20).build());

        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> this.client.setScreen(this.parent)).dimensions(width / 2 - 50, this.height - 28, 150, 20).build());

        this.addDrawableChild(new TextWidget(0, 10, this.width, 9, this.title, this.textRenderer));
        resetScrollOffset();
    }

    private void filterEntries(String keyword) {
        this.searchField.setFocused(false);
        this.searchField.setFocusUnlocked(false);
        this.filteredEntries.clear();
        for (Text entry : this.allEntries) {
            if (entry.getString().toLowerCase().contains(keyword.toLowerCase())) {
                this.filteredEntries.add(entry);
            }
        }
        this.scrollOffset = filteredEntries.size();
        resetScrollOffset();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // FIXME 1.21.6 this.applyBlur();
        this.renderDarkening(context);

        buttonCache.forEach(this::remove);
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

            Text entry = filteredEntries.get(i);
            List<OrderedText> lines = textRenderer.wrapLines(entry, availableWidth);

            int textY = y + 5;
            for (OrderedText line : lines) {
                context.drawText(textRenderer, line, TEXT_MARGIN + 5, textY, Color.white.getRGB(), true);
                textY += this.textRenderer.fontHeight;
            }

            ButtonWidget buttonWidget = ButtonWidget.builder(Text.literal("Copy"), button -> MinecraftClient.getInstance().keyboard.setClipboard(entry.getString())).dimensions(this.width - 70, y, 60, 20).build();
            addDrawableChild(buttonWidget);
            buttonCache.add(buttonWidget);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
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
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.getKeycode();
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) && this.searchField.isFocused()) {
            filterEntries(this.searchField.getText());
        }
        return super.keyPressed(input);
    }
}
