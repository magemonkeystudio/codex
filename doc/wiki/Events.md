# Events

Codex fires several custom events that plugins can listen to like any Bukkit event.

## `ArmorEquipEvent`

Fires whenever a player equips or unequips a piece of armor. Bukkit has no built-in event for this,
and the ways armor can change are surprisingly varied — this event covers all of them.

```java
@EventHandler
public void onArmorEquip(ArmorEquipEvent event) {
    Player    player = event.getPlayer();
    ArmorType type   = event.getType();

    ItemStack oldPiece = event.getOldArmorPiece();
    ItemStack newPiece = event.getNewArmorPiece();

    if (type == ArmorType.HELMET && newPiece != null) {
        player.sendMessage("Nice hat.");
    }
}
```

**Cancellable.** Cancelling prevents the equip or unequip.

### `ArmorType`

| Value | Slot |
|---|---|
| `HELMET` | 39 |
| `CHESTPLATE` | 38 |
| `LEGGINGS` | 37 |
| `BOOTS` | 36 |
| `OFFHAND` | 40 |
| `MAIN_HAND` | — |

### `EquipMethod`

`event.getMethod()` tells you *how* the change happened:

| Method | Meaning |
|---|---|
| `SHIFT_CLICK` | Shift-clicking an armor piece |
| `DRAG` | Dragging the item into the slot |
| `PICK_DROP` | Manually placing or removing the item |
| `HOTBAR` | Right-clicking armor in the hotbar with the inventory closed |
| `HOTBAR_SWAP` | Pressing a hotbar number while hovering an armor slot |
| `DISPENSER` | A dispenser equipped the armor |
| `BROKE` | The piece lost all durability |
| `DEATH` | Death unequipped everything |
| `HELD_SLOT_CHANGE` | Changing held slot, altering the main-hand item |

Filtering on the method matters for stat plugins: recalculating on `DEATH` is usually wrong, and
`BROKE` needs handling if your bonuses are tied to the item.

> `DISPENSER` requires the server version to implement `BlockDispenseArmorEvent`.

### Modifying the event

```java
event.setNewArmorPiece(modifiedItem);
event.setOldArmorPiece(otherItem);
```

---

## Packet events

Codex's packet layer fires events for intercepted traffic.

| Event | Fired for |
|---|---|
| `EnginePacketEvent` | Base type for all packet events |
| `EnginePlayerPacketEvent` | Packets associated with a player |
| `EngineServerPacketEvent` | Server-level packets |

```java
@EventHandler
public void onPacket(EnginePlayerPacketEvent event) {
    // inspect or cancel the packet
}
```

Packet events fire on network threads. **Do not touch the Bukkit API directly from a packet
handler** — schedule work back onto the main thread:

```java
Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage("..."));
```

Keep handlers short; they run for every matching packet.

---

## Event interfaces

Codex's own events implement two small interfaces:

| Interface | Purpose |
|---|---|
| `IEvent` | Marker for Codex events |
| `ICancellableEvent` | Codex events that can be cancelled |

---

## Listening

Standard Bukkit registration:

```java
Bukkit.getPluginManager().registerEvents(new MyListener(), plugin);
```

If your listener belongs to a menu, register it through the menu instead so it is cleaned up
automatically — see [[Menus and GUIs]]:

```java
registerListener(myListener);
```

Codex also provides `AbstractListener` and `IListener` as base types if you want lifecycle handling
tied to a manager.
