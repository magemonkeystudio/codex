package studio.magemonkey.codex.manager.editor.object;

import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.config.api.JYML;
import studio.magemonkey.codex.manager.api.gui.*;
import studio.magemonkey.codex.manager.editor.EditorManager;
import studio.magemonkey.codex.manager.editor.EditorType;
import studio.magemonkey.codex.util.ClickText;
import studio.magemonkey.codex.util.ClickText.ClickWord;
import studio.magemonkey.codex.util.StringUT;
import studio.magemonkey.codex.util.actions.ActionCategory;
import studio.magemonkey.codex.util.actions.Parametized;
import studio.magemonkey.codex.util.actions.params.IParam;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import studio.magemonkey.codex.manager.api.gui.*;

import java.util.*;

public class IEditorActionsParams<P extends CodexPlugin<P>> extends NGUI<P> {

    private IEditorActionsSection<P> sectionEditor;
    private Parametized              parametized;
    private ActionCategory           category;
    private int                      pId;
    private String                   clickedParam;

    private static int[]        objSlots;
    private static String       objName;
    private static List<String> objLore;

    public IEditorActionsParams(
            @NotNull P plugin, @NotNull IEditorActionsSection<P> section,
            @NotNull ActionCategory category, @NotNull Parametized parametized, int pId) {

        super(plugin, EditorManager.EDITOR_ACTIONS_PARAMS, "");
        this.sectionEditor = section;
        this.category = category;
        this.parametized = parametized;
        this.pId = pId;

        JYML cfg = EditorManager.EDITOR_ACTIONS_PARAMS;
        objSlots = cfg.getIntArray("object-slots");
        objName = StringUT.color(cfg.getString("object-name", "&eParam: &6%param-name%"));
        objLore = StringUT.color(cfg.getStringList("object-lore"));

        GuiClick click = new GuiClick() {
            @Override
            public void click(Player p, @Nullable Enum<?> type, InventoryClickEvent e) {
                if (type == null) return;

                Class<?> clazz = type.getClass();
                if (clazz.equals(ContentType.class)) {
                    ContentType type2 = (ContentType) type;
                    switch (type2) {
                        case EXIT: {
                            p.closeInventory();
                            break;
                        }
                        case RETURN: {
                            sectionEditor.getEditorParametized(category).open(p, 1);
                            break;
                        }
                        default: {
                            break;
                        }
                    }
                } else if (clazz.equals(EditorType.class)) {
                    EditorType type2 = (EditorType) type;

                    if (type2 == EditorType.OBJECT_ACTIONS_PARAM_ADD) {
                        EditorManager.tipCustom(p, plugin.lang().Codex_Editor_Actions_Param_Add.getMsg());
                        EditorManager.startEdit(p, IEditorActionsParams.this, type2);
                        p.closeInventory();

                        StringBuilder builder  = new StringBuilder();
                        Set<IParam>                      params   = new HashSet<>(parametized.getParams());
                        IEditorActionsMain.ActionBuilder abuilder = section.getEditorMain().getActionBuilder(section.getSectionId());
                        if (abuilder == null) return;

                        // Remove already added.
                        params.removeIf(param -> {
                            return abuilder.getParametized(category)
                                    .get(pId)
                                    .values()
                                    .stream()
                                    .anyMatch(map -> map.containsKey(param.getKey().toLowerCase()));
                        });

                        params.forEach(param -> {
                            if (builder.length() > 0) builder.append("&7 | ");
                            builder.append("%" + param.getKey() + "%");
                        });

                        ClickText clickText = new ClickText(builder.toString());
                        params.forEach(param -> {
                            ClickWord word =
                                    clickText.createPlaceholder("%" + param.getKey() + "%", "&a" + param.getKey());
                            word.hint(plugin.lang().Codex_Editor_Actions_Param_Hint.asList());
                            word.suggCmd(param.getKey() + " ");
                        });

                        clickText.send(p);
                    }
                }
            }
        };

        for (String sId : cfg.getSection("content")) {
            GuiItem guiItem = cfg.getGuiItem("content." + sId, ContentType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }
            this.addButton(guiItem);
        }

        for (String sId : cfg.getSection("editor")) {
            GuiItem guiItem = cfg.getGuiItem("editor." + sId, EditorType.class);
            if (guiItem == null) continue;

            Enum<?> type = guiItem.getType();
            if (type != null) {
                guiItem.setClick(click);
            }
            this.addButton(guiItem);
        }
    }

    @NotNull
    public IEditorActionsSection<P> getSctionEditor() {
        return this.sectionEditor;
    }

    @NotNull
    public Parametized getParametized() {
        return parametized;
    }

    @NotNull
    public ActionCategory getCategory() {
        return category;
    }

    public int getpId() {
        return pId;
    }

    @NotNull
    public String getClickedParam() {
        return clickedParam;
    }

    @Override
    protected void onCreate(@NotNull Player p, @NotNull Inventory inv, int page) {
        // Check for valid Builder.
        IEditorActionsMain.ActionBuilder builder = this.sectionEditor.getEditorMain().getActionBuilder(sectionEditor.getSectionId());
        if (builder == null) {
            plugin.warn("Invalid ActionBuilder for '" + sectionEditor.getSectionId() + "' section! (2)");
            return;
        }

        Map<Parametized, Map<String, String>> mapTarget = builder.getParametized(category).get(pId);
        Map<String, String>                   par       = mapTarget.get(parametized);

        int count = 0;
        for (Map.Entry<String, String> en : par.entrySet()) {
            ItemStack item = new ItemStack(Material.STONE_BUTTON);
            item.setAmount(count + 1);

            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;

            List<String> lore = new ArrayList<>(objLore);
            lore.replaceAll(line -> line
                    .replace("%param-value%", en.getValue())
                    .replace("%param-key%", en.getKey())
            );

            meta.setDisplayName(objName.replace("%param-value%", en.getValue()).replace("%param-name%", en.getKey()));
            meta.setLore(lore);
            item.setItemMeta(meta);

            JIcon icon = new JIcon(item);
            icon.setClick((p2, type, e) -> {
                if (e.isShiftClick() && e.isRightClick()) {
                    builder.removeParametizedParam(pId, category, parametized, en.getKey());
                    this.open(p, 1);
                    this.getSctionEditor().getEditorMain().save(); // Save config
                    return;
                }

                this.clickedParam = en.getKey();

                EditorManager.tipCustom(p2, plugin.lang().Codex_Editor_Actions_Param_Edit.getMsg());
                EditorManager.startEdit(p2, this, EditorType.OBJECT_ACTIONS_PARAM_VALUE);
                p2.closeInventory();

            });
            this.addButton(p, icon, objSlots[count++]);
        }
    }

    @Override
    protected boolean ignoreNullClick() {
        return true;
    }

    @Override
    protected boolean cancelClick(int slot) {
        return true;
    }

    @Override
    protected boolean cancelPlayerClick() {
        return true;
    }
}
