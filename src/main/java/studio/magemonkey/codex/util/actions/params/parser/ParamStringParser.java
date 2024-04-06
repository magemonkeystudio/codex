package studio.magemonkey.codex.util.actions.params.parser;

import studio.magemonkey.codex.util.StringUT;
import studio.magemonkey.codex.util.actions.params.IParamValue;
import org.jetbrains.annotations.NotNull;

public class ParamStringParser implements IParamParser {

    @Override
    @NotNull
    public IParamValue parseValue(@NotNull String str) {
        return new IParamValue(StringUT.color(str));
    }
}
