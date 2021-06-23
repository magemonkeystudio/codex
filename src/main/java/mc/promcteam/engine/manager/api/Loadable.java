package mc.promcteam.engine.manager.api;

public interface Loadable {

	void setup();
	
	void shutdown();
	
	public default void reload() {
		this.shutdown();
		this.setup();
	}
}
