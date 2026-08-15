# Localization

Codex keeps its user-facing strings in language files under `plugins/Codex/lang/`.

## Selecting a language

```yaml
core:
  lang: en
```

Codex ships two files:

| Value | File |
|---|---|
| `en` | `lang/messages_en.yml` |
| `cn` | `lang/messages_cn.yml` |

To add your own, copy an existing file to `messages_<code>.yml` and set `core.lang` to that code.

> The English file is deliberately small — it contains only the strings Codex overrides. Any message
> not present falls back to its built-in default. The Chinese file is much larger because it
> translates the full set. Both are valid; you do not need to fill in every key.

## Placeholder syntax

Codex messages use **`$<name>`** placeholders, not `%name%`:

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

Standard `&` codes work everywhere. Hex colours are also supported in the form `&#RRGGBB`:

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

Keep the `|2-` indicator and the relative indentation when editing, or YAML parsing will fail and
Codex will fall back to defaults.

## Message types

Some messages support a type prefix controlling *how* they are delivered — as chat, a title, or an
action bar — along with title timing arguments:

| Argument | Meaning |
|---|---|
| `type` | Delivery method |
| `prefix` | Whether to include the plugin prefix |
| `fadeIn` / `stay` / `fadeOut` | Title timings, in ticks |

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

```
/codex reload
```

Reloads language files alongside the main config. Requires `codex.admin` — see [[Commands]].

If a language file contains a YAML syntax error, the reload reports an error and the previous strings
stay loaded. Check the console for the parse failure.
