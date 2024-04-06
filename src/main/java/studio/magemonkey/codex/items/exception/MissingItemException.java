package studio.magemonkey.codex.items.exception;

import studio.magemonkey.codex.items.exception.CodexItemException;

public class MissingItemException extends CodexItemException {
    public MissingItemException(String message) {
        super(message);
    }
}
