package com.promcteam.risecore.util;

import java.util.ArrayList;
import java.util.Set;

public class RiseStringUtils {

    public static String[] splitAround(String baseString, String split) {
        if (!baseString.contains(split))
            return new String[]{baseString};

        return splitAround(new String[]{baseString}, split);
    }

    public static String[] splitAround(String[] base, String split) {
        ArrayList<String> strs = new ArrayList<>();
        for (String section : base) {
            if (!section.contains(split) || section.equals(split)) {
                strs.add(section);
                continue;
            }

            String before = section.substring(0, section.indexOf(split));
            String after  = section.substring(section.indexOf(split) + split.length());
            strs.add(before);
            strs.add(split);
            strs.add(after);
        }

        if (base.length == strs.size())
            return strs.toArray(new String[0]);
        else {
            return splitAround(strs.toArray(new String[0]), split);
        }
    }

    public static String[] splitAround(String base, Set<String> set) {
        if (set.size() == 0)
            return new String[]{base};

        String[] temp = new String[]{base};
        for (String s : set) {
            temp = splitAround(temp, s);
        }

        return temp;
    }

}
