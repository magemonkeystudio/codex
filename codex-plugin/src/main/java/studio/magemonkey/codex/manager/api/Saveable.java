package studio.magemonkey.codex.manager.api;

import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.config.api.JYML;

public interface Saveable {
    void save(@NotNull JYML cfg);
}
