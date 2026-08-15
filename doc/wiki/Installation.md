# Installation

## Requirements

| | |
|---|---|
| **Server software** | Spigot, Paper, or a fork of either |
| **Minecraft versions** | 1.16.5 through 1.21.11, plus 26.2 — see [[Version Support]] |
| **Declared API version** | `1.16` on Spigot, `1.19` on Paper |

## Steps

1. Download `Codex.jar` and place it in your server's `plugins/` folder.
2. Start the server. Codex generates `plugins/Codex/config.yml` and its language files.
3. Stop the server, adjust [[Configuration]] to taste, and start again.
4. Install the plugins that depend on Codex.

Codex has no required dependencies. It will start and run on a bare server.

## Load order

Codex declares `load: STARTUP`, meaning it is enabled **before worlds are loaded**. This is
deliberate: the [[Compat and NMS]] layer has to be ready before dependent plugins initialise, and
some of them register world-related handlers during their own startup.

On Paper, Codex additionally declares `load-before` for **Fabled** and **Divinity**, guaranteeing it
is enabled ahead of them.

## Optional dependencies

All of these are *soft* dependencies. Codex detects them at runtime and enables the matching hook
only if the plugin is present — nothing breaks if they are absent. See [[Hooks]].

| Plugin | What Codex uses it for |
|---|---|
| **Vault** | Permission groups, prefixes/suffixes, economy balance checks |
| **WorldGuard** | Region flag checks, PvP/combat permission checks |
| **MythicMobs** | Mob identification and hook actions (both v4 and v5 APIs supported) |
| **Citizens** | NPC detection, so NPCs are excluded from targeting |
| **Nexo** | Custom item resolution |
| **PlaceholderAPI** | Placeholder expansion inside messages and actions |
| **Oraxen** | Custom item resolution |
| **ItemsAdder** | Custom item resolution |

> Oraxen and ItemsAdder support is provided through the item provider API. See [[Item Providers]].

## Bundled libraries

Codex downloads two libraries at runtime through Bukkit's `libraries` mechanism rather than shading
them:

- `com.mysql:mysql-connector-j`
- `org.apache.commons:commons-lang3`

The first server start therefore needs internet access to Maven Central. If your server is firewalled,
pre-seed the `libraries/` folder.

## Migrating from ProMCCore

Codex is the renamed continuation of **ProMCCore** and declares `provides: ProMCCore`, so any plugin
whose `depend`/`softdepend` still names `ProMCCore` will resolve against Codex.

To migrate:

1. Delete `ProMCCore.jar` from `plugins/`. **Do not run both** — they contain overlapping classes.
2. Drop in `Codex.jar`.
3. Codex uses its own data folder, `plugins/Codex/`. Your old `plugins/ProMCCore/` folder is not read
   or migrated automatically; copy settings across by hand using [[Configuration]] as a reference.

Note that the old `/corereload` command no longer exists. Use `/codex reload` — see [[Commands]].

## Paper vs Spigot

Codex ships both a `plugin.yml` and a `paper-plugin.yml`. On Paper the latter is used, which changes
a few things:

- Dependencies are declared with explicit `load: BEFORE` ordering.
- `has-open-classloader: true` is set, so dependent plugins can access Codex's classes.
- The declared API version is `1.19` rather than `1.16`.

Both paths register the same features; you do not need to configure anything differently.
