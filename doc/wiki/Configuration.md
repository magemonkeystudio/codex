# Configuration

Codex's main config lives at `plugins/CodexCore/config.yml`. Reload it in-game with `/codex reload`
(see [[Commands]]).

## Full default file

```yaml
core:
  lang: en
  command-aliases: codex,codexengine,codexcore

locale:
  world-names:
    world: 'World'
    world_nether: 'Nether'
    world_the_end: 'The End'

Features:
  chat-enabled: true
  scoreboards-enabled: true

Settings:
  command-cooldown-message: "&4Please wait &6{time} seconds &4before using the command again."

file-timings: false

bungee: false
bungee_id: codexcore

onJoin: []
onFirstJoin: []
onInteract: []

debug: false
removeBoatOnExit: true

unstuck:
  cooldown: 30
  warmup: 5

action-bar-legacy: false
```

---

## `core`

### `core.lang` *(inert)*
Auto-injected on first run, but **nothing reads it**. The value is parsed into a field that has no
consumers.

The key that actually selects the language file is `general.lang`, which is not in the shipped file
and must be added by hand:

```yaml
general:
  lang: en
```

Ships with `en` and `cn`. See [[Localization]].

### `core.command-aliases`
Comma-separated list of labels the main command registers under. The **first entry is the primary
label** and is what appears in help output and in permission-related messages.

```yaml
core:
  command-aliases: codex,codexengine,codexcore
```

With the default above, `/codex`, `/codexengine`, and `/codexcore` are all equivalent.

### `core.prefix`
Not present in the shipped file — Codex writes it on first run if missing, defaulting to the plugin
name. It sets the prefix shown in front of plugin messages and supports colour codes.

```yaml
core:
  prefix: '&6Codex'
```

---

## `locale.world-names`

Read into a lookup map and exposed to other plugins via `CoreConfig.getWorldName(world)`.

> ⚠️ **Codex itself never substitutes these.** `getWorldName` has no callers in the codebase, so
> setting them changes nothing in Codex's own messages. They are only useful if a downstream plugin
> reads them.

```yaml
locale:
  world-names:
    world: 'World'
    world_nether: 'Nether'
    world_the_end: 'The End'
    skyblock: '&bSkyblock'
```

---

## `Features` (deprecated modules)

> ⚠️ Both toggles control modules from the legacy **mccore** codebase, which is **deprecated**. New
> setups should leave both `false` and use dedicated plugins instead.

Both default to `true` for backwards compatibility.

| Key | Effect |
|---|---|
| `chat-enabled` | Enables the [[Chat Module]] and its `/chat` commands |
| `scoreboards-enabled` | Enables the [[Scoreboard Module]] and its `/board` commands |

```yaml
Features:
  chat-enabled: false
  scoreboards-enabled: false
```

Turn these off if another plugin already manages chat formatting or scoreboards — running both
produces conflicts, and Minecraft allows only one sidebar objective per player.

---

## `Settings`

### `Settings.command-cooldown-message` *(legacy)*
Shown when a player runs a command still on cooldown. `{time}` is replaced with the remaining
seconds.

> ⚠️ Consumed only by the legacy **mccore** command framework. The current command system has no
> cooldown mechanism at all, so this setting does not apply to `/codex` or its subcommands.

```yaml
Settings:
  command-cooldown-message: "&4Please wait &6{time} seconds &4before using the command again."
```

---

## Top-level keys

### `file-timings` *(deprecated)*
When `true`, logs how long each configuration file takes to load. Default `false`.

> ⚠️ This setting is read only by the legacy **mccore** config layer and applies solely to files
> loaded through it. It has no effect on configs using the current `JYML` loader, so on a modern
> setup it will report little or nothing.

### `bungee` / `bungee_id`
Enables BungeeCord messaging support. `bungee_id` **labels this server** in outgoing plugin messages
— it is not a channel name, and the proxy never compares it against anything. The channel itself is
the fixed constant `magemonkey:codex`, and the `Codex-Bungee` plugin has no corresponding setting.

```yaml
bungee: false
bungee_id: codexcore
```

### `debug` *(effectively inert)*
Intended to enable verbose logging, and it does toggle the legacy `Debugger`. But that logger has
essentially no call sites left in the codebase, so enabling it produces no useful output. Do not
reach for it when troubleshooting.

### `removeBoatOnExit`
When `true`, exiting a boat removes it and **gives it back to the exiting player** as an item —
dropped at their feet if their inventory is full. It is a pick-up-your-boat convenience, not litter
cleanup, and there is no "last passenger" check: any player exiting any boat triggers it.

> The shipped file sets `true`, but the code reads this key with **no default**, so deleting the line
> turns the feature off rather than leaving it on.

### `action-bar-legacy`
Forces the legacy action bar packet path. Set to `true` if action bar messages do not display
correctly on your server version. See [[Version Support]].

### `unstuck`
Controls the `/unstuck` command (**not** `/stuck`, which does not work — see [[Commands]]).

```yaml
unstuck:
  cooldown: 30   # seconds between uses, per player
  warmup: 5      # seconds the player must stay put before teleporting
```

Cancellation is per **block**, not per movement — turning, jumping in place, or shuffling within the
same block is fine; stepping off it cancels the teleport.

> Neither key has a code default and neither is auto-injected, so deleting them yields `0` for both:
> an instant teleport with no cooldown.

### `onJoin` / `onFirstJoin` / `onInteract`
Command automation. These have their own page: [[Join and Interact Commands]].

---

## Reloading

```
/codex reload
```

Requires `codexcore.admin`.

> ## ⚠️ `/codex reload` does not re-read `config.yml`
>
> On the engine, `reload()` only re-applies settings **already held in memory** — it rebuilds the
> config template from the existing in-memory document and never re-reads the file from disk.
>
> **Edit `config.yml` and restart the server.** No key in this file takes effect from `/codex reload`
> alone.
>
> (Child plugins built on Codex do a fuller reload, so `/fabled reload` and similar may behave
> differently. This limitation is specific to the engine.)

Even setting that aside, several keys are parsed once during startup and would need a restart
regardless: `onJoin`, `onFirstJoin`, `onInteract`, and both `Features` toggles all bind listeners,
tasks, and commands at enable time.
