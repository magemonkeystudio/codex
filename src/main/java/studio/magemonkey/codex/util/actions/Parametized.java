package studio.magemonkey.codex.util.actions;

import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.util.actions.params.IParam;
import studio.magemonkey.codex.util.actions.params.IParamResult;
import studio.magemonkey.codex.util.actions.params.IParamValue;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;

public abstract class Parametized {

    protected final CodexPlugin<?> plugin;
    protected final Set<IParam>    params;
    protected final String         key;

    protected static final String                    FLAG_NO_DELAY = "@NODELAY@";
    private static final   Map<String, IParamResult> RESULT_CACHE  = new HashMap<>();

    public Parametized(@NotNull CodexPlugin<?> plugin, @NotNull String key) {
        this.plugin = plugin;
        this.params = new HashSet<>();
        this.key = key.toUpperCase();
        this.registerParams();
    }

    @NotNull
    public final String getKey() {
        return this.key;
    }

    public abstract void registerParams();

    @NotNull
    public abstract List<String> getDescription();

    protected final void registerParam(@NotNull String key) {
        IParam param = plugin.getActionsManager().getParam(key);
        if (param == null) {
            plugin.error("Trying to register invalid param '" + key + "' !");
            return;
        }
        this.params.add(param);
    }

    @NotNull
    public final Set<IParam> getParams() {
        return this.params;
    }

    @NotNull
    protected final IParamResult getParamResult(@NotNull String fullStr) {
        String cache = fullStr.replace(FLAG_NO_DELAY, "");
        if (RESULT_CACHE.containsKey(cache)) {
            return RESULT_CACHE.get(cache);
        }

        Map<String, IParamValue> values = new HashMap<>();

        for (IParam param : this.getParams()) {
            String flag = param.getFlag(); // Raw flag, without '~' prefix
            if (!fullStr.contains(flag)) continue;

            // Search for flag of this parameter
            Matcher m = param.getPattern().matcher(fullStr); // TODO add Fixed ICharSeq

            // Get the flag value
            if (m.find()) {
                String ext = m.group(4).trim(); // Extract only value from all flag string

                // Parse value from a string
                IParamValue v = param.getParser().parseValue(ext);
                values.put(param.getKey(), v); // Put in result map
            }
        }

        IParamResult result = new IParamResult(values);
        RESULT_CACHE.put(cache, result);
        return result;
    }

    public static void clearCache() {
        RESULT_CACHE.clear();
    }
}
