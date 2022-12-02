package mc.promcteam.engine.utils.actions.api;

import mc.promcteam.engine.utils.actions.ActionManipulator;
import org.jetbrains.annotations.NotNull;

public interface IActioned {

    @NotNull
    public ActionManipulator getActions();
}
