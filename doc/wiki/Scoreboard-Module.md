# Scoreboard Module

> ## ⚠️ Deprecated
>
> The scoreboard module is part of the legacy **mccore** codebase, inherited from ProMCCore. It is
> **deprecated and not recommended for new setups.** Several of its internal classes are already
> marked `@Deprecated` in source. It still works and is documented here for servers already relying
> on it, but it is not actively developed and may be removed in a future release.
>
> **Use a dedicated scoreboard plugin instead.** Set `Features.scoreboards-enabled: false` in
> [[Configuration]] to disable this module.

Codex includes an optional scoreboard module that manages sidebar scoreboards and can cycle between
several of them automatically.

**Enable or disable it** in `config.yml` (see [[Configuration]]):

```yaml
Features:
  scoreboards-enabled: true
```

> Disable this if another plugin owns the sidebar. Minecraft allows only one sidebar objective per
> player, so two scoreboard plugins will fight over it.

## Commands

| Command | Permission | Description |
|---|---|---|
| `/board list` | `general.board.list` | Displays a list of active scoreboards |
| `/board show <boardName>` | `general.board.show` | Shows a specific scoreboard |
| `/board cycle` | `general.board.cycle` | Starts cycling through scoreboards |
| `/board stop` | `general.board.stop` | Stops cycling |
| `/board toggle` | `general.board.toggle` | Toggles scoreboard visibility |

See [[Permissions]] for granting these.

Board names passed to `/board show` may contain spaces — the remaining arguments are joined, so no
quoting is needed. Two caveats, both bugs:

- **The name must be all lowercase.** The lookup compares against a lowercased value without
  lowercasing your argument, so `/board show My Server Stats` fails where `my server stats` matches.
- **It always reports failure.** `/board show` prints "You do not have a scoreboard with that name"
  even on success — the board does switch, the message is just wrong.

## Cycling

**Cycling is on by default** — a player's board state is created with the flag already set, so
`/board cycle` on a fresh player just replies that it is already cycling. `/board stop` halts it,
leaving the current board displayed. `/board toggle` hides or shows the sidebar entirely without
affecting which board is selected.

Two fixed, non-configurable timers drive this: the cycle runs every 200 ticks (10 seconds), and stat
boards refresh every 10 ticks (0.5 seconds), updating only the active board.

> The timers survive a rejoin, but **per-player state does not** — quitting or being kicked clears
> the player's boards, current selection, and cycling flag. On rejoin they have no boards until a
> downstream plugin re-registers them.

## Boards and their contents

> ⚠️ **Codex registers no boards of its own.** On a stock install there is nothing to show, and every
> `/board` command is effectively a no-op. Boards come entirely from downstream plugins.

`/board list` shows the boards attached to **the player running it**, not a server-wide registry.

The module distinguishes between:

- **Boards** — a named sidebar display
- **Stat boards** — boards whose lines are bound to values that update automatically

Both appear in `/board list`.

## Teams

The module also manages scoreboard teams, which is what allows name colouring and collision rules to
be applied consistently alongside the sidebar.
