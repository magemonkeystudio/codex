# Menus and GUIs

Codex provides an inventory menu framework with paging, click routing, and lifecycle cleanup. You
subclass `Menu`, fill slots with `Slot` objects, and Codex handles the rest.

## A minimal menu

```java
public class MyMenu extends Menu {

    public MyMenu(Player player) {
        super(player, 3, "&8My Menu");   // 3 rows; colour codes and hex work
    }

    @Override
    public void setContents() {
        setSlot(11, new Slot(new ItemStack(Material.DIAMOND)) {
            @Override
            public void onLeftClick() {
                getPlayer().sendMessage("You clicked the diamond!");
            }
        });

        setSlot(15, new Slot(new ItemStack(Material.BARRIER)) {
            @Override
            public void onLeftClick() {
                close();
            }
        });
    }
}
```

```java
new MyMenu(player).open();
```

`setContents()` is called on open **and on every page change** — build your slots there, not in the
constructor.

## Menu API

| Method | Purpose |
|---|---|
| `open()` / `open(int page)` | Open, optionally at a page |
| `openSync()` | Open on the next server tick |
| `openSubMenu(Menu)` | Open a child, remembering this as parent |
| `close()` | Close (the parent reopens after a tick, if there is one) |
| `close(int layers)` | Schedule *n* `closeInventory()` calls, one per tick |
| `onClose()` | Close hook — **call `super.onClose()`** |
| `fakeClose()` | Close the inventory without running close handling |
| `setSlot(int, Slot)` | Place a slot; `null` removes it |
| `getSlot(int)` | Retrieve by absolute index |
| `getSlotOnCurrentPage(int)` | Retrieve by index within the visible page |
| `clearSlots()` | Remove all slots |
| `getPages()` / `getPage()` | Page count; current page (**zero-based**) |
| `isOpening()` | Whether a menu transition is in progress |
| `registerListener` / `unregisterListener` | Per-menu listeners |
| `registerTask` / `unregisterTask` | Per-menu tasks |
| `Menu.getOpenMenu(player)` | *Static* — the player's open menu, or `null` |

The title supports `%page%` and `%pages%` placeholders, substituted on open. Both are **one-based**,
unlike `getPage()`.

## Paging

Slot indices are **absolute**, not per-page. A 3-row menu holds 27 slots, so slot 30 lives on page 2.

```java
@Override
public void setContents() {
    List<String> entries = loadEntries();
    for (int i = 0; i < entries.size(); i++) {
        setSlot(i, new Slot(iconFor(entries.get(i))) { ... });
    }
}
```

> ### ⚠️ Two paging bugs to work around
>
> **`getPages()` under-counts by one** when the highest slot index is an exact multiple of the page
> size. Exactly 28 entries in a 3-row menu reports 1 page, and `open()`'s `page % getPages()` then
> pins you to page 0 — the last entry is unreachable. Pad the final page or leave a gap until this is
> fixed.
>
> **`PreviousPageButton` on page 0 renders a blank menu.** It calls `open(getPage() - 1)`, and
> `-1 % n` stays negative in Java, so every slot lookup goes out of range. `NextPageButton` wraps
> correctly; only the backwards direction is affected.

### Navigation buttons

```java
setSlot(18, new Menu.PreviousPageButton(new ItemStack(Material.ARROW)));
setSlot(26, new Menu.NextPageButton(new ItemStack(Material.ARROW)));
```

Because indices are absolute, that places buttons on **page 1 only**. For navigation on every page,
place them per page:

```java
for (int p = 0; p < pageCount; p++) {
    setSlot(p * 27 + 18, new Menu.PreviousPageButton(arrow));
    setSlot(p * 27 + 26, new Menu.NextPageButton(arrow));
}
```

> **Use `getSlotOnCurrentPage`, not `getSlot`, for raw inventory positions.** The index Bukkit gives
> you in a click event is relative to the visible page; `getSlot` expects an absolute index. Mixing
> them is the classic "works on page 1, wrong item on page 2" bug — fixed in `MenuManager` itself by
> commit `02fcff9`, with a regression test in `MenuTest`.

## Click handling

Override only what you need; the rest fall back.

| Method | Fallback |
|---|---|
| `onLeftClick()` | — |
| `onRightClick()` | `onLeftClick()` |
| `onShiftLeftClick()` | `onLeftClick()` |
| `onShiftRightClick()` | `onRightClick()` |
| `onMiddleClick()` | `onLeftClick()` |
| `onNumberClick(int)` | `onLeftClick()` |
| `onDoubleClick()` | `onLeftClick()` |
| `onDrop()` | `onLeftClick()` |
| `onControlDrop()` | `onDrop()` |
| `onSwapOffhand()` | `onLeftClick()` |

Overriding only `onLeftClick()` makes a slot respond to every click type.

`getItemStack()` returns a **clone**, so callers cannot mutate your icon.

`MenuManager` cancels all clicks and drags inside menu inventories, and cancels shift-clicks from the
player inventory into a menu — items cannot be moved in or out.

## Sub-menus and closing

```java
openSubMenu(new MyOtherMenu(getPlayer()));
```

`close()` does not "return" to the parent so much as close and then reopen it a tick later — expect a
brief flicker and a fresh `setContents()` call on the parent.

`close(int layers)` schedules that many bare `closeInventory()` calls on consecutive ticks to punch
through stacked menus. It does **not** consult the parent chain, and the timing interacts with the
deferred `onClose()`, so treat it as best-effort rather than a guarantee.

## Listeners and tasks

```java
registerListener(myListener);
registerTask(Bukkit.getScheduler().runTaskTimer(plugin, this::refresh, 20L, 20L));
```

Both are torn down when the menu closes and on player quit — the recommended way to build
live-updating menus.

> If you override `onClose()`, **call `super.onClose()`**. The base implementation is what unregisters
> listeners, cancels tasks, removes the player from the active-menu map, and reopens the parent.
>
> Cleanup is not absolute: a quit during a `fakeClose()` window leaves an entry in the static
> active-menu map, and the quit path can reopen a parent menu for a departing player.

## Related classes

| Class | Purpose |
|---|---|
| `MenuManager` | Routes click/drag/close/quit events to menus |
| `YAMLMenu<T>` | **Not** a `Menu` subclass — a config-backed template that populates one via `setSlots(Menu, T)` |
| `YAMLListMenu<T>` | Extends `YAMLMenu` for list-shaped data |
| `FileExplorerMenu` | Browse files in-game; used by editor GUIs |
| `JIcon` | Icon builder — material, name, lore, enchanted, click |
| `ContentType` | Enum: `NEXT`, `BACK`, `EXIT`, `NONE`, `RETURN`, `ACCEPT`, `DECLINE` |
| `GuiClick` | Functional interface — `click(Player, Enum<?>, InventoryClickEvent)` |

`YAMLMenu` also exposes `reloadMenus(Plugin)` and abstract `getTitle` / `getSlot` for subclasses.

## Inventory access on 1.21+

If you need the raw inventory from an event, go through `Compat` rather than
`event.getView().getTopInventory()` — `InventoryView` changed from a class to an interface in 1.21.
`MenuManager` does this internally. See [[Compat and NMS]].
