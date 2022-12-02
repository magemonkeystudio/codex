package mc.promcteam.engine.manager.editor;

import mc.promcteam.engine.NexEngine;
import mc.promcteam.engine.manager.editor.object.IEditorActionsMain;
import mc.promcteam.engine.manager.editor.object.IEditorActionsParametized;
import mc.promcteam.engine.manager.editor.object.IEditorActionsParams;
import mc.promcteam.engine.manager.editor.object.IEditorActionsSection;
import mc.promcteam.engine.utils.StringUT;
import mc.promcteam.engine.utils.actions.ActionCategory;
import mc.promcteam.engine.utils.actions.ActionSection;
import mc.promcteam.engine.utils.actions.Parametized;
import mc.promcteam.engine.utils.actions.params.IParam;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class EditorActionsHandler extends EditorHandler<NexEngine> {

    public EditorActionsHandler(@NotNull NexEngine plugin) {
        super(plugin, EditorType.class, null);
    }

    @Override
    protected boolean onType(@NotNull Player p, @Nullable Object editObject,
                             @NotNull Enum<?> type, @NotNull String msg) {

        if (type == EditorType.OBJECT_ACTIONS_PARAM_VALUE || type == EditorType.OBJECT_ACTIONS_PARAM_ADD) {
            if (editObject == null) return false;
            return this.onTypeParam(p, editObject, (EditorType) type, msg);
        }
        if (type == EditorType.OBJECT_ACTIONS_SECTION_ADD) {
            if (editObject == null) return false;

            String                sectionId = StringUT.colorOff(msg.toLowerCase());
            IEditorActionsMain<?> editor    = (IEditorActionsMain<?>) editObject;
            if (editor.getActionBuilders().containsKey(sectionId)) return false;

            ActionSection                    section = new ActionSection(new ArrayList<>(), new ArrayList<>(), "null", new ArrayList<>());
            IEditorActionsMain.ActionBuilder builder = new IEditorActionsMain.ActionBuilder(section, sectionId);

            editor.getActionBuilders().put(sectionId, builder);
            editor.open(p, 1);
            editor.save();
            return true;
        }
        if (type == EditorType.OBJECT_ACTIONS_PARAMETIZED_ADD) {
            if (editObject == null) return false;

            IEditorActionsParametized<?> editor   = (IEditorActionsParametized<?>) editObject;
            String                       pzId     = msg;
            ActionCategory               category = editor.getSectionType();
            Parametized                  pz       = plugin.getActionsManager().getParametized(category, pzId);
            if (pz == null) {
                EditorManager.errorCustom(p, plugin.lang().Core_Editor_Actions_Subject_Invalid.getMsg());
                return false;
            }

            // Check for valid Builder.
            IEditorActionsMain.ActionBuilder builder = editor.getSectionEditor().getEditorMain().getActionBuilder(editor.getSectionEditor().getSectionId());
            if (builder == null) {
                EditorManager.errorCustom(p, plugin.lang().Error_Internal.getMsg());
                return false;
            }

            builder.addParametized(pz, category);
            editor.open(p, 1);
            editor.getSectionEditor().getEditorMain().save();
            return true;
        }

        return true;
    }

    private boolean onTypeParam(@NotNull Player p, @NotNull Object editObject,
                                @NotNull EditorType type, @NotNull String msg) {

        IEditorActionsParams<?>          paramEditor   = (IEditorActionsParams<?>) editObject;
        IEditorActionsSection<?>         sectionEditor = paramEditor.getSctionEditor();
        IEditorActionsMain.ActionBuilder builder       = sectionEditor.getEditorMain().getActionBuilder(sectionEditor.getSectionId());
        if (builder == null) {
            EditorManager.errorCustom(p, plugin.lang().Error_Internal.getMsg());
            return false;
        }

        int pId = paramEditor.getpId();

        if (type == EditorType.OBJECT_ACTIONS_PARAM_VALUE) {
            builder.addParametizedParam(pId, paramEditor.getCategory(), paramEditor.getParametized(), paramEditor.getClickedParam(), msg);
        } else if (type == EditorType.OBJECT_ACTIONS_PARAM_ADD) {
            String[] split = msg.split(" ");
            String   param = split[0];
            String   value = split.length >= 2 ? split[1] : "null";

            IParam param2 = plugin.getActionsManager().getParam(param);
            if (param2 == null) {
                EditorManager.errorCustom(p, plugin.lang().Core_Editor_Actions_Param_Invalid.getMsg());
                return false;
            }

            builder.addParametizedParam(pId, paramEditor.getCategory(), paramEditor.getParametized(), param2.getFlag(), value);
        }


        paramEditor.open(p, 1);
        paramEditor.getSctionEditor().getEditorMain().save();
        return true;
    }
}
