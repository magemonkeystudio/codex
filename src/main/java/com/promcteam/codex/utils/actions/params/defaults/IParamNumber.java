package com.promcteam.codex.utils.actions.params.defaults;

import com.promcteam.codex.utils.actions.params.IParam;
import com.promcteam.codex.utils.actions.params.parser.IParamParser;
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
