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

| Field | Meaning | Default if omitted |
|---|---|---|
| `delay` | Ticks to wait **after the previous command in the list ran** (20 ticks = 1 second). `0` runs immediately. | `0` |
| `as` | Who runs the command — `PLAYER`, `OP`, or `CONSOLE` | `CONSOLE` |
| `cmd` | The command to run, **without** a leading `/` | *mandatory* |

> ### ⚠️ Delays are cumulative, not absolute
>
> Each entry is scheduled only once the previous one has run, so delays add up down the list. Two
> entries with `delay: 40` and `delay: 45` fire at tick 40 and tick **85**, not 40 and 45.

> **`cmd` is mandatory** — omitting it throws at plugin enable. And `as` is matched
> **case-sensitively** against the exact enum name, falling back to the default on no match. `as:
> player` silently becomes `CONSOLE`, which is not a harmless difference. Always write it uppercase.

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

> `{player}` is the **only** placeholder supported here. PlaceholderAPI is *not* expanded in join or
> interact commands — it applies to the [[Actions Engine]] and to GUI items, not to this feature.

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

| Field | Meaning | Default if omitted |
|---|---|---|
| `material` | Bukkit material name of the block | — |
| `type` | Legacy data value — **currently ignored** | `-1` |
| `cancelAction` | When `true`, suppresses the block's normal behaviour | `true` |
| `delayedCommands` | List of delayed commands, same format as above | *mandatory* |

> `type` is parsed but never consulted; matching is by `material` alone. Leave it at `-1`.
>
> `material` is matched **case-sensitively** against the exact enum name. A mistyped or lowercase
> value silently becomes `AIR` and never matches — this is the usual cause of "nothing fires".

> ### ⚠️ There is no click-type filter
>
> `onInteract` fires on left-click and physical (pressure-plate) interactions as well as right-click.
> With `cancelAction: true` that means **block breaking is cancelled too**, not just the right-click
> behaviour described below.

### `cancelAction`

With `cancelAction: true` on the crafting table example above, right-clicking gives the cookie and
the crafting GUI does **not** open. Set it to `false` to let the block behave normally as well.

### Bypass permission

Players with `general.oninteract.bypass` do not trigger these blocks at all. See [[Permissions]].

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

**Nothing fires.** Almost always a case-sensitivity problem: `material` and `as` are matched against
exact uppercase enum names, and a mismatch fails silently (`material` becomes `AIR`, `as` becomes
`CONSOLE`). Check spelling against your server version too — material names changed in 1.13 and
occasionally since.

> `debug: true` will not help here. Despite the name, it gates a legacy logger with essentially no
> call sites left; neither the interact listener nor the command block logs anything.

**Fires for staff when it shouldn't.** That is `general.oninteract.bypass` not being granted. See
[[Permissions]].

**Command runs but has no effect on join.** Increase the `delay`. A delay of `0` on join often runs
before the player is fully loaded into the world.
