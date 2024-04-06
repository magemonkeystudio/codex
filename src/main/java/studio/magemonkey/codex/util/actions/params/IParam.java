package studio.magemonkey.codex.util.actions.params;

import studio.magemonkey.codex.util.actions.params.parser.IParamParser;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public abstract class IParam {

    protected final String  key;
    protected final String  flag;
    protected final Pattern pattern;

    public IParam(@NotNull String key, @NotNull String flag) {
        this.key = key.toUpperCase();
        this.flag = flag.toLowerCase();
        this.pattern = Pattern.compile("(~)+(" + this.getFlag() + ")+?(:)+(.*?)(;)");
    }

    @NotNull
    public final String getKey() {
        return this.key;
    }

    @NotNull
    public final Pattern getPattern() {
        return this.pattern;
    }

    @NotNull
    public final String getFlag() {
        return this.flag;
    }

    @NotNull
    public abstract IParamParser getParser();
}
