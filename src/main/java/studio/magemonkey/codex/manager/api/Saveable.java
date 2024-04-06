package studio.magemonkey.codex.manager.api;

import studio.magemonkey.codex.config.api.JYML;
import org.jetbrains.annotations.NotNull;

public interface Saveable {

    public void save(@NotNull JYML cfg);
}
