package com.promcteam.codex.util.actions.params.parser;

import com.promcteam.codex.util.StringUT;
import com.promcteam.codex.util.actions.params.IParamValue;
import org.jetbrains.annotations.NotNull;

public class ParamNumberParser implements IParamParser {

    @Override
    @NotNull
    public IParamValue parseValue(@NotNull String str) {
        boolean               perc = str.contains("%");
        IParamValue.IOperator oper = IParamValue.IOperator.parse(str);

        str = IParamValue.IOperator.clean(str);
        double amount = StringUT.getDouble(str.replace("%", ""), 0D, true);

        IParamValue val = new IParamValue((int) amount);
        val.setBoolean(perc);
        val.setDouble(amount);
        val.setOperator(oper);
        if (perc) val.setRaw(val.getRaw() + "%");

        return val;
    }
}
