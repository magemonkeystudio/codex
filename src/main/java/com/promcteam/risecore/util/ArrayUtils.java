package com.promcteam.risecore.util;

import java.util.ArrayList;
import java.util.Collections;

public class ArrayUtils {

    public static <T> ArrayList<T> toArray(T... objs) {
        ArrayList<T> list = new ArrayList<>();
        Collections.addAll(list, objs);
        return list;
    }

}
