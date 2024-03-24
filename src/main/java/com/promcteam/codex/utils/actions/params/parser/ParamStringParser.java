package com.promcteam.codex.utils.actions.params.parser;

import com.promcteam.codex.utils.StringUT;
import com.promcteam.codex.utils.actions.params.IParamValue;
import org.jetbrains.annotations.NotNull;

public class ParamStringParser implements IParamParser {

    @Override
    @NotNull
    public IParamValue parseValue(@NotNull String str) {
        return new IParamValue(StringUT.color(str));
    }
}
