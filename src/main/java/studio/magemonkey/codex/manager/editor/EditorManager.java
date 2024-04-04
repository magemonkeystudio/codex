package studio.magemonkey.codex.manager.editor;

import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.config.api.JYML;
import studio.magemonkey.codex.util.ClickText;
import studio.magemonkey.codex.util.ClickText.ClickWord;
import studio.magemonkey.codex.util.CollectionsUT;
import studio.magemonkey.codex.util.constants.JStrings;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class EditorManager {
    private static final CodexEngine ENGINE;

    private static final Map<Player, Map.Entry<Enum<?>, Object>> EDITOR_CACHE = new WeakHashMap<>();
    public static        JYML                                    EDITOR_ACTIONS_MAIN;
    public static        JYML                                    EDITOR_ACTIONS_SECTION;
    public static        JYML                                    EDITOR_ACTIONS_PARAMETIZED;
    public static        JYML                                    EDITOR_ACTIONS_PARAMS;

    public static EditorActionsHandler actionsHandler;

    static {
        ENGINE = CodexEngine.get();
    }

    public static void setup() {
        ENGINE.getConfigManager().extract("editor");

        try {
            if (EDITOR_ACTIONS_MAIN == null || !EDITOR_ACTIONS_MAIN.reload()) {
                EDITOR_ACTIONS_MAIN = JYML.loadOrExtract(ENGINE, "/editor/actions_main.yml");
            }
        } catch (Exception e) {
            ENGINE.error("Failed to load editor actions: " + e.getMessage());
            ENGINE.error("Please, check your editor/actions_main.yml file");
            ENGINE.error("Disabling editor...");
            shutdown();
            return;
        }

        try {
            if (EDITOR_ACTIONS_SECTION == null || !EDITOR_ACTIONS_SECTION.reload()) {
                EDITOR_ACTIONS_SECTION = JYML.loadOrExtract(ENGINE, "/editor/actions_section.yml");
            }
        } catch (Exception e) {
            ENGINE.error("Failed to load editor actions: " + e.getMessage());
            ENGINE.error("Please, check your editor/actions_section.yml file");
            ENGINE.error("Disabling editor...");
            shutdown();
            return;
        }

        try {
            if (EDITOR_ACTIONS_PARAMETIZED == null || !EDITOR_ACTIONS_PARAMETIZED.reload()) {
                EDITOR_ACTIONS_PARAMETIZED = JYML.loadOrExtract(ENGINE, "/editor/actions_parametized.yml");
            }
        } catch (Exception e) {
            ENGINE.error("Failed to load editor actions: " + e.getMessage());
            ENGINE.error("Please, check your editor/actions_parametized.yml file");
            ENGINE.error("Disabling editor...");
            shutdown();
            return;
        }

        try {
            if (EDITOR_ACTIONS_PARAMS == null || !EDITOR_ACTIONS_PARAMS.reload()) {
                EDITOR_ACTIONS_PARAMS = JYML.loadOrExtract(ENGINE, "/editor/actions_params.yml");
            }
        } catch (Exception e) {
            ENGINE.error("Failed to load editor actions: " + e.getMessage());
            ENGINE.error("Please, check your editor/actions_params.yml file");
            ENGINE.error("Disabling editor...");
            shutdown();
            return;
        }

        actionsHandler = new EditorActionsHandler(ENGINE);
    }

    public static void shutdown() {
        if (actionsHandler != null) {
            actionsHandler.shutdown();
            actionsHandler = null;
        }
        EDITOR_ACTIONS_MAIN = null;
        EDITOR_ACTIONS_SECTION = null;
        EDITOR_ACTIONS_PARAMETIZED = null;
        EDITOR_ACTIONS_PARAMS = null;
    }

    public static void startEdit(@NotNull Player player, @Nullable Object o, Enum<?> type) {
        EDITOR_CACHE.put(player, new AbstractMap.SimpleEntry<>(type, o));
        ClickText text = new ClickText(ENGINE.lang().Codex_Editor_Tips_Exit_Name.getMsg());
        text.createFullPlaceholder()
                .execCmd("/" + JStrings.EXIT)
                .hint(ENGINE.lang().Codex_Editor_Tips_Exit_Hint.getMsg());
        text.send(player);
    }

    public static void sendClickableTips(@NotNull Player player, @NotNull Collection<String> items2) {
        Collection<String> items = items2.stream().sorted(Comparator.naturalOrder()).collect(Collectors.toList());

        StringBuilder builder = new StringBuilder();
        items.forEach(pz -> {
            if (builder.length() > 0) builder.append(" &7| ");
            builder.append("%" + pz + "%");
        });

        ClickText text = new ClickText(builder.toString());
        items.forEach(pz -> {
            ClickWord word = text.createPlaceholder("%" + pz + "%", "&a" + pz);
            word.hint(ENGINE.lang().Codex_Editor_Tips_Hint.getMsg());
            word.execCmd(pz);
        });

        ENGINE.lang().Codex_Editor_Tips_Header.send(player);
        text.send(player);
    }

    public static void sendCommandTips(@NotNull Player player) {
        ENGINE.lang().Codex_Editor_Tips_Commands.send(player);
    }

    public static boolean isEdit(@NotNull Player player) {
        return getEditor(player) != null;
    }

    public static void endEdit(@NotNull Player player) {
        endEdit(player, true);
    }

    public static void endEdit(@NotNull Player player, boolean msg) {
        if (msg) EditorManager.tip(player, ENGINE.lang().Codex_Editor_Display_Done_Title.getMsg(), "", 40);
        EDITOR_CACHE.remove(player);
    }

    @Nullable
    public static Map.Entry<Enum<?>, Object> getEditor(@NotNull Player player) {
        return EDITOR_CACHE.getOrDefault(player, null);
    }

    public static void tip(@NotNull Player player, @NotNull String title, @NotNull String sub, int stay) {
        if (stay == 999) stay = 100000;

        ENGINE.lang().Codex_Editor_Display_Edit_Format
                .replace("%title%", title)
                .replace("%message%", sub)
                .send(player);
    }

    public static void tipCustom(@NotNull Player player, @NotNull String sub) {
        EditorManager.tip(player, ENGINE.lang().Codex_Editor_Display_Edit_Title.getMsg(), sub, 999);
    }

    public static void errorNumber(@NotNull Player player, boolean mustDecimal) {
        String title = ENGINE.lang().Codex_Editor_Display_Error_Number_Invalid.getMsg();
        String sub   = ENGINE.lang().Codex_Editor_Display_Error_Number_MustInteger.getMsg();
        if (mustDecimal) sub = ENGINE.lang().Codex_Editor_Display_Error_Number_MustDecimal.getMsg();

        EditorManager.tip(player, title, sub, 999);
    }

    public static void errorCustom(@NotNull Player player, @NotNull String sub) {
        EditorManager.tip(player, ENGINE.lang().Codex_Editor_Display_Error_Title.getMsg(), sub, 999);
    }

    public static void errorEnum(@NotNull Player player, @NotNull Class<?> clazz) {
        String title = ENGINE.lang().Codex_Editor_Display_Error_Type_Title.getMsg();
        String sub   = ENGINE.lang().Codex_Editor_Display_Error_Type_Values.getMsg();
        EditorManager.tip(player, title, sub, 999);
        EditorManager.sendClickableTips(player, CollectionsUT.getEnumsList(clazz));
    }
}
