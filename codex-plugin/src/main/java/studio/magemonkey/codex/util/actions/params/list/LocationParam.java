package studio.magemonkey.codex.util.actions.params.list;

import studio.magemonkey.codex.util.actions.params.IParamType;
import studio.magemonkey.codex.util.actions.params.defaults.IParamString;

public class LocationParam extends IParamString {

    public LocationParam() {
        super(IParamType.LOCATION, "location");
    }
}
