# Configuration

Codex's main config lives at `plugins/Codex/config.yml`. Reload it in-game with `/codex reload`
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

### `core.lang`
Language file to load from `plugins/Codex/lang/`. Ships with `en` and `cn`. See [[Localization]].

```yaml
core:
  lang: en
```

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

Display names substituted for world folder names in messages. Add an entry per world; worlds without
an entry fall back to their raw folder name.

```yaml
locale:
  world-names:
    world: 'World'
    world_nether: 'Nether'
    world_the_end: 'The End'
    skyblock: '&bSkyblock'
```

---

## `Features`

Toggles for Codex's two optional built-in modules. Both default to `true`.

| Key | Effect |
|---|---|
| `chat-enabled` | Enables the [[Chat Module]] and its `/chat` commands |
| `scoreboards-enabled` | Enables the [[Scoreboard Module]] and its `/board` commands |

```yaml
Features:
  chat-enabled: true
  scoreboards-enabled: true
```

Turn these off if another plugin already manages chat formatting or scoreboards — running both
usually produces conflicts.

---

## `Settings`

### `Settings.command-cooldown-message`
Shown when a player runs a command still on cooldown. `{time}` is replaced with the remaining
seconds.

```yaml
Settings:
  command-cooldown-message: "&4Please wait &6{time} seconds &4before using the command again."
```

---

## Top-level keys

### `file-timings`
When `true`, Codex logs how long each configuration file takes to load. Useful for diagnosing slow
startup on servers with very large config sets. Default `false`.

### `bungee` / `bungee_id`
Enables BungeeCord messaging support and sets the channel identifier used to talk to the companion
`Codex-Bungee` plugin. Both sides must use the same `bungee_id`.

```yaml
bungee: false
bungee_id: codexcore
```

### `debug`
Enables verbose debug logging. Noisy — leave off unless you are chasing a bug or have been asked for
debug output on Discord.

### `removeBoatOnExit`
When `true`, boats are removed once the last passenger exits. Helps with boat litter on public
servers.

### `action-bar-legacy`
Forces the legacy action bar packet path. Set to `true` if action bar messages do not display
correctly on your server version. See [[Version Support]].

### `unstuck`
Controls the `/stuck` command.

```yaml
unstuck:
  cooldown: 30   # seconds between uses, per player
  warmup: 5      # seconds the player must stand still before teleporting
```

During the warmup the player must not move — moving cancels the teleport. See [[Commands]].

### `onJoin` / `onFirstJoin` / `onInteract`
Command automation. These have their own page: [[Join and Interact Commands]].

---

## Reloading

```
/codex reload
```

Requires `codex.admin`. Most keys apply immediately. Changes to `Features.chat-enabled` and
`Features.scoreboards-enabled` register or unregister listeners and commands, so a full server
restart is the safer option for those.
