package com.promcteam.codex.utils.actions.api;

import com.promcteam.codex.utils.actions.ActionManipulator;
import org.jetbrains.annotations.NotNull;

public interface IActioned {

    @NotNull
    public ActionManipulator getActions();
}
