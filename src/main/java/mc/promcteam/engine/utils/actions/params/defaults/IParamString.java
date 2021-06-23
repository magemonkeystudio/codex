package mc.promcteam.engine.utils.actions.params.defaults;

import mc.promcteam.engine.utils.actions.params.IParam;
import mc.promcteam.engine.utils.actions.params.parser.IParamParser;
import org.jetbrains.annotations.NotNull;

public class IParamString extends IParam {

	public IParamString(@NotNull String key, @NotNull String flag) {
		super(key, flag);
	}

	@Override
	@NotNull
	public final IParamParser getParser() {
		return IParamParser.STRING;
	}
}
