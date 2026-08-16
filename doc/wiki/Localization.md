# Localization

Codex keeps its user-facing strings in language files under `plugins/CodexCore/lang/`.

## ⚠️ Two message systems share one file

This is the thing to understand before editing anything. A Codex language file contains strings
served by **two independent engines** with different placeholder syntax, different fallback rules,
and different loaders:

| Keys | Engine | Placeholders | Language-aware? |
|---|---|---|---|
| `general.*` | `MessageUtil` | `$<name>` | **No** — always `messages_en.yml` |
| everything else (`Prefix`, `Codex.*`, `Error.*`, `Time.*`, the name tables) | `ILangMsg` | `%name%` | Yes, via `general.lang` |

They do not interoperate. A `%placeholder%` in a `general.*` string is literal text, and a `$<...>`
in an `ILangMsg` string is likewise inert.

## Selecting a language

> ### ⚠️ Language selection is effectively broken
>
> The key that picks the file is **`general.lang`**, which is not present in the shipped
> `config.yml`. The documented `core.lang` is read into a field with no consumers, so setting it does
> nothing.
>
> Adding `general: {lang: cn}` by hand does switch the `ILangMsg` half. The `general.*` half is
> loaded from a hardcoded `messages_en.yml` path and can never be localised.

```yaml
general:
  lang: cn
```

Codex ships two files:

| Value | File |
|---|---|
| `en` | `lang/messages_en.yml` |
| `cn` | `lang/messages_cn.yml` |

> The two shipped files are near-disjoint rather than large and small versions of each other.
> `messages_en.yml` (25 lines) holds *only* `custom-placeholders` and `general:` — the `$<>` set.
> `messages_cn.yml` (2028 lines) holds *only* the `%…%` set, with no `general:` section at all. The
> Chinese file is also partly stale: its `CodexCore:` root does not match any current key path, and
> it is missing the `Codex.Editor.*` keys.

On first run Codex appends every missing `ILangMsg` default to the file on disk, including full
entity, material, potion, and enchantment name tables — so the 25-line shipped file becomes very
large once generated. Missing `%…%` keys fall back to their built-in defaults; missing `$<>` keys do
**not** — they render as the literal key path, e.g. `general.notAPlayer`.

## Placeholder syntax

The `general.*` set uses **`$<name>`**:

```yaml
general:
  noPermissions: "&cYou don't have permissions ($<permission>)!"
  notAPlayer: "&cCan't find player named: $<name>!"
  notANumber: "&c\"$<text>\" it's not a number!"
```

Which placeholders are available depends on the message. Keep the ones present in the default string
— removing a placeholder is fine, but inventing a new one will render literally.

Note this is separate from the `{player}` style used by [[Join and Interact Commands]] and from
PlaceholderAPI's `%…%` placeholders.

## Colour codes

Standard `&` codes work everywhere. Hex colours are supported as `&#RRGGBB` — but the leading `&` is
**optional** in the pattern, so a bare `#1a2b3c` anywhere in a message is silently treated as a
colour too. Watch for that if your text legitimately contains a `#` followed by six hex digits.

```yaml
general:
  reload: "&#00ff88Reloaded settings!"
```

## Custom placeholders

The top of the language file defines reusable values you can reference from other messages:

```yaml
custom-placeholders:
  my-color: '#ff0000'
  my-text: 'Sample Text'
```

Reference them wrapped in percent signs — `%my-color%`, `%my-text%` — and note they are substituted
only into `%…%`-style messages, never into `general.*` `$<>` strings.

Define your own here to keep a colour or phrase consistent across many messages — change it once and
every message using it updates.

## Multi-line messages

YAML block scalars are used for messages spanning several lines:

```yaml
general:
  commands:
    help: |2-
      You typed: &7$<text>&r
      Valid syntax:
        &8/&7codex &rreload
```

`|2-` is an ordinary YAML block scalar — a plain `|-`, `|`, or a YAML list works identically. For
`%…%` messages, lines are split on the literal two-character sequence `\n` rather than real newlines.

A YAML parse error does **not** fall back to defaults: the main language config throws and the plugin
is disabled, while a broken `general.*` load leaves every `$<>` message rendering as its raw key.

## Message types

`%…%` messages — and only those — can carry a `{message: …}` block controlling *how* they are
delivered. Each argument is written `~name: value;`, and the whole block is stripped from the output:

```yaml
Codex:
  Example: '{message: ~type: TITLES; ~fadeIn: 10; ~stay: -1; ~fadeOut: 10;}&aHello'
```

| Argument | Meaning |
|---|---|
| `type` | `CHAT`, `ACTION_BAR`, `TITLES`, or `NONE` (suppresses the message entirely) |
| `prefix` | Boolean; only has effect in `CHAT` mode |
| `fadeIn` / `stay` / `fadeOut` | Title timings, in ticks — a `stay` below 0 is coerced to 10000 |

> ⚠️ **Here the timing flags are camelCase** — `~fadeIn:`, `~fadeOut:`. The [[Actions Engine]] uses
> identical-looking `~flag: value;` syntax but a different parser that requires them **lowercase**
> (`~fadein:`, `~fadeout:`). Copying a line between the two will silently drop the timings.

## World names

World display names are configured in `config.yml` rather than the language file, since they are
per-server rather than per-language:

```yaml
locale:
  world-names:
    world: 'World'
    world_nether: 'Nether'
```

See [[Configuration]].

## Reloading

> ⚠️ **`/codex reload` does not re-read language files.** The config objects are built once at
> startup, and the engine's reload only re-applies what is already in memory. Edit your language file
> and **restart the server**. See [[Configuration]].

The command itself requires `codexcore.admin` — see [[Permissions]].
