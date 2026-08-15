# Join and Interact Commands

Codex can run commands automatically when a player joins, when they join for the very first time, or
when they click a configured block. All three are configured in `config.yml` — see [[Configuration]].

All three use the same **delayed command** structure.

## The delayed command format

```yaml
- delay: 100
  as: CONSOLE
  cmd: give {player} cookie 1
```

| Field | Meaning |
|---|---|
| `delay` | Ticks to wait before running (20 ticks = 1 second). `0` runs immediately. |
| `as` | Who runs the command — `PLAYER`, `OP`, or `CONSOLE` |
| `cmd` | The command to run, **without** a leading `/` |

### The `as` values

| Value | Behaviour |
|---|---|
| `PLAYER` | Runs as the player, with the player's own permissions |
| `OP` | Temporarily grants op, runs the command, then removes op **if the player did not already have it** |
| `CONSOLE` | Runs from console |

> **Use `OP` sparingly.** It grants full operator status for the duration of the command. If the
> command opens a GUI or triggers something asynchronous, the elevated state can outlive what you
> intended. Prefer `CONSOLE` where possible.

### Placeholders

| Placeholder | Replaced with |
|---|---|
| `{player}` | The player's name |

If PlaceholderAPI is installed, its placeholders are also expanded. See [[Hooks]].

---

## `onJoin`

Runs **every time** a player joins.

```yaml
onJoin:
  - delay: 20
    as: CONSOLE
    cmd: msg {player} Welcome back!
```

Give the server a moment before firing anything that touches the player's inventory or screen — a
small delay (20–40 ticks) avoids racing with resource pack prompts and other join-time handlers.

## `onFirstJoin`

Runs **only on a player's first ever join**. Ideal for starter kits.

```yaml
onFirstJoin:
  - delay: 40
    as: CONSOLE
    cmd: give {player} bread 16
  - delay: 45
    as: CONSOLE
    cmd: broadcast &e{player} &7joined for the first time!
```

Both `onJoin` and `onFirstJoin` fire for a first-time player — `onFirstJoin` does not replace
`onJoin`.

---

## `onInteract`

Runs commands when a player clicks a configured block.

```yaml
onInteract:
  - material: CRAFTING_TABLE
    type: -1
    cancelAction: true
    delayedCommands:
      - delay: 0
        as: CONSOLE
        cmd: give {player} cookie 1
```

| Field | Meaning |
|---|---|
| `material` | Bukkit material name of the block |
| `type` | Legacy data value; use `-1` to match any |
| `cancelAction` | When `true`, suppresses the block's normal behaviour |
| `delayedCommands` | List of delayed commands, same format as above |

### `cancelAction`

With `cancelAction: true` on the crafting table example above, right-clicking gives the cookie and
the crafting GUI does **not** open. Set it to `false` to let the block behave normally as well.

### Bypass permission

Players with `core.oninteract.bypass` do not trigger these blocks at all. See [[Permissions]].

### Multiple entries

Add as many entries as you like. Each is matched independently, so several can fire from one click if
they target the same material.

```yaml
onInteract:
  - material: OAK_SIGN
    type: -1
    cancelAction: false
    delayedCommands:
      - delay: 0
        as: PLAYER
        cmd: warp spawn

  - material: BEACON
    type: -1
    cancelAction: true
    delayedCommands:
      - delay: 0
        as: CONSOLE
        cmd: effect give {player} minecraft:speed 30 1
```

---

## Troubleshooting

**Nothing fires.** Check the material name matches your server version exactly — material names
changed in 1.13 and occasionally since. Enable `debug: true` in [[Configuration]] to see what Codex
matched.

**Fires for staff when it shouldn't.** That is `core.oninteract.bypass` not being granted. See
[[Permissions]].

**Command runs but has no effect on join.** Increase the `delay`. A delay of `0` on join often runs
before the player is fully loaded into the world.
