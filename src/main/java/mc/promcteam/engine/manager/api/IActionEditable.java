package mc.promcteam.engine.manager.api;

import mc.promcteam.engine.NexPlugin;
import mc.promcteam.engine.manager.editor.object.IEditorActionsMain;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.utils.actions.api.IActioned;

public interface IActionEditable extends IActioned, Editable {

	@NotNull
	public IEditorActionsMain<? extends NexPlugin<?>> getEditorActions();
	
	@NotNull
	public String getActionsPath();
}
