# Hooks

Codex integrates with several third-party plugins. Every hook is optional — Codex detects the plugin
at runtime and enables the integration only if it is present and enabled.

| Plugin | Hook constant | Used for |
|---|---|---|
| Vault | `Hooks.VAULT` | Permission groups, prefixes/suffixes, economy |
| Citizens | `Hooks.CITIZENS` | NPC detection |
| PlaceholderAPI | `Hooks.PLACEHOLDER_API` | Placeholder expansion |
| MythicMobs | `Hooks.MYTHIC_MOBS` | Mob identification, hook actions |
| Nexo | `Hooks.NEXO` | Custom items |
| WorldGuard | `Hooks.WORLD_GUARD` | Region and combat checks |

Oraxen and ItemsAdder integrate through the item provider system instead — see [[Item Providers]].

## Checking availability

```java
if (Hooks.hasPlugin(Hooks.VAULT)) {
    // safe to use Vault-backed helpers
}

if (Hooks.hasPlaceholderAPI()) {
    text = PlaceholderAPI.setPlaceholders(player, text);
}
```

The `Hooks` helpers are null-safe: with Vault absent, `getPermGroup` and `getPrefix`/`getSuffix`
return `""` and `getPermissionGroups` returns an empty set. They will not throw. Guard anyway when an
empty result is not a meaningful answer for your logic.

> ⚠️ **The engine's hook getters can return `null` even when the plugin is installed.** Only Vault
> and Nexo are hooked eagerly at startup. WorldGuard, Citizens, and MythicMobs are hooked
> *exclusively* from the `PluginEnableEvent` path, with no fallback scan — so if Codex misses that
> event, `getWorldGuard()`, `getCitizens()`, and `getMythicMobs()` stay `null`. Null-check them, or
> go through the `Hooks` helpers, which handle absence for you.

## Accessing individual hooks

```java
CodexEngine engine = CodexEngine.get();

VaultHK      vault      = engine.getVault();
WorldGuardHK worldGuard = engine.getWorldGuard();
CitizensHK   citizens   = engine.getCitizens();
IMythicHook  mythic     = engine.getMythicMobs();
NexoHK       nexo       = engine.getNexo();

HookManager hooks = engine.getHooksManager();
```

---

## Vault helpers

The `Hooks` class exposes several convenience methods built on Vault's permission API.

### Groups

```java
String       group  = Hooks.getPermGroup(player);       // primary group
Set<String>  groups = Hooks.getPermissionGroups(player); // all groups
```

### Prefixes and suffixes

```java
String prefix = Hooks.getPrefix(player);
String suffix = Hooks.getSuffix(player);
```

Used by the [[Chat Module]].

### Per-group values

A common pattern is "give each rank a different limit" without hard-coding rank names. Codex resolves
the best value across all of a player's groups:

```java
Map<String, Integer> limits = Map.of(
    "default", 3,
    "vip",     10,
    "admin",   100
);

int limit = Hooks.getGroupValueInt(player, limits, false);
```

The final `boolean` is `isNegaBetter`. By default the **highest** value across the player's groups
wins. Setting it to `true` does *not* invert that — it makes a **negative** value win outright, which
is the usual "unlimited / no cap" sentinel. Positive values are still compared highest-first.

```java
Map.of("default", 3, "vip", 10)   // → 10, with isNegaBetter either way
Map.of("default", 3, "vip", -1)   // → -1 when isNegaBetter is true, 3 when false
```

Three things to watch:

- **Map keys must be lowercase.** Group names are lowercased before comparison, but the map lookup is
  case-sensitive, so a key of `"VIP"` never matches. (`"default"` is the one exception.)
- **All three return `-1` when nothing matches**, which is indistinguishable from a real `-1`.
- **`getGroupValueDouble` is broken for fractional values** — its comparator casts the difference to
  `int`, so `2.5` and `2.1` compare equal. `getGroupValueLong` has the same narrowing cast.

Long and double variants exist too:

```java
Hooks.getGroupValueLong(player, longMap, false);
Hooks.getGroupValueDouble(player, doubleMap, false);
```

---

## Entity checks

```java
boolean isNpc    = Hooks.isNPC(entity);      // Citizens
boolean isMythic = Hooks.isMythic(entity);   // MythicMobs
```

Both return `false` when the backing plugin is absent, so they are safe to call unguarded.

### Combat checks

```java
if (Hooks.canFights(attacker, victim)) {
    // damage is permitted here
}
```

This consults WorldGuard region flags and excludes Citizens NPCs where applicable. Use it rather than
checking PvP yourself — it is what the [[Actions Engine]]'s `~attackable:` parameter uses, so
behaviour stays consistent between code and config.

---

## MythicMobs versions

MythicMobs changed its API substantially at version 5. Codex ships two implementations behind one
interface:

```java
IMythicHook mythic = CodexEngine.get().getMythicMobs();
```

`MythicMobsHK` handles v4 and `MythicMobsHKv5` handles v5; the right one is selected automatically.
Program against `IMythicHook` and you do not need to care which is active.

---

## Writing a hook

Hooks extend `NHook` and are managed by `HookManager`. Each has a `HookState` describing whether it
is enabled, disabled, or unavailable.

The pattern is: check whether the target plugin is present, set up integration if so, and degrade
gracefully if not. Codex logs which hooks activated during startup, so a missing integration is
visible in console rather than silent.

## Late hooking

Some plugins enable after Codex does. Codex listens for `PluginEnableEvent` and completes hook setup
when a relevant plugin appears later in the startup sequence, so load order is not something server
owners need to manage manually.
