package com.promcteam.codex.items.exception;

import com.promcteam.codex.items.providers.IProItemProvider;

/**
 * Thrown when an attempting to use an {@link IProItemProvider}.
 * If a provider is missing, or the plugin it hooks into is not enabled, a {@link MissingProviderException} is thrown.
 * If an items is missing from a provider, a {@link MissingItemException} is thrown.
 */
public class ProItemException extends Exception {
    public ProItemException(String message) {
        super(message);
    }
}
