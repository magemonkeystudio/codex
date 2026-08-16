# Actions Engine

The actions engine is Codex's YAML-driven system for "pick some targets, check some conditions, do
some things." Plugins built on Codex use it to let server owners configure effects without writing
code.

```
target-selectors  →  who does this affect?
conditions        →  should it happen at all?
action-executors  →  what happens?
```

## YAML structure

```yaml
actions:
  my-section:
    target-selectors:
      - 'RADIUS ~distance: 5; ~allow-self: false; ~name: nearby;'
    conditions:
      list:
        - 'PERMISSION ~target: nearby; ~name: myplugin.special;'
      actions-on-fail: 'denied'
    action-executors:
      - 'MESSAGE ~target: nearby; ~message: &aYou triggered it!;'
      - 'POTION ~target: nearby; ~name: SPEED; ~duration: 100; ~amount: 2;'

  denied:
    target-selectors:
      - 'SELF ~name: me;'
    action-executors:
      - 'MESSAGE ~target: me; ~message: &cYou lack permission.;'
```

A file can hold as many named sections as you like. Section names are case-insensitive. Calling the
engine without naming a section runs the **first section in file order**.

## Parameter syntax

```
TYPE ~flag: value; ~flag: value;
```

- The **trailing `;` is required** on every parameter. This is the most common mistake.
- Whitespace after the colon is insignificant — the value is trimmed.
- **Type names are case-insensitive.** Uppercase is conventional.
- **Flag names are case-sensitive and must be lowercase** — see the warning below.
- `[TYPE] ~flag: value;` with brackets is equally valid, and is the form the GUI editor writes.

> ### ⚠️ Flags are lowercase, always
>
> Flag matching is case-sensitive against an internally lowercased name. Anything you write with a
> capital letter is **silently ignored** — no error, no warning, the parameter just does not apply.
>
> This bites hardest on the title timings, which read naturally as camelCase: write `~fadein:` and
> `~fadeout:`, **not** `~fadeIn:` / `~fadeOut:`.
>
> Confusingly, the message-type blocks in [[Localization]] use the same `~flag: value;` syntax with a
> *different* parser that **does** expect `~fadeIn:` / `~fadeOut:`. Do not copy lines between them.

## Targets are mandatory, and they are not inherited

Most actions require a `~target:` naming a selector group. Without one the action logs
"No Target specified" and does nothing.

Name a group with `~name:` on the selector, then reference it:

```yaml
target-selectors:
  - 'RADIUS ~distance: 8; ~name: nearby;'
action-executors:
  - 'MESSAGE ~target: nearby; ~message: hi;'
```

`~target:` accepts a comma-separated list of group names. A selector with no `~name:` lands in the
group `default`.

> **Group names must be lowercase.** `~target:` values are lowercased before lookup, but group names
> are stored exactly as written — so a group declared `~name: Nearby;` can never be selected.

> ### ⚠️ `GOTO` and `actions-on-fail` do not carry targets across
>
> A section reached by `GOTO`, or via `conditions.actions-on-fail`, starts with an **empty** target
> map. It must declare its own `target-selectors` or every action in it will fail. This is why the
> `denied` section in the example above has its own `SELF` selector.

---

## Target selectors

| Selector | Parameters | Description |
|---|---|---|
| `SELF` | `~name:` | The executor only |
| `RADIUS` | `~name:` `~distance:` `~allow-self:` `~attackable:` | Everything within a radius |
| `FROM_SIGHT` | `~name:` `~distance:` `~allow-self:` `~attackable:` | What the executor is looking at |

`RADIUS` and `FROM_SIGHT` **select nothing** without a `~distance:` greater than zero.

`~allow-self: true` *adds* the executor to the group even if it was not otherwise selected.

`~attackable: true` filters to entities the executor may actually damage. That check also excludes
self-damage, invulnerable entities, and non-living entities; it delegates to WorldGuard when present
and defaults to permitting damage when it is absent. Citizens NPCs are excluded **unless** Sentinel is
installed and the NPC carries `SentinelTrait`. See [[Hooks]].

---

## Conditions

