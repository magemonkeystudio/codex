package studio.magemonkey.codex.util.actions.api;

import studio.magemonkey.codex.util.actions.ActionManipulator;
import org.jetbrains.annotations.NotNull;

public interface IActioned {

    @NotNull
    public ActionManipulator getActions();
}
