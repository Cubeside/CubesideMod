package de.fanta.cubeside.config.gui;

import de.fanta.cubeside.config.option.ArgbColor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

final class ColorListDraft {
    private final boolean listMode;
    private final List<ArgbColor> defaults;
    private final List<ArgbColor> colors;
    private int selectedIndex;
    private int scrollOffset;
    private int visibleRows = 1;

    static ColorListDraft single(ArgbColor initial, ArgbColor defaultColor) {
        return new ColorListDraft(false, List.of(initial), List.of(defaultColor));
    }

    static ColorListDraft list(List<ArgbColor> initial, List<ArgbColor> defaults) {
        return new ColorListDraft(true, initial, defaults);
    }

    private ColorListDraft(boolean listMode, List<ArgbColor> initial, List<ArgbColor> defaults) {
        this.listMode = listMode;
        this.colors = opaqueCopy(initial);
        this.defaults = List.copyOf(opaqueCopy(defaults));
        selectedIndex = colors.isEmpty() ? -1 : 0;
    }

    boolean listMode() {
        return listMode;
    }

    List<ArgbColor> colors() {
        return List.copyOf(colors);
    }

    Optional<ArgbColor> selectedColor() {
        return selectedIndex >= 0 ? Optional.of(colors.get(selectedIndex)) : Optional.empty();
    }

    int selectedIndex() {
        return selectedIndex;
    }

    int scrollOffset() {
        return scrollOffset;
    }

    int visibleRows() {
        return visibleRows;
    }

    void setVisibleRows(int rows) {
        visibleRows = Math.max(1, rows);
        clampScrollOffset();
    }

    void select(int index) {
        if (index < 0 || index >= colors.size()) {
            return;
        }
        selectedIndex = index;
        ensureSelectedVisible();
    }

    void setSelectedColor(ArgbColor color) {
        if (selectedIndex >= 0) {
            colors.set(selectedIndex, color.opaque());
        }
    }

    void addColor() {
        if (!listMode) {
            return;
        }
        colors.add(ArgbColor.fromColor(0xFFFFFFFF));
        selectedIndex = colors.size() - 1;
        ensureSelectedVisible();
    }

    void remove(int index) {
        if (!listMode || index < 0 || index >= colors.size()) {
            return;
        }
        colors.remove(index);
        if (colors.isEmpty()) {
            selectedIndex = -1;
        } else if (selectedIndex == index) {
            selectedIndex = Math.min(index, colors.size() - 1);
        } else if (selectedIndex > index) {
            selectedIndex--;
        }
        clampScrollOffset();
        ensureSelectedVisible();
    }

    void move(int index, int direction) {
        if (!listMode) {
            return;
        }
        int target = index + direction;
        if (index < 0 || index >= colors.size() || target < 0 || target >= colors.size()) {
            return;
        }
        Collections.swap(colors, index, target);
        if (selectedIndex == index) {
            selectedIndex = target;
        } else if (selectedIndex == target) {
            selectedIndex = index;
        }
        ensureSelectedVisible();
    }

    void reset() {
        colors.clear();
        colors.addAll(defaults);
        selectedIndex = colors.isEmpty() ? -1 : 0;
        scrollOffset = 0;
    }

    void scrollRows(int rows) {
        scrollOffset += rows;
        clampScrollOffset();
    }

    void setScrollOffset(int offset) {
        scrollOffset = offset;
        clampScrollOffset();
    }

    private void ensureSelectedVisible() {
        if (selectedIndex < 0) {
            scrollOffset = 0;
        } else if (selectedIndex < scrollOffset) {
            scrollOffset = selectedIndex;
        } else if (selectedIndex >= scrollOffset + visibleRows) {
            scrollOffset = selectedIndex - visibleRows + 1;
        }
        clampScrollOffset();
    }

    private void clampScrollOffset() {
        scrollOffset = Math.clamp(scrollOffset, 0, Math.max(0, colors.size() - visibleRows));
    }

    private static List<ArgbColor> opaqueCopy(List<ArgbColor> source) {
        return source.stream().map(ArgbColor::opaque).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
