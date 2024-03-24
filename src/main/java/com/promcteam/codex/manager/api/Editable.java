package com.promcteam.codex.manager.api;

import com.promcteam.codex.manager.api.gui.NGUI;
import org.jetbrains.annotations.NotNull;

public interface Editable {

    @NotNull
    public NGUI<?> getEditor();
}
