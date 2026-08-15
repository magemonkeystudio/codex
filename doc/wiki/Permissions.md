# Permissions

## Codex core

| Node | Default | Grants |
|---|---|---|
| `codex.user` | op | `/codex help` |
| `codex.admin` | op | `/codex reload` |
| `codex.cmd.editor` | op | `/codex editor` |
| `codex.stuck` | **everyone** | `/stuck` and `/unstuck` |
| `core.oninteract.bypass` | op | Exempts the holder from `onInteract` triggers |

`codex.stuck` is the only node granted to all players by default. Revoke it explicitly if you do not
want `/stuck` generally available:

```yaml
# In your permission plugin
codex.stuck: false
```

### `core.oninteract.bypass`

Players holding this node do **not** trigger `onInteract` command blocks. This is meant for staff, so
that admins clicking on configured blocks while building do not fire the attached commands. See
[[Join and Interact Commands]].

---

## Chat module

Active only when `Features.chat-enabled` is `true`. See [[Chat Module]].

| Node | Grants |
|---|---|
| `general.chat.list` | `/chat list` |
| `general.chat.name` | `/chat name` |
| `general.chat.prefix` | `/chat prefix` |
| `general.chat.reset` | `/chat reset` |

Grant the whole module with `general.chat.*`.

---

## Scoreboard module

Active only when `Features.scoreboards-enabled` is `true`. See [[Scoreboard Module]].

| Node | Grants |
|---|---|
| `general.board.cycle` | `/board cycle` |
| `general.board.list` | `/board list` |
| `general.board.show` | `/board show` |
| `general.board.stop` | `/board stop` |
| `general.board.toggle` | `/board toggle` |

Grant the whole module with `general.board.*`.

---

## How Codex builds permission nodes

Codex derives command permissions from the plugin's own name, lowercased with spaces and hyphens
stripped. For the engine that produces the `codex.` prefix:

| Command | Node pattern | Resolves to |
|---|---|---|
| `help` | `<plugin>.user` | `codex.user` |
| `reload` | `<plugin>.admin` | `codex.admin` |
| `editor` | `<plugin>.cmd.editor` | `codex.cmd.editor` |

The same pattern applies to every plugin built on Codex, so **Fabled** uses `fabled.admin`,
**Divinity** uses `divinity.admin`, and so on. If you are writing such a plugin, you get these nodes
for free — see [[Developer Getting Started]].

---

## Suggested group setup

**Default players**

```yaml
- codex.stuck
```

**Staff**

```yaml
- codex.stuck
- codex.user
- core.oninteract.bypass
- general.chat.*
- general.board.*
```

**Administrators**

```yaml
- codex.admin
- codex.cmd.editor
```

Note that `codex.admin` grants config reloading, and `codex.cmd.editor` grants access to the in-game
editor, which can rewrite plugin configuration files. Treat both as administrative.
