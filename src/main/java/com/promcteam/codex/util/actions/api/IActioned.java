package com.promcteam.codex.util.actions.api;

import com.promcteam.codex.util.actions.ActionManipulator;
import org.jetbrains.annotations.NotNull;

public interface IActioned {

    @NotNull
    public ActionManipulator getActions();
}
