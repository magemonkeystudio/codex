package studio.magemonkey.codex.util.actions.params.parser;

import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.util.StringUT;
import studio.magemonkey.codex.util.actions.params.IParamValue;

public class ParamStringParser implements IParamParser {

    @Override
    @NotNull
    public IParamValue parseValue(@NotNull String str) {
        return new IParamValue(StringUT.color(str));
    }
}
