package mc.promcteam.engine.utils.actions.actions.list;

import java.util.List;
import java.util.Set;

import mc.promcteam.engine.NexPlugin;
import mc.promcteam.engine.utils.actions.actions.IActionExecutor;
import mc.promcteam.engine.utils.actions.actions.IActionType;
import mc.promcteam.engine.utils.actions.params.IParamResult;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.utils.actions.params.IParamType;

public class Action_Goto extends IActionExecutor {

	public Action_Goto(@NotNull NexPlugin<?> plugin) {
		super(plugin, IActionType.GOTO);
	}

	@Override
	@NotNull
	public List<String> getDescription() {
		return plugin.lang().Core_Editor_Actions_Action_Goto_Desc.asList();
	}
	
	@Override
	public void registerParams() {
		this.registerParam(IParamType.NAME);
	}

	@Override
	protected void execute(@NotNull Entity exe, @NotNull Set<Entity> targets, @NotNull IParamResult result) {
		
	}

	@Override
	public boolean mustHaveTarget() {
		return false;
	}
}