Checked before the actions run. On the first failure, the section named by `actions-on-fail` runs
instead — leave it empty to do nothing.

| Condition | Key parameters | Checks |
|---|---|---|
| `PERMISSION` | `~target:` `~name:` | Target holds a permission node |
| `WORLD_TIME` | `~target:` `~name:` `~amount:` | World time (`~name:` is the world) |
| `VAULT_BALANCE` | `~target:` `~amount:` | Economy balance, via Vault |
| `ENTITY_HEALTH` | `~target:` `~amount:` | Target's health |
| `ENTITY_TYPE` | `~target:` `~name:` | Target's entity type |

Every condition also accepts `~message:` — the text shown to targets when it fails — and `~filter:`.

### Comparison operators

Number parameters accept a leading `<`, `>`, or `=`. **With no operator the comparison is `EQUALS`,
not a threshold** — `~amount: 10;` means "exactly 10".

```yaml
- 'ENTITY_HEALTH ~target: nearby; ~amount: <10;'    # below 10
- 'ENTITY_HEALTH ~target: nearby; ~amount: <50%;'   # below 50% of max
```

A trailing `%` switches `ENTITY_HEALTH` to a percentage of maximum health.

### `~filter:`

- `~filter: true;` — targets failing the check are **removed from the group**, and the condition
  still passes.
- Absent or `false` — **all** targets must satisfy the check or the whole section fails.

### `PERMISSION` negation

Prefix the node with `-` to pass when the target *lacks* it:

```yaml
- 'PERMISSION ~target: nearby; ~name: -myplugin.exempt;'
```

> ### ⚠️ `VAULT_BALANCE` fails open
>
> Without Vault installed, the condition cannot evaluate — Codex logs an error and treats it as
> **passed**. A balance gate silently becomes a no-op rather than blocking. The same happens if
> `~amount:` is missing.

---

## Action executors

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
| `THROW` | Push targets away from the executor |
| `HOOK` | Pull targets toward the executor |
| `PROJECTILE` | Launch a projectile |
| `COMMAND_PLAYER` | Run a command as the player |
| `COMMAND_CONSOLE` | Run a command from console |
| `COMMAND_OP` | Run a command with temporary op |
| `GOTO` | Jump to another action section |

> `HOOK` is a **velocity action** — the inverse of `THROW`, yanking targets toward the executor. It
> has nothing to do with plugin hooks.

### The `COMMAND_*` actions take the command in `~message:`

Not an obvious mapping, so worth stating plainly:

```yaml
- 'COMMAND_CONSOLE ~target: nearby; ~message: give %target% bread 1;'
```

### `GOTO`

```yaml
actions:
  entry:
    target-selectors:
      - 'SELF ~name: me;'
    conditions:
      list:
        - 'PERMISSION ~target: me; ~name: myplugin.vip;'
      actions-on-fail: 'normal'
    action-executors:
      - 'GOTO ~name: vip-reward;'

  vip-reward:
    target-selectors:
      - 'SELF ~name: me;'
    action-executors:
      - 'MESSAGE ~target: me; ~message: &6VIP reward!;'

  normal:
    target-selectors:
      - 'SELF ~name: me;'
    action-executors:
      - 'MESSAGE ~target: me; ~message: &7Standard reward.;'
```

> **There is no loop protection.** A section that `GOTO`s itself recurses until `StackOverflowError`.

---

## Parameter reference

