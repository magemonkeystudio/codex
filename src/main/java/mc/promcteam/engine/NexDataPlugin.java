package mc.promcteam.engine;

import mc.promcteam.engine.data.IDataHandler;
import mc.promcteam.engine.data.users.IAbstractUser;
import mc.promcteam.engine.data.users.IUserManager;
import org.jetbrains.annotations.NotNull;

public abstract class NexDataPlugin<P extends NexDataPlugin<P, U>, U extends IAbstractUser<P>> extends NexPlugin<P> {

	protected IUserManager<P, U> userManager;
	
	protected abstract boolean setupDataHandlers();
	
	protected void shutdownDataHandlers() {
		if (this.userManager != null) this.userManager.shutdown();
		
		IDataHandler<P, U> dataHandler = this.getData();
		if (dataHandler != null) dataHandler.shutdown();
	}
	
	public abstract IDataHandler<P, U> getData();
	
	@NotNull
	public IUserManager<P, U> getUserManager() {
		return this.userManager;
	}
}
