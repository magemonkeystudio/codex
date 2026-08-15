# Compat and NMS

Codex's compatibility layer is what lets a single plugin jar run from 1.16.5 through 1.21.11 and
26.2. If you are writing a plugin on Codex, **read this before calling any Bukkit API that has
changed across versions.**

## The three entry points

```java
NMS       nms   = VersionManager.getNms();
Compat    compat = VersionManager.getCompat();
ArmorUtil armor  = VersionManager.getArmorUtil();
```

Never import or reference a `codex-nms-v1_XX_X` class directly. The correct implementation is chosen
at startup based on the running server; hard-coding one guarantees a crash on every other version.

---

## `Compat` — API shims

`Compat` wraps Bukkit APIs whose *signatures* changed, so your code compiles once and runs everywhere.

### Inventory access

`InventoryView` was a **class** before 1.21 and is an **interface** from 1.21 onward. Code compiled
against one form throws `NoSuchMethodError` on the other. Use these instead:

```java
Inventory top    = compat.getTopInventory(event);
Inventory top2   = compat.getTopInventory(player);
Inventory bottom = compat.getBottomInventory(event);

ItemStack item   = compat.getItem(event, slot);
Inventory inv    = compat.getInventory(event, slot);
int converted    = compat.convertSlot(event, slot);

compat.setItem(player, slot, item);
compat.setCursor(event, item);
```

This matters in [[Menus and GUIs]] — any click handler reaching for the raw inventory should go
through `Compat`.

### Attribute modifiers

Attribute modifier construction changed shape across versions. `Compat` normalises it:

```java
// Applies regardless of equipped slot
AttributeModifier mod = compat.createAttributeModifier(
        attribute, amount, AttributeModifier.Operation.ADD_NUMBER);

// Scoped to one equipment slot
AttributeModifier scoped = compat.createAttributeModifier(
        attribute, amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
```

The three-argument overload is the slot-agnostic form — equivalent to passing `null` as the slot.

Read a modifier's key back with:

```java
String key = compat.getAttributeKey(modifier);
String key2 = compat.getAttributeKey(nbtAttribute);
```

`Compat.ATTRIBUTE_BONUS_UUID` is the shared UUID Codex uses for its own bonus modifiers.

### Item names

```java
String name = compat.getItemName(item);
```

---

## `NMS` — low-level operations

`NMS` exposes things Bukkit does not.

| Method | Purpose |
|---|---|
| `getVersion()` | The resolved NMS version string |
| `getConnection(player)` | Player connection object |
| `getChannel(player)` | Netty channel, for packet interception |
| `sendPacket(player, packet)` | Send a raw packet |
| `openChestAnimation(block, open)` | Play the chest open/close animation |
| `sendAttackPacket(player, id)` | Send an attack animation |
| `fixColors(str)` | Version-correct colour handling |
| `getDefaultDamage(item)` | Base attack damage of an item |
| `getDefaultSpeed(item)` | Base attack speed |
| `getDefaultArmor(item)` | Base armor value |
| `getDefaultToughness(item)` | Base armor toughness |
| `isWeapon(item)` / `isArmor(item)` / `isTool(item)` | Item classification |
| `toJson(item)` | Serialise an item to JSON |
| `getNMSCopy(item)` | The underlying NMS item stack |
| `setKiller(entity, player)` | Attribute a kill to a player |
| `changeSkull(block, hash)` | Set a skull block's texture |
| `getMaterial(boat)` | The material a boat is made of |

The `getDefault*` methods are useful for building item tooltips: they give you the values Minecraft
itself would apply before your plugin's modifiers.

---

## `ArmorUtil` and armor events

The armor utility backs `ArmorEquipEvent`, which fires whenever a player equips or unequips armor —
including cases Bukkit gives you no event for, such as dispensers and durability breaks. See
[[Events]].

---

## Guidance

**Do:**

- Go through `VersionManager` for anything version-sensitive.
- Test against both the oldest and newest supported versions — the 1.16/1.21 boundary is where most
  breakage lives.
- Use `Compat` inventory accessors in every inventory event handler.

**Don't:**

- Import `net.minecraft.*` or `org.bukkit.craftbukkit.*` directly.
- Reflect into NMS yourself — if Codex is missing something you need, adding it to the `NMS`
  interface benefits every plugin in the suite.
- Assume a method exists because it compiles. Compiling against 1.21 says nothing about 1.16.

## Adding support for a new Minecraft version

1. Add a `codex-nms-vX_XX_X` module, copying the closest existing one.
2. Update it against the new mappings.
3. Register it in `VersionManager`.
4. Add the module to `codex-nms/pom.xml`.

See [[Version Support]] for the current list.
