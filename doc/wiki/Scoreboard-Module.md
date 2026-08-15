# Scoreboard Module

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

Board names passed to `/board show` may contain spaces — the remaining arguments are joined, so
`/board show My Server Stats` works without quoting.

## Cycling

When cycling is active, Codex rotates through the registered boards on a timer. `/board cycle` starts
the rotation and `/board stop` halts it, leaving the current board displayed. `/board toggle` hides
or shows the sidebar entirely without affecting which board is selected.

Cycling is driven by an internal task started when the module is enabled, so it continues across
player rejoins.

## Boards and their contents

Scoreboards are registered by Codex and by plugins built on it — for example a stats board fed by a
downstream plugin. `/board list` shows what is currently registered on your server, which will vary
depending on which Codex-based plugins you have installed.

The module distinguishes between:

- **Boards** — a named sidebar display
- **Stat boards** — boards whose lines are bound to values that update automatically

Both appear in `/board list`.

## Teams

The module also manages scoreboard teams, which is what allows name colouring and collision rules to
be applied consistently alongside the sidebar.
