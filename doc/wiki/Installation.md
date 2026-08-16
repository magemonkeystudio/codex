# Installation

## Requirements

| | |
|---|---|
| **Server software** | Spigot, Paper, or a fork of either |
| **Minecraft versions** | Selected releases from 1.16.5 to 1.21.11, plus 26.1.2 and 26.2 |
| **Declared API version** | `1.16` on Spigot, `1.19` on Paper |

> Not every version in that span is supported — 1.18, 1.18.1, 1.19–1.19.3 and 1.20 are explicitly
> rejected, and an unsupported version makes Codex disable itself. Check the exact list in
> [[Version Support]] before upgrading.

## Steps

1. Download the Codex jar (built as `codex-<version>.jar`) into your server's `plugins/` folder.
2. Start the server. Codex generates `plugins/CodexCore/config.yml` and its language files.
3. Stop the server, adjust [[Configuration]] to taste, and start again.
4. Install the plugins that depend on Codex.

> **The plugin registers itself as `CodexCore`, not `Codex`.** That is the name Bukkit knows it by,
> the name of its data folder, and the prefix on its permission nodes. The Maven artifact is `codex`
> — the two deliberately differ. See [[Permissions]] and [[Developer Getting Started]].

Codex has no required dependencies. It will start and run on a bare server.

## Load order

Codex declares `load: STARTUP`, so it is enabled **before worlds are loaded**. The
[[Compat and NMS]] layer has to be ready before dependent plugins initialise.

`paper-plugin.yml` also carries a `load-before` block naming Fabled and Divinity. Be aware this block
is very likely inert — `load-before` is not part of Paper's schema (ordering there is expressed
through `dependencies.server.<Plugin>.load: BEFORE`), and the entries misspell `bootstrap`. In
practice ordering rests on `load: STARTUP` and each dependent plugin's own `depend`.

## Optional dependencies

None are required. They divide into two groups, which behave differently at load time.

### Declared soft-dependencies

Load order is guaranteed relative to these:

| Plugin | What Codex uses it for |
|---|---|
| **Vault** | Permission groups, player prefixes/suffixes, economy access |
| **WorldGuard** | Region lookup and PvP/combat permission checks |
| **MythicMobs** | Mob identification (both v4 and v5 APIs, selected automatically) |
| **Oraxen** | Custom item resolution |

### Detected at runtime only

Not declared anywhere, so load order is not guaranteed. Codex compensates by completing hook setup
when it sees the plugin enable later:

| Plugin | What Codex uses it for |
|---|---|
| **Citizens** | NPC trait registration and NPC click events; NPCs are also excluded from combat targeting |
| **Nexo** | Custom item resolution |
| **PlaceholderAPI** | Placeholder expansion in the actions engine and GUI items |
| **ItemsAdder** | Custom item resolution |

> Oraxen, Nexo, and ItemsAdder resolve items through the provider API — see [[Item Providers]]. Note
> the Oraxen provider is marked deprecated for removal in source, though it is still registered.
>
> A quirk worth knowing: the Vanilla, Oraxen, and ItemsAdder providers are registered **whether or
> not** those plugins are installed. Availability is checked lazily at lookup time. Only Nexo is
> registered conditionally.

**Sentinel** is a further undeclared integration: combat checks consult `SentinelTrait` to decide
whether an NPC may be attacked.

## Bundled libraries

Codex downloads two libraries at runtime through Bukkit's `libraries` mechanism rather than shading
them:

- `com.mysql:mysql-connector-j`
- `org.apache.commons:commons-lang3`

The first server start therefore needs access to Maven Central. If your server is firewalled,
pre-seed the `libraries/` folder.

## Migrating from ProMCCore

Codex is the renamed continuation of **ProMCCore** and declares `provides: ProMCCore`, so any plugin
whose `depend`/`softdepend` still names `ProMCCore` resolves against Codex.

**The data folder is migrated for you.** On first startup Codex renames `plugins/ProMCCore` to
`plugins/CodexCore` and rewrites `Core:` keys to `CodexCore:` in `lang/messages_en.yml`. A legacy
`plugins/Codex` folder is migrated the same way.

To migrate:

1. **Back up your `plugins/` folder.** Migration failures are logged only as a `WARNING`, so a
   partial migration is easy to miss.
2. Delete `ProMCCore.jar`. **Do not run both** — they contain overlapping classes.
3. Drop in the Codex jar and start the server.
4. Check the console for `Migrating ProMCCore to CodexCore` and confirm `plugins/CodexCore/` looks
   right.

Note that `/corereload` no longer exists. Use `/codex reload` — see [[Commands]], and read the
caveat there about what reload actually does.

## Paper vs Spigot

Codex ships both a `plugin.yml` and a `paper-plugin.yml`. On Paper the latter is used, which changes
a few things:

- The four soft-dependencies are declared with explicit `load: BEFORE` ordering.
- `has-open-classloader: true` is set, so dependent plugins can access Codex's classes.
- The declared API version is `1.19` rather than `1.16`.
- **There is no `commands:` section**, so the (already non-functional) `/stuck` label does not exist
  at all on Paper. `/unstuck` works on both, because it is registered at runtime. See [[Commands]].

Otherwise both paths register the same features.
