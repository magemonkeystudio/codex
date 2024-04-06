package studio.magemonkey.codex.manager.api;

import studio.magemonkey.codex.manager.api.gui.NGUI;
import org.jetbrains.annotations.NotNull;

public interface Editable {

    @NotNull
    public NGUI<?> getEditor();
}
