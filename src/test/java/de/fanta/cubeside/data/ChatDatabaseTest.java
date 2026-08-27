package de.fanta.cubeside.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ChatDatabaseTest {
    @TempDir
    File temporaryDirectory;

    @Test
    void replacementIsWrittenAtomicallyAndCompactedCompatibly() throws Exception {
        File databaseFile = new File(temporaryDirectory, "chat.dat");
        ChatDatabase database = new ChatDatabase("test", null, databaseFile);
        database.addMessageEntry("A");
        database.addMessageEntry("B");
        database.replaceNewestMessage("B (2x)");
        database.replaceNewestMessage("B (3x)");
        database.close();

        Journal firstJournal = readJournal(databaseFile);
        assertEquals(List.of(false, false, true, true), firstJournal.replaceFlags());
        assertEquals(List.of("A", "B (3x)"), firstJournal.messages());

        ChatDatabase reopened = new ChatDatabase("test", null, databaseFile);
        reopened.close();

        Journal compactedJournal = readJournal(databaseFile);
        assertEquals(List.of(false, false), compactedJournal.replaceFlags());
        assertEquals(List.of("A", "B (3x)"), compactedJournal.messages());
    }

    @Test
    void replacingAnEmptyDatabaseWritesANormalFirstEntry() throws Exception {
        File databaseFile = new File(temporaryDirectory, "empty.dat");
        ChatDatabase database = new ChatDatabase("test", null, databaseFile);
        database.replaceNewestMessage("first");
        database.close();

        Journal journal = readJournal(databaseFile);
        assertEquals(List.of(false), journal.replaceFlags());
        assertEquals(List.of("first"), journal.messages());
    }

    private static Journal readJournal(File file) throws Exception {
        List<Boolean> replaceFlags = new ArrayList<>();
        List<String> messages = new ArrayList<>();
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            while (true) {
                int type = input.readByte();
                assertEquals(0, type);
                boolean replacesNewest = input.readBoolean();
                replaceFlags.add(replacesNewest);
                if (replacesNewest) {
                    assertFalse(messages.isEmpty());
                    messages.removeLast();
                }
                messages.addLast(input.readUTF());
                assertTrue(input.readLong() > 0);
            }
        } catch (EOFException ignored) {
        }
        return new Journal(replaceFlags, messages);
    }

    private record Journal(List<Boolean> replaceFlags, List<String> messages) {
    }
}
