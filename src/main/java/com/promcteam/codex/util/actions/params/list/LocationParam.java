package com.promcteam.codex.util.actions.params.list;

import com.promcteam.codex.util.actions.params.IParamType;
import com.promcteam.codex.util.actions.params.defaults.IParamString;

public class LocationParam extends IParamString {

    public LocationParam() {
        super(IParamType.LOCATION, "location");
    }
}
