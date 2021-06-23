package mc.promcteam.engine.manager.api;

import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.config.api.JYML;

public interface Saveable {

	public void save(@NotNull JYML cfg);
}
