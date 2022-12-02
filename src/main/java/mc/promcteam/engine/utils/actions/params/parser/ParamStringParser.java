package mc.promcteam.engine.utils.actions.params.parser;

import mc.promcteam.engine.utils.StringUT;
import mc.promcteam.engine.utils.actions.params.IParamValue;
import org.jetbrains.annotations.NotNull;

public class ParamStringParser implements IParamParser {

    @Override
    @NotNull
    public IParamValue parseValue(@NotNull String str) {
        return new IParamValue(StringUT.color(str));
    }
}
