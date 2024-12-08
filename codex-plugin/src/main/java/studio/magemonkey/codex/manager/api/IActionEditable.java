package studio.magemonkey.codex.manager.api;

import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.manager.editor.object.IEditorActionsMain;
import studio.magemonkey.codex.util.actions.api.IActioned;

public interface IActionEditable extends IActioned, Editable {
    @NotNull
    public IEditorActionsMain<? extends CodexPlugin<?>> getEditorActions();

    @NotNull
    public String getActionsPath();
}
