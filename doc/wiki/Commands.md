# Commands

Codex registers one main command plus a standalone `/stuck` command. The [[Chat Module]] and
[[Scoreboard Module]] add their own trees when enabled.

## `/codex`

The main command. Its labels come from `core.command-aliases` in [[Configuration]] — by default
`/codex`, `/codexengine`, and `/codexcore` all work.

Running `/codex` with no arguments shows the help output.

| Subcommand | Permission | Description |
|---|---|---|
| `/codex help` | `codex.user` | Lists available subcommands |
| `/codex reload` | `codex.admin` | Reloads configuration and language files |
| `/codex editor` | `codex.cmd.editor` | Opens the in-game editor GUI |
| `/codex about` | *none* | Shows plugin version and credits |

### Availability notes

Two subcommands are conditional, which is why you may not see them:

- **`editor`** is only registered by plugins that declare an editor. The Codex engine itself does
  not, so `/codex editor` will not appear — but `/fabled editor` and similar will on plugins built
  on Codex that do. See [[Actions Engine]].
- **`about`** is registered only on *child* plugins, not on the engine. `/codex about` is therefore
  not available; `/fabled about` is.

This is the same command framework every Codex-based plugin uses, so `/fabled …` and `/divinity …`
expose the same `help` / `reload` / `editor` / `about` structure with their own permission prefixes
(`fabled.admin`, `divinity.admin`, and so on).

---

## `/stuck`

Teleports a player who is trapped in terrain to a safe location.

```
/stuck
```

- **Permission:** `codex.stuck` (granted to everyone by default)
- **Players only** — cannot be run from console
- Also registered as `/unstuck`

### Behaviour

1. The player runs `/stuck`.
2. A warmup begins, lasting `unstuck.warmup` seconds (default 5). The player is told to stand still.
3. **If the player moves, the teleport is cancelled.**
4. On completion the player is teleported to a safe location.
5. The command then goes on cooldown for `unstuck.cooldown` seconds (default 30).

Both values are configurable — see [[Configuration]].

### Logging

Every successful use is logged to console with the player's name and origin coordinates:

```
STUCK - Notch executed '/stuck' at world,120.5,64.0,-88.2
```

This is intentional, so staff can review whether the command is being abused to escape from areas
players are meant to be confined to.

---

## Module commands

Available only when the corresponding feature is enabled in [[Configuration]].

| Command | Requires | Page |
|---|---|---|
| `/chat …` | `Features.chat-enabled: true` | [[Chat Module]] |
| `/board …` | `Features.scoreboards-enabled: true` | [[Scoreboard Module]] |

---

## Removed commands

**`/corereload` no longer exists.** It was the ProMCCore-era reload command. Use `/codex reload`.

See [[Permissions]] for the full node list.
