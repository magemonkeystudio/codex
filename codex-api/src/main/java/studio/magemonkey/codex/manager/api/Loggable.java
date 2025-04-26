package studio.magemonkey.codex.manager.api;

import org.jetbrains.annotations.NotNull;

public interface Loggable {
    void info(@NotNull String msg);

    void warn(@NotNull String msg);

    void error(@NotNull String msg);
}
