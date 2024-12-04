/**
 * Codex
 * studio.magemonkey.codex.mccore.scoreboard.BoardManager
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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import studio.magemonkey.codex.CodexEngine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Main accessor for player scoreboards
 */
public class BoardManager {
    private static final Map<String, PlayerBoards> players = new HashMap<>();
    private static final Map<String, String>       teams   = new HashMap<>();

    private static Scoreboard scoreboard;

    private static boolean scoreboardUsed = false;

    static void init(Player player) {
        init();
        if (scoreboardUsed && player != null) player.setScoreboard(scoreboard);
    }

    /**
     * Initializes the scoreboard utility
     */
    public static void init() {
        if (scoreboard != null) return;
        if (Bukkit.getScoreboardManager() == null) {
            throw new IllegalStateException("Scoreboard manager is null");
        }

        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    }

    /**
     * Retrieves scoreboard data for a player
     *
     * @param player player
     * @return player's scoreboard data
     */
    public static PlayerBoards getPlayerBoards(String player) {
        if (!players.containsKey(player.toLowerCase())) players.put(player.toLowerCase(), new PlayerBoards(player));
        return players.get(player.toLowerCase());
    }

    /**
     * Registers a new team with all player boards
     *
     * @param team team to register
     */
    public static void registerTeam(Team team) {
        init();
        org.bukkit.scoreboard.Team sbTeam = scoreboard.getTeam(team.getName());
        if (sbTeam == null) {
            sbTeam = scoreboard.registerNewTeam(team.getName());
            sbTeam.setAllowFriendlyFire(true);
            sbTeam.setCanSeeFriendlyInvisibles(false);
        }
        updateTeam(team);
    }

    /**
     * Updates a team with all players
     *
     * @param team team to update
     */
    public static void updateTeam(Team team) {
        init();
        org.bukkit.scoreboard.Team sbTeam = scoreboard.getTeam(team.getName());
        if (sbTeam == null) return;

        if (team.getPrefix() != null) sbTeam.setPrefix(team.getPrefix());
        if (team.getSuffix() != null) sbTeam.setSuffix(team.getSuffix());

        // Also update on all player boards
        getAllPlayerBoards().forEach(playerBoards -> {
            Board activeBoard = playerBoards.getActiveBoard();
            if (activeBoard == null) return;

            Scoreboard                 pBoard = activeBoard.getScoreboard();
            org.bukkit.scoreboard.Team pTeam  = pBoard.getTeam(team.getName());
            if (pTeam != null) return;

            pTeam = pBoard.registerNewTeam(team.getName());
            pTeam.setAllowFriendlyFire(true);
            pTeam.setCanSeeFriendlyInvisibles(false);
        });
    }

    /**
     * <p>Sets the team for a player</p>
     * <p>If the team doesn't exist, it will be created
     * with a prefix matching its name</p>
     *
     * @param player player to set to the team
     * @param team   team to add the player to
     */
    public static void setTeam(String player, String team) {
        try {
            enableScoreboard();
            org.bukkit.scoreboard.Team sTeam = scoreboard.getTeam(team);
            if (sTeam == null) {
                sTeam = scoreboard.registerNewTeam(team);
            }
            sTeam.addEntry(player);
            teams.put(player, team);

            // Also update on all player boards
            getAllPlayerBoards().forEach(playerBoards -> {
                Board activeBoard = playerBoards.getActiveBoard();
                if (activeBoard == null) return;

                Scoreboard                 pBoard = activeBoard.getScoreboard();
                org.bukkit.scoreboard.Team pTeam  = pBoard.getTeam(team);
                if (pTeam == null) {
                    pTeam = pBoard.registerNewTeam(team);
                }
                pTeam.addEntry(player);
            });
        } catch (NoSuchMethodError ignored) {
            // Cauldron/Thermos cannot do this
        }
    }

    /**
     * Clears the team for the player with the provided name
     *
     * @param player player name
     */
    public static void clearTeam(String player) {
        if (!teams.containsKey(player)) return;

        init();
        String                     teamName = teams.remove(player);
        org.bukkit.scoreboard.Team sbTeam   = scoreboard.getTeam(teamName);
        if (sbTeam != null) sbTeam.removeEntry(player);

        // Also remove from all player boards
        getAllPlayerBoards().forEach(playerBoards -> {
            Board activeBoard = playerBoards.getActiveBoard();
            if (activeBoard == null) return;

            Scoreboard                 pBoard = activeBoard.getScoreboard();
            org.bukkit.scoreboard.Team pTeam  = pBoard.getTeam(teamName);
            if (pTeam != null) pTeam.removeEntry(player);
        });
    }

