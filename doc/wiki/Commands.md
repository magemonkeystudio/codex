# Commands

Codex registers one main command plus a standalone `/stuck` command. The [[Chat Module]] and
[[Scoreboard Module]] add their own trees when enabled.

## `/codex`

The main command. Its labels come from `core.command-aliases` in [[Configuration]] — by default
`/codex`, `/codexengine`, and `/codexcore` all work.

Running `/codex` with no arguments shows the help output.

| Subcommand | Permission | Description |
|---|---|---|
| `/codex help` | `codexcore.user` | Lists available subcommands |
| `/codex reload` | `codexcore.admin` | Reloads configuration and language files |
| `/codex editor` | `codexcore.cmd.editor` | Opens the in-game editor GUI (players only) |
| `/codex about` | *none* | Shows plugin version and credits |

> The prefix is `codexcore.`, not `codex.` — the plugin's declared name is `CodexCore`. See
> [[Permissions]].

Running `/codex` bare shows help, so it requires `codexcore.user` like the explicit subcommand does.

### Availability notes

Two subcommands are conditional, which is why you may not see them:

- **`editor`** is only registered by plugins that declare an editor. The Codex engine itself does
  not, so `/codex editor` will not appear — but `/fabled editor` and similar will on plugins built
  on Codex that do. See [[Actions Engine]].
- **`about`** is registered only on *child* plugins, not on the engine. `/codex about` is therefore
  not available; `/fabled about` is.

This is the same command framework every Codex-based plugin uses, so `/fabled …` and `/divinity …`
expose the same `help` / `reload` / `editor` / `about` structure, with each plugin's own permission
prefix derived from its declared name.

---

## `/unstuck`

Teleports a player who is trapped in terrain back to a position they previously stood on.

```
/unstuck
```

- **Permission:** `codex.stuck` (granted to everyone by default)
- **Players only** — cannot be run from console

> ### ⚠️ `/stuck` does not work
>
> `plugin.yml` declares a `stuck` command, but nothing ever assigns it an executor — the runtime
> command is registered under the label `unstuck` only. Running `/stuck` therefore just prints its
> usage line and teleports nobody.
>
> On **Paper** it is worse: `paper-plugin.yml` has no `commands:` section at all, so `/stuck` does
> not exist as a command. `/unstuck` still works on both, because it is registered at runtime rather
> than through either descriptor.
>
> Tell your players `/unstuck`.

### Behaviour

1. The player runs `/unstuck`.
2. A warmup begins, lasting `unstuck.warmup` seconds (default 5). The player is told to stand still.
3. **If the player moves, the teleport is cancelled.**
4. On completion the player is teleported to a previously recorded position.
5. The cooldown — `unstuck.cooldown` seconds, default 30 — is applied only after a *completed*
   teleport, so a cancelled attempt does not consume it.

Both values are configurable — see [[Configuration]].

### Where it teleports you

Not to a computed safe spot. Codex records positions the player has stood on with solid ground
beneath, keeping up to ten, and `/unstuck` sends them to the **oldest** recorded position. A player
who has not yet moved enough to record any position has nowhere to be sent.

### Logging

Each accepted invocation is logged to console when the **warmup begins**:

```
STUCK - Notch executed '/stuck' at world,120.5,64.0,-88.2
```

Two caveats: the line is written before the teleport, so it appears even for attempts later
cancelled by movement; and it always says `'/stuck'` regardless of the label actually used.
Invocations rejected for cooldown or an in-progress warmup are not logged.

Codex also appends each use to `plugins/CodexCore/unstuck.log`, which is purged when it is three or more
days past its last purge.

---

## Module commands (deprecated)

> ⚠️ Both command trees below belong to the legacy **mccore** codebase and are **deprecated**. They
> are documented for servers already using them; new setups should use a dedicated chat or scoreboard
> plugin instead.

Available only when the corresponding feature is enabled in [[Configuration]].

| Command | Requires | Page |
|---|---|---|
| `/chat …` | `Features.chat-enabled: true` | [[Chat Module]] |
| `/board …` | `Features.scoreboards-enabled: true` | [[Scoreboard Module]] |

---

## Removed commands

**`/corereload` no longer exists.** It was the ProMCCore-era reload command. Use `/codex reload`.

See [[Permissions]] for the full node list.
