# Menus and GUIs

Codex provides an inventory menu framework with automatic paging, click routing, and lifecycle
cleanup. You subclass `Menu`, fill slots with `Slot` objects, and Codex handles the rest.

## A minimal menu

```java
public class MyMenu extends Menu {

    public MyMenu(Player player) {
        super(player, 3, "&8My Menu");   // 3 rows, colour codes supported
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

Open it:

```java
new MyMenu(player).open();
```

`setContents()` is called when the menu opens and on each page change — build your slots there, not
in the constructor, so paging works.

## Menu API

| Method | Purpose |
|---|---|
| `open()` | Open at the current page |
| `open(int page)` | Open at a specific page |
| `openSync()` | Open on the next server tick |
| `openSubMenu(Menu)` | Open a child menu, remembering this one as parent |
| `close()` | Close the menu |
| `close(int layers)` | Close this menu and *n* parent levels |
| `fakeClose()` | Close the inventory without running close handling |
| `onClose()` | Override to react to the menu closing |
| `setSlot(int, Slot)` | Place a slot; `null` removes it |
| `getSlot(int)` | Retrieve a slot by absolute index |
| `getSlotOnCurrentPage(int)` | Retrieve by index within the visible page |
| `clearSlots()` | Remove all slots |
| `getPages()` | Number of pages |
| `getPage()` | Current page (zero-based) |
| `getPlayer()` | The viewing player |
| `Menu.getOpenMenu(player)` | *Static* — the player's currently open menu, or `null` |

## Paging

Paging is implicit. Slot indices are **absolute**, not per-page: a 3-row menu holds 27 slots, so slot
`30` lives on page 2. `getPages()` derives the page count from the highest index used.

```java
@Override
public void setContents() {
    List<String> entries = loadEntries();
    for (int i = 0; i < entries.size(); i++) {
        setSlot(i, new Slot(iconFor(entries.get(i))) { ... });
    }
}
```

That code pages automatically no matter how many entries there are.

Codex supplies ready-made navigation buttons:

```java
setSlot(18, new Menu.PreviousPageButton(new ItemStack(Material.ARROW)));
setSlot(26, new Menu.NextPageButton(new ItemStack(Material.ARROW)));
```

> **Use `getSlotOnCurrentPage`, not `getSlot`, when handling raw inventory positions.** The index
> Bukkit gives you in a click event is relative to the visible page; `getSlot` expects an absolute
> index. Mixing the two is the classic source of "works on page 1, wrong item on page 2" bugs.

## Click handling

`Slot` exposes a method per click type. Override only what you need — the rest fall back sensibly.

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

Overriding only `onLeftClick()` therefore makes a slot respond to every click type — which is usually
what you want for a simple button.

`getItemStack()` returns a **clone**, so callers cannot mutate your icon by accident.

## Sub-menus

```java
openSubMenu(new MyOtherMenu(getPlayer()));
```

The child remembers its parent, so `close()` returns to the parent rather than closing everything.
Use `close(2)` to close both.

## Listeners and tasks

Register per-menu listeners and tasks and Codex tears them down when the menu closes:

```java
registerListener(myListener);
registerTask(Bukkit.getScheduler().runTaskTimer(plugin, this::refresh, 20L, 20L));
```

This is the recommended way to build live-updating menus — no manual cleanup, and nothing leaks if
the player disconnects with the menu open.

## Related classes

| Class | Purpose |
|---|---|
| `MenuManager` | Routes inventory events to menus; handles close and quit cleanup |
| `YAMLMenu` / `YAMLListMenu` | Menus defined from YAML configuration |
| `FileExplorerMenu` | Browse files in-game, used by editor GUIs |
| `JIcon` | Icon builder |
| `ContentType` / `GuiClick` | Editor GUI content and click descriptors |

The `MenuManager` is available from the engine:

```java
MenuManager menus = CodexEngine.get().getMenuManager();
```

You do not normally interact with it directly — creating a `Menu` is enough.

## Inventory access on 1.21+

If you need the raw inventory from an event, go through `Compat` rather than
`event.getView().getTopInventory()`. `InventoryView` changed from a class to an interface in 1.21 and
direct access breaks across versions. See [[Compat and NMS]].
