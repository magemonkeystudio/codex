# Codex

**Codex** (formerly *ProMCCore*) is the core library plugin for the MageMonkey Studio / VoidEdge
plugin suite. It is a dependency plugin: on its own it provides a handful of utility features, but
its main job is to give downstream plugins such as **Fabled** and **Divinity** a shared foundation —
version compatibility, menus, hooks, an actions engine, item providers, and configuration handling.

[![Discord](https://dcbadge.limes.pink/api/server/mQrkW4htUA?style=flat)](https://discord.gg/mQrkW4htUA)

---

## For server owners

| Page | What's in it |
|---|---|
| **[[Installation]]** | Requirements, supported servers, optional dependencies |
| **[[Configuration]]** | Every key in `config.yml`, explained |
| **[[Commands]]** | The full command tree |
| **[[Permissions]]** | Every permission node |
| **[[Join and Interact Commands]]** | `onJoin`, `onFirstJoin`, `onInteract` |
| **[[Localization]]** | Language files and message placeholders |
| **[[Version Support]]** | Which Minecraft versions are supported |
| **[[Chat Module]]** ⚠️ | *Deprecated* — player prefixes and display names |
| **[[Scoreboard Module]]** ⚠️ | *Deprecated* — scoreboard cycling and display |

## For developers

| Page | What's in it |
|---|---|
| **[[Developer Getting Started]]** | Maven coordinates, extending `CodexPlugin` |
| **[[Actions Engine]]** | The YAML-driven action/condition/target system |
| **[[Item Providers]]** | Cross-plugin item resolution (Oraxen, Nexo, ItemsAdder) |
| **[[Menus and GUIs]]** | Building paged inventory menus |
| **[[Compat and NMS]]** | Writing version-independent code |
| **[[Hooks]]** | Vault, WorldGuard, MythicMobs, Citizens, Nexo |
| **[[Events]]** | Custom events Codex fires |
| **[[Utilities]]** | The `*UT` helper classes |

---

## Quick start

1. Download Codex and drop it in `plugins/`.
2. Start the server once to generate `plugins/CodexCore/config.yml`.
3. Install the plugins that depend on Codex (Fabled, Divinity, …).

Codex loads at server **startup** (before worlds load), because dependent plugins need its
compatibility layer available very early. See [[Installation]] for details.

## A note on the names

Three names are in play, and they differ on purpose:

| | |
|---|---|
| **Codex** | The project |
| **CodexCore** | The Bukkit plugin name — the data folder, `depend:` entry, and permission prefix |
| **codex** | The Maven artifactId |

So the config lives at `plugins/CodexCore/config.yml`, permissions read `codexcore.admin`, and Maven
wants `studio.magemonkey:codex`.

Codex was previously called **ProMCCore** and still declares `provides: ProMCCore`, so plugins
depending on the old name resolve against it. On first startup it **automatically renames** an
existing `plugins/ProMCCore/` folder to `plugins/CodexCore/`. Remove the old `ProMCCore.jar` before
starting — running both causes duplicate class conflicts. Back up first; see [[Installation]].

## Contributing

Source lives at [magemonkeystudio/codex](https://github.com/magemonkeystudio/codex). Issues and pull
requests are welcome.

<a href="https://github.com/magemonkeystudio/codex/graphs/contributors">
<img src="https://contrib.rocks/image?repo=magemonkeystudio/codex" />
</a>
