# Permissions

## Codex core

| Node | Default | Grants |
|---|---|---|
| `codexcore.user` | undeclared (op) | `/codex help` |
| `codexcore.admin` | undeclared (op) | `/codex reload` |
| `codexcore.cmd.editor` | undeclared (op) | `/codex editor` |
| `codex.stuck` | **everyone** | `/unstuck` |
| `general.oninteract.bypass` | undeclared (op) | Exempts the holder from `onInteract` triggers |

> ### ⚠️ The prefix is `codexcore.`, not `codex.`
>
> Command permissions are derived from the plugin's name, and the plugin is named **CodexCore** —
> `plugin.yml` uses `name: ${project.name}`, and the Maven module's `<name>` is `CodexCore`. So the
> derived nodes are `codexcore.user`, `codexcore.admin`, and `codexcore.cmd.editor`.
>
> `codex.stuck` is the exception: it is a hardcoded literal, not derived, so it really is `codex.`

### Declared vs undeclared

Only `codex.stuck` is actually declared, in both `plugin.yml` and `paper-plugin.yml`, with
`default: true`. Nothing else is declared in either file or registered programmatically.

The other four fall through to Bukkit's default for unregistered permissions, which is op-only. That
usually produces the behaviour you want, but it is a fallback rather than a stated default —
permission plugins that do not consult Bukkit's fallback may treat them differently. Grant them
explicitly rather than relying on op.

`codex.stuck` is the only node granted to all players by default. Revoke it explicitly if you do not
want `/unstuck` generally available:

```yaml
# In your permission plugin
codex.stuck: false
```

### `general.oninteract.bypass`

Players holding this node do **not** trigger `onInteract` command blocks. This is meant for staff, so
that admins clicking on configured blocks while building do not fire the attached commands. See
[[Join and Interact Commands]].

> ⚠️ The comment in the shipped `config.yml` names this `core.oninteract.bypass`. That comment is
> wrong — the node the code checks is `general.oninteract.bypass`.

Individual `onInteract` entries can also carry their own `permission` option, checked in addition to
the bypass node.

---

## Chat module (deprecated)

> ⚠️ Part of the legacy **mccore** codebase. Deprecated — see [[Chat Module]].

Active only when `Features.chat-enabled` is `true`.

| Node | Grants |
|---|---|
| `general.chat.list` | `/chat list` |
| `general.chat.name` | `/chat name` |
| `general.chat.prefix` | `/chat prefix` |
| `general.chat.reset` | `/chat reset` |

These are complete node strings, not suffixes.

> Codex does not register `general.chat.*` as a parent permission, and Bukkit will not expand it.
> Whether a wildcard grant works depends entirely on your permission plugin — LuckPerms expands
> wildcards at query time, others do not. Grant the four nodes individually if yours does not.

---

## Scoreboard module (deprecated)

> ⚠️ Part of the legacy **mccore** codebase. Deprecated — see [[Scoreboard Module]].

Active only when `Features.scoreboards-enabled` is `true`.

| Node | Grants |
|---|---|
| `general.board.cycle` | `/board cycle` |
| `general.board.list` | `/board list` |
| `general.board.show` | `/board show` |
| `general.board.stop` | `/board stop` |
| `general.board.toggle` | `/board toggle` |

These are complete node strings, not suffixes. The same wildcard caveat as above applies.

---

## How Codex builds permission nodes

Codex derives command permissions from the plugin's own name, lowercased with spaces and hyphens
stripped. Because the engine's plugin name is **CodexCore**, that produces the `codexcore.` prefix:

| Command | Node pattern | Resolves to |
|---|---|---|
| `help` | `<plugin>.user` | `codexcore.user` |
| `reload` | `<plugin>.admin` | `codexcore.admin` |
| `editor` | `<plugin>.cmd.editor` | `codexcore.cmd.editor` |

The same pattern applies to every plugin built on Codex, so a plugin whose declared name is `Fabled`
gets `fabled.admin`. Note the transformation only removes spaces and hyphens — it does not shorten
the name, which is exactly why the engine's nodes are `codexcore.` and not `codex.` Check your own
plugin's declared `name:` rather than assuming. See [[Developer Getting Started]].

---

## Suggested group setup

**Default players**

```yaml
- codex.stuck
```

**Staff**

```yaml
- codex.stuck
- codexcore.user
- general.oninteract.bypass
```

Add `general.chat.*` and `general.board.*` only if you are still running the deprecated chat and
scoreboard modules.

**Administrators**

```yaml
- codexcore.admin
- codexcore.cmd.editor
```

Note that `codexcore.admin` grants config reloading, and `codexcore.cmd.editor` grants access to the
in-game editor, which can rewrite plugin configuration files. Treat both as administrative.
