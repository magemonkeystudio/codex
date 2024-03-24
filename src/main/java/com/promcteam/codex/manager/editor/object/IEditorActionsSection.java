package com.promcteam.codex.manager.editor.object;

import com.promcteam.codex.CodexPlugin;
import com.promcteam.codex.config.api.JYML;
import com.promcteam.codex.manager.api.gui.ContentType;
import com.promcteam.codex.manager.api.gui.GuiClick;
import com.promcteam.codex.manager.api.gui.GuiItem;
import com.promcteam.codex.manager.api.gui.NGUI;
import com.promcteam.codex.manager.editor.EditorManager;
import com.promcteam.codex.utils.actions.ActionCategory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class IEditorActionsSection<P extends CodexPlugin<P>> extends NGUI<P> {

    private Map<ActionCategory, IEditorActionsParametized<P>> parametizedEditors;

    private String                sectionId;
    private IEditorActionsMain<P> editorMain;

    public IEditorActionsSection(@NotNull P plugin, @NotNull IEditorActionsMain<P> editorMain, @NotNull String sectionId) {
        super(plugin, EditorManager.EDITOR_ACTIONS_SECTION, "");
        this.parametizedEditors = new HashMap<>();
        this.editorMain = editorMain;
        this.sectionId = sectionId;

        GuiClick click = (p, type, e) -> {
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
                        this.getEditorMain().open(p, 1);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            } else if (clazz.equals(ActionCategory.class)) {
                ActionCategory type2 = (ActionCategory) type;
                this.parametizedEditors
                        .computeIfAbsent(type2, editor -> new IEditorActionsParametized<P>(plugin, IEditorActionsSection.this, type2))
                        .open(p, 1);
            }
        };

        JYML cfg = EditorManager.EDITOR_ACTIONS_SECTION;

        for (String sId : cfg.getSection("content")) {
            GuiItem guiItem = cfg.getGuiItem("content." + sId, ContentType.class);
            if (guiItem == null) continue;

            if (guiItem.getType() != null) {
                guiItem.setClick(click);
            }
            this.addButton(guiItem);
        }

        for (String sId : cfg.getSection("editor")) {
            GuiItem guiItem = cfg.getGuiItem("editor." + sId, ActionCategory.class);
            if (guiItem == null) continue;

            Enum<?> type = guiItem.getType();
            if (type != null) {
                guiItem.setClick(click);
            }
            this.addButton(guiItem);
        }
    }

    @Override
    public void shutdown() {
        if (this.parametizedEditors != null) {
            this.parametizedEditors.values().forEach(editor -> editor.shutdown());
            this.parametizedEditors.clear();
            this.parametizedEditors = null;
        }
        super.shutdown();
    }

    @NotNull
    public String getSectionId() {
        return this.sectionId;
    }

    @NotNull
    public IEditorActionsMain<P> getEditorMain() {
        return editorMain;
    }

    public IEditorActionsParametized<P> getEditorParametized(@NotNull ActionCategory category) {
        return this.parametizedEditors.get(category);
    }

    @Override
    protected void onCreate(@NotNull Player p, @NotNull Inventory inv, int page) {

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
