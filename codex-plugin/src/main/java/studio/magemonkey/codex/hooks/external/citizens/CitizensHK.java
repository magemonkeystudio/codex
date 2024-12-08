package studio.magemonkey.codex.hooks.external.citizens;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.codex.CodexEngine;
import studio.magemonkey.codex.CodexPlugin;
import studio.magemonkey.codex.hooks.HookState;
import studio.magemonkey.codex.hooks.NHook;

import java.util.*;

public class CitizensHK extends NHook<CodexEngine> {
    private Map<CodexPlugin<?>, Set<TraitInfo>>        traits;
    private Map<CodexPlugin<?>, Set<CitizensListener>> listeners;

    public CitizensHK(@NotNull CodexEngine plugin) {
        super(plugin);
    }

    @Override
    @NotNull
    protected HookState setup() {
        this.traits = new HashMap<>();
        this.listeners = new HashMap<>();

        this.registerListeners();
        return HookState.SUCCESS;
    }

    @Override
    protected void shutdown() {
        this.unregisterListeners();

        this.traits.forEach((plugin, traits) -> {
            traits.forEach(trait -> CitizensAPI.getTraitFactory().deregisterTrait(trait));
        });
        this.traits.clear();
        this.listeners.clear();
    }

    public void addListener(@NotNull CodexPlugin<?> plugin, @NotNull CitizensListener listener) {
        this.getListeners(plugin).add(listener);
    }

    public void removeListener(@NotNull CitizensListener listener) {
        this.getListeners(plugin).remove(listener);
    }

    @NotNull
    public Set<CitizensListener> getListeners(@NotNull CodexPlugin<?> plugin) {
        return this.listeners.computeIfAbsent(plugin, set -> new HashSet<>());
    }

    public void unregisterListeners(@NotNull CodexPlugin<?> plugin) {
        this.listeners.remove(plugin);
        plugin.info("[Citizens Hook] Unregistered listeners");
    }

    public void registerTrait(@NotNull CodexPlugin<?> plugin, @NotNull Class<? extends Trait> trait) {
        TraitInfo traitInfo = TraitInfo.create(trait);
        this.registerTrait(plugin, traitInfo);
    }

    public void registerTrait(@NotNull CodexPlugin<?> plugin, @NotNull TraitInfo trait) {
        this.unregisterTrait(plugin, trait);
        if (this.traits.computeIfAbsent(plugin, set -> new HashSet<>()).add(trait)) {
            plugin.info("[Citizens Hook] Registered trait: " + trait.getTraitName());
            CitizensAPI.getTraitFactory().registerTrait(trait);
        }
    }

    public void unregisterTrait(@NotNull CodexPlugin<?> plugin, @NotNull TraitInfo trait) {
        if (this.traits.getOrDefault(plugin, Collections.emptySet()).remove(trait)) {
            plugin.info("[Citizens Hook] Unregistered trait: " + trait.getTraitName());
        }
        CitizensAPI.getTraitFactory().deregisterTrait(trait);
    }

    public void unregisterTraits(@NotNull CodexPlugin<?> plugin) {
        this.traits.getOrDefault(plugin, Collections.emptySet()).forEach(trait -> {
            plugin.info("[Citizens Hook] Unregistered trait: " + trait.getTraitName());
            CitizensAPI.getTraitFactory().deregisterTrait(trait);
        });
        this.traits.remove(plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onLeftClick(NPCLeftClickEvent e) {
        this.listeners.values().forEach(set -> set.forEach(listener -> listener.onLeftClick(e)));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onRightClick(NPCRightClickEvent e) {
        this.listeners.values().forEach(set -> set.forEach(listener -> listener.onRightClick(e)));
    }
}
