package mc.promcteam.engine.utils.actions.params.defaults;

import mc.promcteam.engine.utils.actions.params.IParam;
import mc.promcteam.engine.utils.actions.params.parser.IParamParser;
import org.jetbrains.annotations.NotNull;

public class IParamBoolean extends IParam {

    public IParamBoolean(@NotNull String key, @NotNull String flag) {
        super(key, flag);
    }

    @Override
    @NotNull
    public final IParamParser getParser() {
        return IParamParser.BOOLEAN;
    }
}
