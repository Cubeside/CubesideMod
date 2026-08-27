package de.fanta.cubeside.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.junit.jupiter.api.Test;

class DuplicateMessageTrackerTest {
    @Test
    void replacesOneLogicalMessageAndAllOfItsRenderedLines() {
        DuplicateMessageTracker tracker = new DuplicateMessageTracker();
        DuplicateMessageTracker.Key key = key(Component.literal("repeat"), GuiMessageSource.SYSTEM_SERVER, null);
        GuiMessage previous = message("repeat", GuiMessageSource.SYSTEM_SERVER, null);
        GuiMessage older = message("older", GuiMessageSource.SYSTEM_SERVER, null);
        tracker.commit(new DuplicateMessageTracker.Decision(key, 1, false, false, 0), previous, true);

        List<GuiMessage> allMessages = new ArrayList<>(List.of(previous, older));
        List<GuiMessage.Line> trimmedMessages = new ArrayList<>(List.of(line(previous, false), line(previous, true), line(older, true)));

        DuplicateMessageTracker.Decision decision = tracker.prepareVisible(key, allMessages, trimmedMessages);

        assertTrue(decision.duplicate());
        assertTrue(decision.previousPersisted());
        assertEquals(2, decision.count());
        assertEquals(2, decision.removedLineCount());
        assertEquals(List.of(older), allMessages);
        assertEquals(1, trimmedMessages.size());
        assertSame(older, trimmedMessages.getFirst().parent());

        GuiMessage replacement = message("repeat (2x)", GuiMessageSource.SYSTEM_SERVER, null);
        allMessages.addFirst(replacement);
        trimmedMessages.addFirst(line(replacement, true));
        tracker.commit(decision, replacement, true);
        DuplicateMessageTracker.Decision thirdMessage = tracker.prepareVisible(key, allMessages, trimmedMessages);
        assertTrue(thirdMessage.duplicate());
        assertEquals(3, thirdMessage.count());
        assertEquals(List.of(older), allMessages);
    }

    @Test
    void onlyConsecutiveVisibleMessagesAreCombined() {
        DuplicateMessageTracker tracker = new DuplicateMessageTracker();
        DuplicateMessageTracker.Key aKey = key(Component.literal("A"), GuiMessageSource.SYSTEM_SERVER, null);
        DuplicateMessageTracker.Key bKey = key(Component.literal("B"), GuiMessageSource.SYSTEM_SERVER, null);
        GuiMessage a = message("A", GuiMessageSource.SYSTEM_SERVER, null);
        tracker.commit(new DuplicateMessageTracker.Decision(aKey, 1, false, false, 0), a, false);

        List<GuiMessage> allMessages = new ArrayList<>(List.of(a));
        List<GuiMessage.Line> trimmedMessages = new ArrayList<>(List.of(line(a, true)));
        DuplicateMessageTracker.Decision bDecision = tracker.prepareVisible(bKey, allMessages, trimmedMessages);
        assertFalse(bDecision.duplicate());

        GuiMessage b = message("B", GuiMessageSource.SYSTEM_SERVER, null);
        allMessages.addFirst(b);
        trimmedMessages.addFirst(line(b, true));
        tracker.commit(bDecision, b, false);

        DuplicateMessageTracker.Decision nextA = tracker.prepareVisible(aKey, allMessages, trimmedMessages);
        assertFalse(nextA.duplicate());
        assertEquals(1, nextA.count());
    }

    @Test
    void anUncommittedFilteredMessageDoesNotBreakTheVisibleSequence() {
        DuplicateMessageTracker tracker = new DuplicateMessageTracker();
        DuplicateMessageTracker.Key aKey = key(Component.literal("A"), GuiMessageSource.SYSTEM_SERVER, null);
        GuiMessage visibleA = message("A", GuiMessageSource.SYSTEM_SERVER, null);
        tracker.commit(new DuplicateMessageTracker.Decision(aKey, 1, false, false, 0), visibleA, false);

        List<GuiMessage> allMessages = new ArrayList<>(List.of(visibleA));
        List<GuiMessage.Line> trimmedMessages = new ArrayList<>(List.of(line(visibleA, true)));
        // A filtered message never reaches prepareVisible/commit.
        DuplicateMessageTracker.Decision nextVisibleA = tracker.prepareVisible(aKey, allMessages, trimmedMessages);

        assertTrue(nextVisibleA.duplicate());
        assertEquals(2, nextVisibleA.count());
    }

