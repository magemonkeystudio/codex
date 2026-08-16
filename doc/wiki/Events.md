# Events

Codex fires several custom events that plugins can listen to like any Bukkit event.

## `ArmorEquipEvent`

Fires when a player equips or unequips armor. Bukkit has no built-in event for this, and the ways
armor can change are surprisingly varied — this event covers most of them.

> ### Its scope is wider than "armor"
>
> `ArmorType.matchType` returns `MAIN_HAND` for **any** non-air, non-armor item, which is why the
> event also fires for ordinary items on held-slot changes and item consumption. It also maps
> `ELYTRA` → `CHESTPLATE`, `SHIELD` → `OFFHAND`, and any `*_SKULL` / `*_HEAD` → `HELMET`.
>
> Codex only fires the event when the piece actually changed (compared with `isSimilar`), so no-op
> swaps are silent.

Fired by `ArmorListener` in `codex-plugin` — not by the NMS `ArmorUtil`, which handles armor trims
and is unrelated. See [[Compat and NMS]].

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

**Cancellable — with one exception.** Cancelling a `BROKE` event throws
`UnsupportedOperationException`; you cannot veto a durability break.

### `ArmorType`

| Value | Slot |
|---|---|
| `HELMET` | 39 |
| `CHESTPLATE` | 38 |
| `LEGGINGS` | 37 |
| `BOOTS` | 36 |
| `OFFHAND` | 40 |
| `MAIN_HAND` | -1 |

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
| `HELD_SLOT_CHANGE` | Changing held slot, altering the main-hand item |
| `DROP` | Using the drop key (Q by default) |
| `CONSUME` | Consuming a held item |
| `DEATH` | **Declared but never fired** — no death handler exists |

Filtering on the method matters for stat plugins: `BROKE` needs handling if your bonuses are tied to
the item, and `DROP`/`CONSUME`/`HELD_SLOT_CHANGE` fire for ordinary items, not just armor.

> `DISPENSER` requires the server version to implement `BlockDispenseArmorEvent`.
>
> Do not write logic against `DEATH` — the constant exists but nothing emits it.

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
| `EnginePacketEvent` | Abstract base type — never fired directly |
| `EnginePlayerPacketEvent` | **Outbound** packets to a player — the only one actually fired |
| `EngineServerPacketEvent` | Declared but never constructed anywhere |

> Only **server → client** packets are intercepted. The injected handler overrides `write()` and not
> `channelRead()`, so inbound client packets produce no event at all.

```java
@EventHandler
public void onPacket(EnginePlayerPacketEvent event) {
    Object packet = event.getPacket();   // opaque NMS object
    Player p = event.getReciever();      // note the spelling in the API
    // event.setPacket(...) to replace, or cancel to drop the packet entirely
}
```

`PacketManager.registerHandler(IPacketHandler)` is the alternative extension point if you would
rather not use `@EventHandler`.

These are **asynchronous** Bukkit events, fired from the netty pipeline thread. **Do not touch the
Bukkit API directly from a packet handler** — schedule work back onto the main thread:

```java
Bukkit.getScheduler().runTask(plugin, () -> player.sendMessage("..."));
```

Keep handlers short; they run for every matching packet.

---

## Event base classes

`IEvent` and `ICancellableEvent` are **abstract classes**, not interfaces — events extend them.

| Class | Purpose |
|---|---|
| `IEvent` | Extends Bukkit's `Event`; supplies a shared `HandlerList`, a `final getHandlers()`, and an `(boolean async)` constructor |
| `ICancellableEvent` | Adds cancellation on top of `IEvent` |

Only the packet events use this hierarchy. **`ArmorEquipEvent` does not** — it extends `PlayerEvent`
and implements `Cancellable` with its own `HandlerList`.

> Every `IEvent` descendant shares a single static `HandlerList`, so a listener registered for any one
> of them is walked for all of them. Given these fire per outbound packet, keep such handlers cheap.

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

Codex also provides two base types for lifecycle handling tied to a manager. Note the naming is the
reverse of what you would expect: **`AbstractListener` is the interface** (extending `Listener`, with
`registerListeners()` and a default `unregisterListeners()`), and **`IListener<P>` is the abstract
class** implementing it.
