package de.fanta.cubeside.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DuplicateMessageFormatterTest {
    @Test
    void formatsValidCustomValue() {
        DuplicateMessageFormatter.Result result = DuplicateMessageFormatter.format(" [%s messages]", 4);

        assertEquals(" [4 messages]", result.text());
        assertFalse(result.usedFallback());
    }

    @Test
    void malformedFormatFallsBackWithoutDroppingTheMessage() {
        DuplicateMessageFormatter.Result result = DuplicateMessageFormatter.format("%q", 3);

        assertEquals(" (3x)", result.text());
        assertTrue(result.usedFallback());
    }
}
