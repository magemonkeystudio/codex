package com.promcteam.codex.manager.api;

import com.promcteam.codex.config.api.JYML;
import org.jetbrains.annotations.NotNull;

public interface Saveable {

    public void save(@NotNull JYML cfg);
}
