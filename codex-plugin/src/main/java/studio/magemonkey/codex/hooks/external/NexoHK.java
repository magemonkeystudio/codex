package studio.magemonkey.codex.hooks.external;

import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.api.items.providers.NexoProvider;
import studio.magemonkey.codex.hooks.HookState;
import studio.magemonkey.codex.hooks.NHook;

public class NexoHK extends NHook<CodexEngine> {

    public NexoHK(@NotNull CodexEngine plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        if (!this.plugin.getItemManager().hasProvider(NexoProvider.NAMESPACE)) {
            this.plugin.getItemManager().registerProvider(NexoProvider.NAMESPACE, new NexoProvider());
        }
        return HookState.SUCCESS;
    }

    @Override
    public void shutdown() {
        this.plugin.getItemManager().unregisterProvider(NexoProvider.class);
    }
}
