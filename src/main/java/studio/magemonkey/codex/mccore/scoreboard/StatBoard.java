/**
 * MCCore
 * com.rit.sucy.scoreboard.StatBoard
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2024 Mage Monkey Studios
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package studio.magemonkey.codex.mccore.scoreboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Stat board for displaying various statistics
 */
public class StatBoard extends Board {
    private final List<StatHolder> holders = new ArrayList<>();
    private final List<String>     stats   = new ArrayList<>();

    /**
     * Constructor
     *
     * @param title scoreboard title
     */
    public StatBoard(String title, String plugin) {
        super(title, plugin);
    }

    /**
     * Add stats to the scoreboard
     *
     * @param holder holder of the stats
     */
    public void addStats(StatHolder holder) {
        holders.add(holder);
        update();
    }

    /**
     * Clears the stats from a stat holder
     *
     * @param holder stat holder
     */
    public void clearStats(StatHolder holder) {
        for (String stat : stats)
            set(stat, 0);
    }

    /**
     * Updates the stats for this scoreboard
     */
    public void update() {
        for (StatHolder holder : holders) {
            int index = 0;
            for (Integer value : holder.getValues())
                set(holder.getNames().get(index++), value);
        }
        showPlayer();
    }
}
