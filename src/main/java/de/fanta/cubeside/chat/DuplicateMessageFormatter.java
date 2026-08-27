package de.fanta.cubeside.chat;

import java.util.IllegalFormatException;

public final class DuplicateMessageFormatter {
    public record Result(String text, IllegalFormatException error) {
        public boolean usedFallback() {
            return error != null;
        }
    }

    private DuplicateMessageFormatter() {
    }

    public static Result format(String format, int count) {
        try {
            return new Result(String.format(format, count), null);
        } catch (IllegalFormatException exception) {
            return new Result(" (" + count + "x)", exception);
        }
    }
}
