package mc.promcteam.engine.manager.api;

import mc.promcteam.engine.config.api.JYML;
import org.jetbrains.annotations.NotNull;

public interface Saveable {

    public void save(@NotNull JYML cfg);
}
