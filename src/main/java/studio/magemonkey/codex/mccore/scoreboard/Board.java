/**
 * Codex
 * studio.magemonkey.codex.mccore.scoreboard.Board
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

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.UUID;

/**
 * A manager for a scoreboard
 */
public abstract class Board {
    protected final String     plugin;
    private final   String     title;
    @Getter
    private final   Scoreboard scoreboard;
    private final   Objective  objective;
    private         UUID       uuid;

    /**
     * @param title  title of the scoreboard
     * @param plugin plugin owning the scoreboard
     */
    @SuppressWarnings("deprecation")
    public Board(String title, String plugin) {
        this.plugin = plugin;
        this.title = title;

        if (Bukkit.getScoreboardManager() == null) {
            throw new IllegalStateException("Scoreboard manager is null");
        }

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        objective = scoreboard.registerNewObjective(title, "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    /**
     * Sets the player for the board
     *
     * @param player owning player
     */
    public void setPlayer(Player player) {
        this.uuid = player.getUniqueId();
    }

    /**
     * Gets the name of the scoreboard
     *
     * @return scoreboard name
     */
    public String getName() {
        return title;
    }

    /**
     * Sets a score to the scoreboard
     *
     * @param label label to use
     * @param score score to show
     */
    protected void set(String label, int score) {
        resetScores(score);
        objective.getScore(label).setScore(score);
    }

    private void resetScores(int score) {
        if (objective.getScoreboard() == null) return;

        objective.getScoreboard().getEntries()
                .stream().filter(str -> objective.getScore(str).getScore() == score)
                .forEach(objective.getScoreboard()::resetScores);
    }

    /**
     * Shows the board to it's player
     */
    public boolean showPlayer() {
        Player player = getPlayer();
        if (player == null || !player.isOnline())
            return false;

        BoardManager.update(scoreboard);
        player.setScoreboard(scoreboard);
        return true;
    }

    protected Player getPlayer() {
        if (uuid == null) return null;
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Sets the health objective for the scoreboard
     * - Recommended not to use this method -
     * - Use PlayerBoard or BoardManager instead -
     *
     * @param label scoreboard label
     * @deprecated use setTextBelowNames in BoardManager instead
     */
    @Deprecated
    public void setHealthLabel(String label) {
        BoardManager.setTextBelowNames(label);
    }

    /**
     * Clears the side board display
     */
    public void clearDisplay() {
        Player player = getPlayer();
        if (player == null || !player.isOnline() || Bukkit.getScoreboardManager() == null)
            return;

        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        BoardManager.update(board);
        player.setScoreboard(board);
    }

    /**
     * Hashes by name
     *
     * @return name hash
     */
    @Override
    public int hashCode() {
        return title.hashCode();
    }

    /**
     * Equates by name
     *
     * @param other other board to equate to
     * @return true if titles are equal
     */
    @Override
    public boolean equals(Object other) {
        return other instanceof Board && title.equals(((Board) other).title);
    }
}
