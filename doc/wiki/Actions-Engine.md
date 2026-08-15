# Actions Engine

The actions engine is Codex's YAML-driven system for describing "pick some targets, check some
conditions, do some things." Plugins built on Codex use it to let server owners configure effects
without writing code.

It has four moving parts:

```
target-selectors  →  who does this affect?
conditions        →  should it happen at all?
action-executors  →  what happens?
params            →  the settings on each of the above
```

## YAML structure

```yaml
actions:
  my-section:
    target-selectors:
      - 'RADIUS ~distance: 5; ~allow-self: false;'
    conditions:
      list:
        - 'PERMISSION ~name: myplugin.special;'
      actions-on-fail: 'denied-section'
    action-executors:
      - 'MESSAGE ~message: &aYou triggered it!;'
      - 'POTION ~name: SPEED; ~duration: 100; ~amount: 1;'

  denied-section:
    action-executors:
      - 'MESSAGE ~message: &cYou lack permission.;'
```

A file can hold as many named sections as you like. Section names are case-insensitive.

## Parameter syntax

Every selector, condition, and action takes parameters in the same form:

```
TYPE ~flag: value; ~flag: value;
```

- The type is uppercase.
- Each parameter starts with `~`, then the flag name, a colon, the value, and a **terminating
  semicolon**.
- The trailing `;` is required — omitting it is the most common mistake.

---

## Target selectors

Determine which entities the actions apply to.

| Selector | Parameters | Description |
|---|---|---|
| `SELF` | — | The executor only |
| `RADIUS` | `~distance:` `~allow-self:` `~attackable:` | Everything within a radius |
| `FROM_SIGHT` | `~distance:` `~allow-self:` `~attackable:` | What the executor is looking at |

```yaml
target-selectors:
  - 'SELF'
  - 'RADIUS ~distance: 8; ~allow-self: false; ~attackable: true;'
```

`~attackable: true` filters to entities the executor is actually allowed to damage, which respects
WorldGuard regions and excludes Citizens NPCs when those hooks are present. See [[Hooks]].

You can also name a selector group:

```yaml
target-selectors:
  - 'RADIUS ~distance: 5; ~name: nearby;'
```

---

## Conditions

Checked before the actions run. If a condition fails, the section named by `actions-on-fail` runs
instead — leave it empty to simply do nothing.

| Condition | Key parameters | Checks |
|---|---|---|
| `PERMISSION` | `~target:` `~name:` | Target holds a permission node |
| `WORLD_TIME` | | Current world time |
| `VAULT_BALANCE` | | Economy balance, via Vault |
| `ENTITY_HEALTH` | | Target's health |
| `ENTITY_TYPE` | | Target's entity type |

```yaml
conditions:
  list:
    - 'PERMISSION ~name: myplugin.vip;'
    - 'ENTITY_HEALTH ~amount: 10;'
  actions-on-fail: 'not-allowed'
```

`VAULT_BALANCE` requires Vault to be installed; without it the condition cannot pass.

---

## Action executors

The full set of actions:

| Action | Purpose |
|---|---|
| `MESSAGE` | Send a chat message to targets |
| `BROADCAST` | Broadcast to the server |
| `ACTION_BAR` | Send an action bar message |
| `TITLES` | Send title / subtitle |
| `PROGRESS_BAR` | Display a progress bar |
| `SOUND` | Play a sound |
| `PARTICLE_SIMPLE` | Spawn particles |
| `FIREWORK` | Spawn a firework |
| `LIGHTNING` | Strike lightning |
| `DAMAGE` | Damage targets |
| `HEALTH` | Modify health |
| `BURN` | Set targets on fire |
| `POTION` | Apply a potion effect |
| `HUNGER` | Modify hunger |
| `SATURATION` | Modify saturation |
| `TELEPORT` | Teleport targets |
| `THROW` | Throw targets |
| `PROJECTILE` | Launch a projectile |
| `COMMAND_PLAYER` | Run a command as the player |
| `COMMAND_CONSOLE` | Run a command from console |
| `COMMAND_OP` | Run a command with temporary op |
| `HOOK` | Invoke a hooked plugin, e.g. MythicMobs |
| `GOTO` | Jump to another action section |

### `GOTO`

`GOTO` chains sections together, which is how you build branching behaviour:

```yaml
actions:
  entry:
    conditions:
      list:
        - 'PERMISSION ~name: myplugin.vip;'
      actions-on-fail: 'normal'
    action-executors:
      - 'GOTO ~name: vip-reward;'

  vip-reward:
    action-executors:
      - 'MESSAGE ~message: &6VIP reward!;'

  normal:
    action-executors:
      - 'MESSAGE ~message: &7Standard reward.;'
```

> Watch for loops. A section that `GOTO`s back into itself will recurse.

---

## Parameter reference

| Flag | Type | Used by |
|---|---|---|
| `~amount:` | number | `DAMAGE`, `HEALTH`, `POTION`, `HUNGER`, … |
| `~delay:` | number | Most actions |
| `~distance:` | number | `RADIUS`, `FROM_SIGHT` |
| `~duration:` | number | `POTION`, `BURN`, … |
| `~speed:` | number | `PROJECTILE`, `THROW` |
| `~message:` | string | `MESSAGE`, `BROADCAST`, `ACTION_BAR` |
| `~name:` | string | `PERMISSION`, `POTION`, `SOUND`, `GOTO` |
| `~target:` | string | Selects which target group to act on |
| `~filter:` | boolean | Filtering behaviour |
| `~allow-self:` | boolean | `RADIUS`, `FROM_SIGHT` |
| `~attackable:` | boolean | `RADIUS`, `FROM_SIGHT` |
| `~location:` | location | `TELEPORT`, `PARTICLE_SIMPLE` |
| `~offset:` | offset | Positional offset |
| `~title:` | string | `TITLES` |
| `~subtitle:` | string | `TITLES` |
| `~fadeIn:` `~stay:` `~fadeOut:` | number | `TITLES` timings, in ticks |
| `~color-empty:` `~color-fill:` | string | `PROGRESS_BAR` |

Parameters not applicable to an action are ignored.

## Message placeholders

Inside `~message:` and similar string parameters:

| Placeholder | Replaced with |
|---|---|
| `%executor%` | Name of the entity that triggered the section |
| `%target%` | Name of the entity currently being acted on |

PlaceholderAPI placeholders are expanded as well when it is installed.

---

## The in-game editor

Codex ships a GUI editor for action sections, so server owners can build them without hand-editing
YAML. Plugins that declare an editor expose it through their own command:

```
/<plugin> editor
```

Requires `<plugin>.cmd.editor` — see [[Permissions]]. The Codex engine itself does not register an
editor, so `/codex editor` is not available; plugins built on it, such as Fabled, do.

The editor walks the same structure described above: sections, then target selectors, conditions, and
action executors within each.

---

## For plugin developers

Load a set of sections from a config file:

```java
ActionManipulator actions = new ActionManipulator(plugin, cfg, "actions");
```

`cfg` is a Codex `JYML` config and `"actions"` is the path holding your named sections.

Register your own action types, conditions, target selectors, or params through the actions manager:

```java
CodexEngine.get().getActionsManager();
```

`ActionManipulator` also supports `replace(UnaryOperator<String>)`, returning a copy with a
substitution applied across all executors — useful for injecting per-invocation values before running
a section.

### Format migration

Older configs stored `target-selectors` as a map of named lists. Codex migrates these to the current
flat-list form automatically on load, rewriting the file in place and appending `~name:` to preserve
the original group names. No action is needed on your part, but do not be surprised to see the file
change shape after an upgrade.
