package studio.magemonkey.codex.items.exception;

import studio.magemonkey.codex.items.exception.CodexItemException;

public class MissingProviderException extends CodexItemException {
    public MissingProviderException(String message) {
        super(message);
    }
}
