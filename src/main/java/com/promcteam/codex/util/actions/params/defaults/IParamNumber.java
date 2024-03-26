package com.promcteam.codex.util.actions.params.defaults;

import com.promcteam.codex.util.actions.params.IParam;
import com.promcteam.codex.util.actions.params.parser.IParamParser;
import org.jetbrains.annotations.NotNull;

public class IParamNumber extends IParam {

    public IParamNumber(@NotNull String key, @NotNull String flag) {
        super(key, flag);
    }

    @Override
    @NotNull
    public final IParamParser getParser() {
        return IParamParser.NUMBER;
    }
}
