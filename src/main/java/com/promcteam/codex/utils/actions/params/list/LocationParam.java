package com.promcteam.codex.utils.actions.params.list;

import com.promcteam.codex.utils.actions.params.IParamType;
import com.promcteam.codex.utils.actions.params.defaults.IParamString;

public class LocationParam extends IParamString {

    public LocationParam() {
        super(IParamType.LOCATION, "location");
    }
}
