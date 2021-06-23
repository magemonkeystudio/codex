package mc.promcteam.engine.data.event;

import mc.promcteam.engine.data.users.IAbstractUser;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.NexDataPlugin;

public class EngineUserCreatedEvent<P extends NexDataPlugin<P, U>, U extends IAbstractUser<P>> extends EngineUserEvent<P, U> {

	public EngineUserCreatedEvent(@NotNull P plugin, @NotNull U user) {
		super(plugin, user);
	}
}
