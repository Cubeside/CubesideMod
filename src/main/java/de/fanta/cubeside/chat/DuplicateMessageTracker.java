package de.fanta.cubeside.chat;

import java.util.List;
import java.util.Objects;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.client.multiplayer.chat.GuiMessageSource;
import net.minecraft.client.multiplayer.chat.GuiMessageTag;
import net.minecraft.network.chat.Component;

public final class DuplicateMessageTracker {
    public record Key(Component content, GuiMessageSource source, GuiMessageTag tag) {
        public Key {
            Objects.requireNonNull(content, "content");
            Objects.requireNonNull(source, "source");
        }
    }

    public record Decision(Key key, int count, boolean duplicate, boolean previousPersisted, int removedLineCount) {
    }

    private Key lastKey;
    private GuiMessage lastMessage;
    private int count;
    private boolean lastMessagePersisted;

    public Decision prepareVisible(Key key, List<GuiMessage> allMessages, List<GuiMessage.Line> trimmedMessages) {
        boolean canReplace = lastKey != null
                && lastKey.equals(key)
                && lastMessage != null
                && !allMessages.isEmpty()
                && allMessages.getFirst() == lastMessage;
        if (!canReplace) {
            return new Decision(key, 1, false, false, 0);
        }

        allMessages.removeFirst();
        int previousLineCount = trimmedMessages.size();
        trimmedMessages.removeIf(line -> line.parent() == lastMessage);
        int removedLineCount = previousLineCount - trimmedMessages.size();
        return new Decision(key, count + 1, true, lastMessagePersisted, removedLineCount);
    }

    public void commit(Decision decision, GuiMessage message, boolean persisted) {
        lastKey = decision.key();
        lastMessage = message;
        count = decision.count();
        lastMessagePersisted = persisted;
    }

    public static boolean sequencePersisted(Decision decision, boolean persistenceSucceeded) {
        return persistenceSucceeded || decision.duplicate() && decision.previousPersisted();
    }

    public void reset() {
        lastKey = null;
        lastMessage = null;
        count = 0;
        lastMessagePersisted = false;
    }
}
