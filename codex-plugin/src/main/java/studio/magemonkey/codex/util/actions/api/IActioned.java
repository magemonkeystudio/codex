package studio.magemonkey.codex.util.actions.api;

import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.util.actions.ActionManipulator;

public interface IActioned {

    @NotNull
    public ActionManipulator getActions();
}
