package mc.promcteam.engine.utils.actions.params.parser;

import mc.promcteam.engine.utils.actions.params.IParamValue;
import org.jetbrains.annotations.NotNull;

public class ParamBooleanParser implements IParamParser {

	@Override
	@NotNull
	public IParamValue parseValue(@NotNull String str) {
		boolean b = Boolean.valueOf(str);
		return new IParamValue(b);
	}
}
