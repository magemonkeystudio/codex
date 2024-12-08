package studio.magemonkey.codex.hooks;

import org.jetbrains.annotations.NotNull;

public enum HookState {
    SUCCESS("Success!"),
    ERROR("Error!"),
    ;

    private final String state;

    HookState(@NotNull String state) {
        this.state = state;
    }

    @NotNull
    public String getName() {
        return this.state;
    }
}
