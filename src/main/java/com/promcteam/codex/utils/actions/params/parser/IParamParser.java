package com.promcteam.codex.utils.actions.params.parser;

import com.promcteam.codex.utils.actions.params.IParamValue;
import org.jetbrains.annotations.NotNull;

public interface IParamParser {

    public static final IParamParser BOOLEAN = new ParamBooleanParser();
    public static final IParamParser NUMBER  = new ParamNumberParser();
    public static final IParamParser STRING  = new ParamStringParser();

    @NotNull
    public IParamValue parseValue(@NotNull String str);
}
