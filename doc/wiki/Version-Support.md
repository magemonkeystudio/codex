# Version Support

Codex ships a separate NMS implementation module per Minecraft version. The correct one is selected
at runtime by `VersionManager` during startup.

## Supported versions

| | |
|---|---|
| 1.16.5 | 1.20.6 |
| 1.17.1 | 1.21.1 |
| 1.18.2 | 1.21.2 |
| 1.19.4 | 1.21.4 |
| 1.20.1 | 1.21.5 |
| 1.20.2 | 1.21.7 |
| 1.20.4 | 1.21.10 |
| | 1.21.11 |
| | 26.2 |

Versions are matched to the closest supported implementation. A version not listed here — for example
a brand-new release that Codex has not been updated for yet — will fail to find an NMS implementation
and Codex will not start correctly.

## How version selection works

`VersionManager.setup()` runs during plugin load and resolves three things:

| Accessor | Purpose |
|---|---|
| `VersionManager.getNms()` | Low-level NMS operations |
| `VersionManager.getCompat()` | Version-independent API shims |
| `VersionManager.getArmorUtil()` | Armor equip/unequip detection |

Developers should never reference an NMS module directly. Go through these accessors — see
[[Compat and NMS]].

## Why so many modules

Minecraft's internal packages are remapped every version, and Mojang periodically changes public
Bukkit API types as well. Two examples Codex works around:

- **`InventoryView` became an interface in 1.21.** Code compiled against the old class fails at
  runtime on 1.21+. `Compat` provides inventory accessors that work on both.
- **Attribute modifiers changed shape.** `Compat.createAttributeModifier` normalises this.

## Version-related config

One setting exists specifically for older clients:

```yaml
action-bar-legacy: false
```

Set this to `true` if action bar messages do not render correctly on your server version. See
[[Configuration]].
