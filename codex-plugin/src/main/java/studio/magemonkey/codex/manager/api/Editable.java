package studio.magemonkey.codex.manager.api;

import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.manager.api.gui.NGUI;

public interface Editable {

    @NotNull
    public NGUI<?> getEditor();
}
