package de.fanta.cubeside.config.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.fanta.cubeside.config.option.ArgbColor;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ColorListDraftTest {
    private static final ArgbColor RED = ArgbColor.fromColor(0xFFFF0000);
    private static final ArgbColor GREEN = ArgbColor.fromColor(0xFF00FF00);
    private static final ArgbColor BLUE = ArgbColor.fromColor(0xFF0000FF);

    @Test
    void editingADraftDoesNotMutateTheOriginalList() {
        List<ArgbColor> original = new ArrayList<>(List.of(RED, GREEN));
        ColorListDraft draft = ColorListDraft.list(original, List.of(BLUE));

        draft.setSelectedColor(BLUE);
        draft.addColor();

        assertEquals(List.of(RED, GREEN), original);
        assertEquals(List.of(BLUE, GREEN, ArgbColor.fromColor(0xFFFFFFFF)), draft.colors());
    }

    @Test
    void addSelectsTheNewColorAndScrollsItIntoView() {
        ColorListDraft draft = ColorListDraft.list(List.of(RED, GREEN), List.of());
        draft.setVisibleRows(2);

        draft.addColor();

        assertEquals(2, draft.selectedIndex());
        assertEquals(1, draft.scrollOffset());
        assertEquals(ArgbColor.fromColor(0xFFFFFFFF), draft.selectedColor().orElseThrow());
    }

    @Test
    void movingRowsKeepsTheSameLogicalSelection() {
        ColorListDraft draft = ColorListDraft.list(List.of(RED, GREEN, BLUE), List.of());
        draft.select(1);

        draft.move(0, 1);

        assertEquals(List.of(GREEN, RED, BLUE), draft.colors());
        assertEquals(0, draft.selectedIndex());
        assertEquals(GREEN, draft.selectedColor().orElseThrow());
    }

    @Test
    void deletingTheSelectionSelectsTheNearestRemainingColor() {
        ColorListDraft draft = ColorListDraft.list(List.of(RED, GREEN, BLUE), List.of());
        draft.select(1);

        draft.remove(1);
        assertEquals(1, draft.selectedIndex());
        assertEquals(BLUE, draft.selectedColor().orElseThrow());

        draft.remove(1);
        draft.remove(0);
        assertEquals(-1, draft.selectedIndex());
        assertTrue(draft.selectedColor().isEmpty());
    }

    @Test
    void scrollingIsClampedAfterListChanges() {
        ColorListDraft draft = ColorListDraft.list(List.of(RED, GREEN, BLUE), List.of());
        draft.setVisibleRows(2);

        draft.setScrollOffset(100);
        assertEquals(1, draft.scrollOffset());

        draft.remove(2);
        assertEquals(0, draft.scrollOffset());
    }

    @Test
    void resetRestoresSingleColorAndCompleteListDefaults() {
        ColorListDraft single = ColorListDraft.single(RED, BLUE);
        single.setSelectedColor(GREEN);
        single.reset();
        assertEquals(List.of(BLUE), single.colors());

        ColorListDraft list = ColorListDraft.list(List.of(RED), List.of(GREEN, BLUE));
        list.addColor();
        list.reset();
        assertEquals(List.of(GREEN, BLUE), list.colors());
        assertEquals(0, list.selectedIndex());
        assertEquals(0, list.scrollOffset());
    }
}
