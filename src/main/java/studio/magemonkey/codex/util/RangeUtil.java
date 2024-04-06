package studio.magemonkey.codex.util;

import org.apache.commons.lang3.DoubleRange;
import org.apache.commons.lang3.Validate;

import java.util.Random;

public class RangeUtil {

    private static final Random rand = new Random();

    public static double getRandomDouble(DoubleRange range) {
        double min = range.getMinimum(), max = range.getMaximum();
        if (Double.compare(min, max) == 0) {
            return max;
        }
        Validate.isTrue(max > min, "Max can't be smaller than min!");
        return (rand.nextDouble() * (max - min)) + min;
    }

    public static boolean getChance(double chance) {
        return (chance > 0) && ((chance >= 100) || (chance >= getRandomDouble(DoubleRange.of(0d, 100d))));
    }

}
