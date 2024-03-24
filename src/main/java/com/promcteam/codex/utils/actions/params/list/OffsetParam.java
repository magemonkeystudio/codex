package com.promcteam.codex.utils.actions.params.list;

import com.promcteam.codex.utils.StringUT;
import com.promcteam.codex.utils.actions.params.IParam;
import com.promcteam.codex.utils.actions.params.IParamType;
import com.promcteam.codex.utils.actions.params.IParamValue;
import com.promcteam.codex.utils.actions.params.parser.IParamParser;
import org.jetbrains.annotations.NotNull;

public class OffsetParam extends IParam {

    private final IParamParser parser;

    public OffsetParam() {
        super(IParamType.OFFSET, "offset");

        this.parser = (str) -> {
            String[] split = str.replace(" ", "").split(",");

            double x = 0;
            x = StringUT.getDouble(split[0], 0, true);

            double y = 0;
            if (split.length >= 2) {
                y = StringUT.getDouble(split[1], 0, true);
            }

            double z = 0;
            if (split.length == 3) {
                z = StringUT.getDouble(split[2], 0, true);
            }

            double[] arr = new double[]{x, y, z};

            return new IParamValue(arr);
        };
    }

    @Override
    @NotNull
    public IParamParser getParser() {
        return this.parser;
    }
}
