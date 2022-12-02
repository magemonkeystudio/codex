package mc.promcteam.engine.manager.api;

import mc.promcteam.engine.manager.api.gui.NGUI;
import org.jetbrains.annotations.NotNull;

public interface Editable {

    @NotNull
    public NGUI<?> getEditor();
}
