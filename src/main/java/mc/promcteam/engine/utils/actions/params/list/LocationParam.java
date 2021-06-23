package mc.promcteam.engine.utils.actions.params.list;

import mc.promcteam.engine.utils.actions.params.IParamType;
import mc.promcteam.engine.utils.actions.params.defaults.IParamString;

public class LocationParam extends IParamString {

	public LocationParam() {
		super(IParamType.LOCATION, "location");
	}
}