| Flag | Type | Used by |
|---|---|---|
| `~delay:` | number | **All actions** (ticks) — reschedules the action |
| `~target:` | string | All actions and conditions; comma-separated group names |
| `~name:` | string | All target selectors; `PERMISSION`, `ENTITY_TYPE`, `WORLD_TIME`, `POTION`, `SOUND`, `PROJECTILE`, `PARTICLE_SIMPLE`, `GOTO` |
| `~message:` | string | `MESSAGE`, `BROADCAST`, `ACTION_BAR`, the three `COMMAND_*` actions, and **all conditions** (failure text) |
| `~amount:` | number | `DAMAGE`, `HEALTH`, `POTION`, `HUNGER`, `SATURATION`, `PARTICLE_SIMPLE`, and conditions `ENTITY_HEALTH`, `VAULT_BALANCE`, `WORLD_TIME` |
| `~duration:` | number | `POTION`, `BURN`, `PROGRESS_BAR` |
| `~distance:` | number | `RADIUS`, `FROM_SIGHT` |
| `~speed:` | number | `PROJECTILE`, `PARTICLE_SIMPLE` |
| `~filter:` | boolean | Conditions |
| `~allow-self:` | boolean | `RADIUS`, `FROM_SIGHT` |
| `~attackable:` | boolean | `RADIUS`, `FROM_SIGHT` |
| `~location:` | location | `TELEPORT` |
| `~offset:` | offset | `PARTICLE_SIMPLE` — `x,y,z` doubles |
| `~title:` `~subtitle:` | string | `TITLES`, `PROGRESS_BAR` |
| `~fadein:` `~stay:` `~fadeout:` | number | `TITLES` timings, in ticks — **lowercase** |
| `~color-empty:` `~color-fill:` | string | `PROGRESS_BAR` |

Parameters not applicable to an action are ignored.

### Units and quirks

- `~delay:` is in **ticks**.
- `POTION ~amount:` is the **level**, not the amplifier — amplifier is `amount - 1`. `~duration:` is
  in ticks and must be greater than zero.
- `PROGRESS_BAR ~duration:` is ticks divided by 20, so anything below 20 is a no-op. Its `~subtitle:`
  is the **character the bar is drawn from**, not a subtitle.
- `TELEPORT ~location:` is `world,x,y,z` and supports `%executor.world%` / `%executor.x%` / … and the
  same for `%target.…%`.

---

## Message placeholders

| Placeholder | Available in |
|---|---|
| `%executor%` | `MESSAGE`, `ACTION_BAR`, `BROADCAST`, `COMMAND_PLAYER`, `COMMAND_CONSOLE`, `COMMAND_OP` |
| `%target%` | The same list **except `BROADCAST`**, which has no targets |

These are not general — `TITLES`, `PROGRESS_BAR`, `SOUND` and the rest substitute neither.

PlaceholderAPI is expanded only when it is installed **and the executor is a player**. Expansion is
applied to the entire line before parsing, not just to string parameters.

---

## The in-game editor

Plugins that declare an editor expose it through their own command:

```
/<plugin> editor
```

Requires `<plugin>.cmd.editor` — see [[Permissions]]. The Codex engine registers no editor, so
`/codex editor` does not exist; plugins built on it, such as Fabled, do.

> The editor writes `target-selectors`, `conditions.list` and `action-executors`, but never
> `conditions.actions-on-fail`. Its output for the titles and progress-bar parameters also does not
> currently parse back correctly — check those by hand after editing in game.

---

## For plugin developers

Load a set of sections from a config file:

```java
ActionManipulator actions = new ActionManipulator(plugin, cfg, "actions");
```

`cfg` is a Codex `JYML` and `"actions"` is the path holding your named sections.

| Method | Purpose |
|---|---|
| `new ActionManipulator(plugin, cfg, path)` | Load from config |
| `new ActionManipulator(plugin, other)` | Copy constructor (deep-copies all lists) |
| `process(entity)` | Run the first section |
| `process(entity, sectionId)` | Run a named section |
| `replace(UnaryOperator<String>)` | Return a copy with a substitution applied to every line |
| `ActionManipulator.processConditions(plugin, exec, list)` | Evaluate conditions standalone |

Register your own types through the actions manager:

```java
ActionsManager manager = CodexEngine.get().getActionsManager();
// registerExecutor / registerCondition / registerTargetSelector / registerParam
```

`registerManipulator` / `getManipulator` / `unregisterManipulator` are also available.

> Parsed parameter results are held in a **static, process-wide cache keyed by the full line**. It is
> cleared by `Parametized.clearCache()`, which runs on plugin reload — if you build lines
> dynamically, be aware they accumulate.

### Format migration

Older configs stored `target-selectors` as a map of named lists. Codex migrates these to the current
flat-list form on load, appending `~name: <groupId>;` to preserve the group names and rewriting the
file in place. No action needed, but do not be surprised when the file changes shape after an upgrade.
