/**
 * Codex
 * studio.magemonkey.codex.mccore.scoreboard.PlayerBoards
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2024 MageMonkeyStudio
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
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Scoreboard data for a player
 */
public class PlayerBoards {
    /**
     * -- GETTER --
     *
     * @return the boards attached to the player
     */
    @Getter
    private final List<Board> boards = new ArrayList<>();

    /**
     * -- GETTER --
     *
     * @return name of the owning player
     */
    @Getter
    private final String  playerName;
    /**
     * Whether the player's scoreboard is cycling
     * -- GETTER --
     *
     * @return true if cycling, false otherwise
     */
    @Getter
    protected     boolean cycling;
    private       Board   currentBoard;
    /**
     * -- GETTER --
     *
     * @return true if enabled, false otherwise
     */
    @Getter
    private       boolean enabled = true;
    private       int     current = 0;

    /**
     * Constructor
     *
     * @param playerName name of the player
     */
    public PlayerBoards(String playerName) {
        this.playerName = playerName;
        cycling = true;
    }

    /**
     * Toggles visibility of the scoreboard
     */
    public void toggle() {
        if (currentBoard != null) {
            if (enabled)
                currentBoard.clearDisplay();
            else
                currentBoard.showPlayer();
        }
        enabled = !enabled;
    }

    /**
     * @return owning player reference
     */
    public Player getPlayer() {
        return Bukkit.getPlayer(playerName);
    }

    /**
     * Adds a scoreboard for the player
     *
     * @param board board to add
     */
    public void addBoard(Board board) {
        Player player = getPlayer();
        if (player == null)
            throw new IllegalStateException("Cannot add boards when no player is present");

        board.setPlayer(player);
        boards.add(board);
        if (currentBoard == null)
            showNextBoard();
    }

    /**
     * Removes a board from a player
     *
     * @param board board to remove
     */
    public void removeBoard(Board board) {
        if (!boards.contains(board))
            return;

        boards.remove(board);
        if (currentBoard == board) {
            clear();
            showNextBoard();
        }
    }

    /**
     * Removes all boards from a plugin
     *
     * @param plugin plugin name
     */
    public void removeBoards(String plugin) {
        boards.stream().filter(board -> board.plugin.equalsIgnoreCase(plugin))
                .collect(Collectors.toSet()).forEach(this::removeBoard);

        clear();
    }

    /**
     * Clears current board data
     */
    private void clear() {
        if (currentBoard != null)
            currentBoard.clearDisplay();
        current = -1;
        currentBoard = null;
    }

    /**
     * Shows a scoreboard for the player
     *
     * @param name name of the scoreboard
     * @return true if successful, false otherwise
     */
    public boolean showBoard(String name) {
        for (int i = 0; i < boards.size(); i++)
            if (format(boards.get(i).getName()).equals(name))
                show(i);

        return false;
    }

    private void show(int index) {
        if (currentBoard != null)
            currentBoard.clearDisplay();
        current = index;
        currentBoard = boards.get(index);
        currentBoard.showPlayer();
    }

    /**
     * Shows the next scoreboard
     */
    public void showNextBoard() {
        if (boards.isEmpty() || !enabled) return;

        int next = (current + 1) % boards.size();
        if (next != current) show(next);
    }

    /**
     * Formats the name for the hash table
     *
     * @param name board name
     * @return formatted board name
     */
    private String format(String name) {
        return ChatColor.stripColor(name.toLowerCase());
    }

    /**
     * Retrieves a board manager
     *
     * @param name scoreboard name
     * @return board manager
     */
    public Board getBoard(String name) {
        return boards.stream().filter(board -> format(board.getName()).equals(name)).findFirst().orElse(null);
    }

    /**
     * Gets the active board for the player
     *
     * @return active board
     */
    @Nullable
    public Board getActiveBoard() {
        return currentBoard;
    }

    /**
     * Checks whether the player has an active scoreboard
     *
     * @return if the player has an active scoreboard
     */
    public boolean hasActiveBoard() {
        return !boards.isEmpty() && enabled;
    }

    /**
     * Makes the player's scoreboard cycle
     */
    public void startCycling() {
        cycling = true;
    }

    /**
     * Makes the player's scoreboard stop cycling
     */
    public void stopCycling() {
        cycling = false;
    }

    /**
     * Sets the health label for this player
     *
     * @param label health label
     * @deprecated use BoardManager.setTextBelowNames instead
     */
    @Deprecated
    public void setHealthLabel(String label) {
        BoardManager.setTextBelowNames(label);
    }
}
