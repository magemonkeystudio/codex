package mc.promcteam.engine.utils.actions.params.defaults;

import mc.promcteam.engine.utils.actions.params.parser.IParamParser;
import org.jetbrains.annotations.NotNull;

import mc.promcteam.engine.utils.actions.params.IParam;

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