    /**
     * Sets the text below player names
     *
     * @param t text to show
     */
    @SuppressWarnings("deprecation")
    public static void setTextBelowNames(String t) {
        enableScoreboard();
        Objective objective = scoreboard.getObjective(DisplaySlot.BELOW_NAME);
        if (objective == null) {
            objective = scoreboard.registerNewObjective("dummy", "dummy");
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }
        objective.setDisplayName(t);
    }

    /**
     * Enables the scoreboard, overriding all players' scoreboards
     */
    private static void enableScoreboard() {
        if (scoreboardUsed) return;

        init();
        Bukkit.getOnlinePlayers().forEach(player -> player.setScoreboard(scoreboard));
        scoreboardUsed = true;
    }

    /**
     * Sets the score for a player for below name text
     *
     * @param player player to set for
     * @param score  score to set
     */
    @SuppressWarnings("deprecation")
    public static void setBelowNameScore(String player, int score) {
        if (!scoreboardUsed) throw new IllegalStateException("Cannot set below name score before text");

        init();
        Objective objective = scoreboard.getObjective(DisplaySlot.BELOW_NAME);
        if (objective == null) {
            CodexEngine.get().warn("Failed to update below-name score since the objective is missing");
            return;
        }

        objective.getScore(player).setScore(score);

        // Also update on all player boards
        getAllPlayerBoards().forEach(playerBoards -> {
            Board activeBoard = playerBoards.getActiveBoard();
            if (activeBoard == null) return;

            Scoreboard pBoard = activeBoard.getScoreboard();
            Objective  obj    = pBoard.getObjective(DisplaySlot.BELOW_NAME);
            if (obj == null) {
                obj = pBoard.registerNewObjective("dummy", "dummy");
                obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
            }

            obj.getScore(player).setScore(score);
        });
    }

    /**
     * Clears the score for a player
     *
     * @param player player to clear for
     */
    public static void clearScore(String player) {
        init();
        scoreboard.resetScores(player);

        // Also update on all player boards
        getAllPlayerBoards().forEach(playerBoards -> {
            Board activeBoard = playerBoards.getActiveBoard();
            if (activeBoard == null) return;

            Scoreboard pBoard = activeBoard.getScoreboard();
            pBoard.resetScores(player);
        });
    }

    /**
     * @return collection of player data
     */
    public static Collection<PlayerBoards> getAllPlayerBoards() {
        return players.values();
    }

    /**
     * Sets a label for a health bar under each player's name
     *
     * @param label label to set
     * @deprecated use setTextBelowNames instead
     */
    @Deprecated
    public static void setGlobalHealthBar(String label) {
        setTextBelowNames(label);
    }

    /**
     * Gives the scoreboard to every player
     *
     * @param board scoreboard to add
     */
    public static void addGlobalScoreboard(Board board) {
        players.values().forEach(player -> player.addBoard(board));
    }

    /**
     * Clears a board for all players
     *
     * @param plugin plugin to clear
     */
    public static void clearPluginBoards(String plugin) {
        players.values().forEach(player -> player.removeBoards(plugin));
    }

    /**
     * Clears data for a player with a given name
     *
     * @param name player name
     */
    public static void clearPlayer(String name) {
        players.remove(name.toLowerCase());
    }

    /**
     * Copies the scoreboard data from the main scoreboard to the provided scoreboard
     *
     * @param update scoreboard to copy to
     */
    public static void update(Scoreboard update) {
        if (scoreboard == null) return;

        scoreboard.getTeams().forEach(team -> {
            org.bukkit.scoreboard.Team sbTeam = update.getTeam(team.getName());
            if (sbTeam == null) {
                sbTeam = update.registerNewTeam(team.getName());
            }
            sbTeam.setAllowFriendlyFire(team.allowFriendlyFire());
            sbTeam.setCanSeeFriendlyInvisibles(team.canSeeFriendlyInvisibles());
            sbTeam.setDisplayName(team.getDisplayName());
            sbTeam.setPrefix(team.getPrefix());
            sbTeam.setSuffix(team.getSuffix());

            team.getEntries().forEach(sbTeam::addEntry);
        });

        scoreboard.getObjectives().forEach(objective -> {
            Objective obj = update.getObjective(objective.getName());
            if (obj == null) {
                try {
                    obj = update.registerNewObjective(objective.getName(),
                            objective.getTrackedCriteria(),
                            objective.getDisplayName());
                } catch (NoSuchMethodError e) {
                    obj = update.registerNewObjective(objective.getName(),
                            objective.getCriteria(),
                            objective.getDisplayName());
                }
            }
            obj.setDisplaySlot(objective.getDisplaySlot());
            obj.setDisplayName(objective.getDisplayName());

            Objective finalObj = obj;
            Objects.requireNonNull(objective.getScoreboard())
                    .getEntries()
                    .forEach(entry -> finalObj.getScore(entry).setScore(objective.getScore(entry).getScore()));
        });
    }
}
