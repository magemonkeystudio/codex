# Version Support

Codex ships a separate NMS implementation per Minecraft version, selected at runtime by
`VersionManager`.

## Supported versions

Matching is **exact**. Each running server version is looked up in a hardcoded list — there is no
nearest-version fallback. Some versions share one implementation:

| Server version | Implementation |
|---|---|
| 1.16.5 | `v1_16_5` |
| 1.17, 1.17.1 | `v1_17` |
| 1.18.2 | `v1_18_2` |
| 1.19.4 | `v1_19_4` |
| 1.20.1 | `v1_20_1` |
| 1.20.2 | `v1_20_2` |
| 1.20.3, 1.20.4 | `v1_20_4` |
| 1.20.5, 1.20.6 | `v1_20_6` |
| 1.21, 1.21.1 | `v1_21_1` |
| 1.21.2, 1.21.3 | `v1_21_2` |
| 1.21.4 | `v1_21_4` |
| 1.21.5 | `v1_21_5` |
| 1.21.6, 1.21.7, 1.21.8 | `v1_21_7` |
| 1.21.9, 1.21.10 | `v1_21_10` |
| 1.21.11 | `v1_21_11` |
| 26.1.2, 26.2 | `v26_2` |

## Explicitly unsupported versions

These fall **inside** the range above but are rejected deliberately, with a message telling you to
upgrade:

`1.18` · `1.18.1` · `1.19` · `1.19.1` · `1.19.2` · `1.19.3` · `1.20`

Only the newest patch of each minor line is supported. If you are on one of these, moving to the
latest patch of the same minor version (1.18.2, 1.19.4, 1.20.1) is enough.

## What happens on an unsupported version

Codex **disables itself**. `VersionManager` throws `UnsupportedVersionException`, the engine logs a
`SEVERE` error with a stack trace, and Bukkit disables the plugin. Every plugin depending on Codex
then fails to enable too.

> On newer Paper builds the failure can surface earlier and less helpfully, as an
> `ExceptionInInitializerError` from the internal `Version` enum, before the friendlier "please
> upgrade" message is reached. If you see that, check your server version against the table above
> first.

## How version selection works

`VersionManager.setup()` runs during `onEnable` (with `load: STARTUP`, still before worlds load) and
resolves three accessors:

| Accessor | Purpose |
|---|---|
| `VersionManager.getNms()` | Low-level NMS operations |
| `VersionManager.getCompat()` | Version-independent API shims |
| `VersionManager.getArmorUtil()` | Armor **trim** helpers |

Developers should never reference an NMS module directly — go through these. See
[[Compat and NMS]].

> `getArmorUtil()` silently returns a **no-op implementation** on versions before 1.19.4, since those
> modules ship no `ArmorUtilImpl`. Trim calls succeed and do nothing rather than failing.

## Why so many modules

Minecraft's internal packages are remapped every version, and Mojang periodically changes public
Bukkit API types too. Two Codex works around:

- **`InventoryView` became an interface in 1.21.** Code compiled against the old class fails at
  runtime on 1.21+. `Compat` provides accessors that work on both.
- **Attribute modifier construction changed shape.** `Compat.createAttributeModifier` normalises it.

Note the module directory and the Java package do not always match — `codex-nms-v1_17_1` contains
package `v1_17`.

## Version-related config

```yaml
action-bar-legacy: false
```

Set to `true` if action bar messages do not render correctly on your server version — it switches
from Adventure to the Spigot action bar path. See [[Configuration]].
