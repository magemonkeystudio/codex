package studio.magemonkey.codex.items.exception;

import studio.magemonkey.codex.items.providers.ICodexItemProvider;

/**
 * Thrown when an attempting to use an {@link ICodexItemProvider}.
 * If a provider is missing, or the plugin it hooks into is not enabled, a {@link MissingProviderException} is thrown.
 * If an items is missing from a provider, a {@link MissingItemException} is thrown.
 */
public class CodexItemException extends Exception {
    public CodexItemException(String message) {
        super(message);
    }
}
