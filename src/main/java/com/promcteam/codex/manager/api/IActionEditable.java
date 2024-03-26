package com.promcteam.codex.manager.api;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.manager.editor.object.IEditorActionsMain;
import com.promcteam.codex.util.actions.api.IActioned;
import org.jetbrains.annotations.NotNull;

public interface IActionEditable extends IActioned, Editable {

    @NotNull
    public IEditorActionsMain<? extends CodexPlugin<?>> getEditorActions();

    @NotNull
    public String getActionsPath();
}
