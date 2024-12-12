package studio.magemonkey.codex.util.actions.params.defaults;

import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.util.actions.params.IParam;
import studio.magemonkey.codex.util.actions.params.parser.IParamParser;

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