    @Test
    void staleStateNeverDeletesADifferentNewestMessage() {
        DuplicateMessageTracker tracker = new DuplicateMessageTracker();
        DuplicateMessageTracker.Key aKey = key(Component.literal("A"), GuiMessageSource.SYSTEM_SERVER, null);
        GuiMessage staleA = message("A", GuiMessageSource.SYSTEM_SERVER, null);
        GuiMessage newestB = message("B", GuiMessageSource.SYSTEM_SERVER, null);
        tracker.commit(new DuplicateMessageTracker.Decision(aKey, 1, false, false, 0), staleA, true);

        List<GuiMessage> allMessages = new ArrayList<>(List.of(newestB, staleA));
        List<GuiMessage.Line> trimmedMessages = new ArrayList<>(List.of(line(newestB, true), line(staleA, true)));
        DuplicateMessageTracker.Decision decision = tracker.prepareVisible(aKey, allMessages, trimmedMessages);

        assertFalse(decision.duplicate());
        assertEquals(List.of(newestB, staleA), allMessages);
        assertEquals(2, trimmedMessages.size());
    }

    @Test
    void formattingSourceAndTagArePartOfTheDuplicateKey() {
        Component plain = Component.literal("same");
        Component clickable = Component.literal("same").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/test")));
        GuiMessageTag firstTag = new GuiMessageTag(1, null, Component.literal("first"), "first");
        GuiMessageTag secondTag = new GuiMessageTag(1, null, Component.literal("second"), "second");

        assertFalse(key(plain, GuiMessageSource.SYSTEM_SERVER, firstTag).equals(key(clickable, GuiMessageSource.SYSTEM_SERVER, firstTag)));
        assertFalse(key(plain, GuiMessageSource.SYSTEM_SERVER, firstTag).equals(key(plain, GuiMessageSource.SYSTEM_CLIENT, firstTag)));
        assertFalse(key(plain, GuiMessageSource.SYSTEM_SERVER, firstTag).equals(key(plain, GuiMessageSource.SYSTEM_SERVER, secondTag)));
    }

    @Test
    void resetAndPersistenceStatusStartANewSequenceCleanly() {
        DuplicateMessageTracker tracker = new DuplicateMessageTracker();
        DuplicateMessageTracker.Key key = key(Component.literal("A"), GuiMessageSource.SYSTEM_SERVER, null);
        GuiMessage first = message("A", GuiMessageSource.SYSTEM_SERVER, null);
        tracker.commit(new DuplicateMessageTracker.Decision(key, 1, false, false, 0), first, false);

        List<GuiMessage> allMessages = new ArrayList<>(List.of(first));
        List<GuiMessage.Line> trimmedMessages = new ArrayList<>(List.of(line(first, true)));
        DuplicateMessageTracker.Decision unsavedReplacement = tracker.prepareVisible(key, allMessages, trimmedMessages);
        assertFalse(unsavedReplacement.previousPersisted());

        GuiMessage second = message("A (2x)", GuiMessageSource.SYSTEM_SERVER, null);
        allMessages.addFirst(second);
        trimmedMessages.addFirst(line(second, true));
        tracker.commit(unsavedReplacement, second, true);
        DuplicateMessageTracker.Decision savedReplacement = tracker.prepareVisible(key, allMessages, trimmedMessages);
        assertTrue(savedReplacement.previousPersisted());

        tracker.reset();
        DuplicateMessageTracker.Decision afterReset = tracker.prepareVisible(key, allMessages, trimmedMessages);
        assertFalse(afterReset.duplicate());
        assertEquals(1, afterReset.count());
    }

    @Test
    void persistedAncestorSurvivesTemporarilyDisabledSaving() {
        DuplicateMessageTracker.Key key = key(Component.literal("A"), GuiMessageSource.SYSTEM_SERVER, null);
        DuplicateMessageTracker.Decision persistedPrevious = new DuplicateMessageTracker.Decision(key, 2, true, true, 1);
        DuplicateMessageTracker.Decision neverPersistedPrevious = new DuplicateMessageTracker.Decision(key, 2, true, false, 1);

        assertTrue(DuplicateMessageTracker.sequencePersisted(persistedPrevious, false));
        assertFalse(DuplicateMessageTracker.sequencePersisted(neverPersistedPrevious, false));
        assertTrue(DuplicateMessageTracker.sequencePersisted(neverPersistedPrevious, true));
    }

    private static DuplicateMessageTracker.Key key(Component component, GuiMessageSource source, GuiMessageTag tag) {
        return new DuplicateMessageTracker.Key(component, source, tag);
    }

    private static GuiMessage message(String content, GuiMessageSource source, GuiMessageTag tag) {
        return new GuiMessage(0, Component.literal(content), null, source, tag);
    }

    private static GuiMessage.Line line(GuiMessage parent, boolean endOfEntry) {
        return new GuiMessage.Line(parent, FormattedCharSequence.forward(parent.content().getString(), Style.EMPTY), endOfEntry);
    }
}
