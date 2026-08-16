# Item Providers

Codex provides a single way to reference an item regardless of which plugin defines it. A config
value like `ORAXEN_ruby_sword` resolves to the right `ItemStack` whether the item comes from vanilla,
Oraxen, Nexo, or ItemsAdder — and the plugin asking for it does not need to know which.

## The ID format

```
NAMESPACE_id
```

| Namespace | Source plugin | What `id` looks like |
|---|---|---|
| `VANILLA` | Vanilla Minecraft materials | Material name |
| `ORAXEN` | Oraxen (provider is `@Deprecated(forRemoval)`) | Plain item id |
| `NEXO` | Nexo | Plain item id |
| `ITEMSADDER` | ItemsAdder | **`ia_namespace:item`** — see below |

```
DIAMOND_SWORD                    → vanilla diamond sword
ORAXEN_ruby_sword                → Oraxen item "ruby_sword"
NEXO_magic_wand                  → Nexo item "magic_wand"
ITEMSADDER_myitems:cool_hat      → ItemsAdder item "cool_hat" in namespace "myitems"
```

### ⚠️ ItemsAdder carries its own namespace

ItemsAdder organises items into its own namespaces, and Codex uses ItemsAdder's *namespaced* ID as
the item id. That means an ItemsAdder key has **two** namespaces stacked:

```
ITEMSADDER_myitems:ruby_sword
└────┬───┘ └──┬──┘ └────┬────┘
  Codex    ItemsAdder  item
 namespace  namespace
```

`ITEMSADDER_ruby_sword` will **not** resolve — the ItemsAdder namespace is required. If you are
unsure what it is, it is the same `namespace:id` ItemsAdder itself uses in `/iaget` and in its
configuration.

Oraxen and Nexo have no equivalent concept; their ids are flat.

### ⚠️ The namespace prefix must be UPPERCASE

Provider lookup is **case-sensitive**. `ORAXEN_ruby_sword` resolves; `oraxen_ruby_sword` does not —
it falls through to the vanilla provider, which then fails to find a material by that name.

This trips people up because prefix *stripping* inside each provider is case-insensitive, and
`hasProvider` uppercases its argument — only the lookup itself is strict. Always write the namespace
in capitals.

(One exception explains inconsistent reports: `vanilla_diamond_sword` *does* work. It fails the
lookup, falls through to the vanilla provider with the full key, and gets stripped
case-insensitively there.)

## For server owners

Wherever a Codex-based plugin asks for an item, you can use any of the forms above, provided the
source plugin is installed. If it is not, Codex reports a missing provider rather than silently
substituting something else.

Vanilla items need no prefix, so a plain `DIAMOND_SWORD` always works with no extra plugins. A
`VANILLA_` prefix is accepted but redundant. Vanilla lookups are also forgiving about separators —
spaces and hyphens are converted to underscores, so `diamond sword` and `diamond-sword` both resolve.

---

## For developers

### Resolving an item

```java
CodexItemManager items = CodexEngine.get().getItemManager();

try {
    ItemType type = items.getItemType("ORAXEN_ruby_sword");
    ItemStack stack = type.create();
} catch (MissingProviderException e) {
    // Oraxen isn't installed or isn't enabled
} catch (MissingItemException e) {
    // Oraxen is present but has no item with that id
}
```

Both are **checked** exceptions extending `CodexItemException`, so you must handle or declare them.
Distinguishing them in your error messages is worthwhile — "install Oraxen" and "check your item id"
are very different fixes for a server owner.

> ### Nexo behaves differently on both counts
>
> Nexo's provider is registered only when Nexo is installed, so `NEXO_x` on a server without it falls
> through to the **vanilla** provider with the whole key and raises `MissingItemException`, not
> `MissingProviderException`.
>
> And Nexo never validates ids at lookup time — `getItemType("NEXO_anything")` always succeeds, with
> `create()` returning `null` later for an unknown id. Catch `CodexItemException` to cover both
> exception types, and null-check `create()` for Nexo items.

You can also pass namespace and id separately, which avoids the parsing rules entirely:

```java
ItemType type = items.getItemType("ORAXEN", "ruby_sword");
ItemType hat  = items.getItemType("ITEMSADDER", "myitems:cool_hat");
```

The namespace argument is case-sensitive here too — pass it uppercase.

Note the ItemsAdder id retains its own `namespace:item` form. See
[the ID format](#the-id-format) above.

### Going the other way

Identify an existing `ItemStack`:

```java
ItemType type = items.getMainItemType(stack);   // highest-Category match
Set<ItemType> all = items.getItemTypes(stack);  // every provider that claims it
```

`getItemTypes` returns a set because more than one provider can recognise the same stack.
`getMainItemType` picks the one with the highest `Category` — the enum's declaration order
(`VANILLA` < `MOD` < `EXTERNAL` < `PRO`) is load-bearing here.

> **`getMainItemType` is effectively never `null`.** The vanilla provider matches *every* stack, so
> the result set is never empty. Do not use a null check to mean "not a custom item".

> ### ⚠️ Two manager-level methods are currently unusable
>
> - **`isCustomItem(stack)` always returns `true`** for any non-null stack, because the vanilla
>   provider's implementation is `item != null`.
> - **`isCustomItemOfId(stack, id)` always returns `false`** for Oraxen, Nexo, and ItemsAdder — a
>   prefix comparison is off by one and can never match.
>
> Until these are fixed, go through the provider directly, or compare with `ItemType.isInstance`.

### Comparing items

```java
if (type.isInstance(stack)) {
    // stack is an instance of this item type
}
```

Prefer `isInstance` over comparing `ItemStack`s directly — custom item plugins store their identity
in NBT or persistent data, and a naive equality check will fail on items that differ only in
durability, enchantments, or stack size.

`ItemType` implements `equals`/`hashCode` over namespace and id, so it is safe as a map key.

### The `ItemType` API

| Method | Returns |
|---|---|
| `getNamespace()` | Provider namespace, e.g. `ORAXEN` |
| `getID()` | The item id within that provider — **not uniform**: vanilla returns the lowercased material name, Oraxen and Nexo the raw id, ItemsAdder `namespace:id` |
| `getNamespacedID()` | Combined `NAMESPACE_id` (plain id for vanilla) |
| `getCategory()` | `VANILLA`, `MOD`, `EXTERNAL`, or `PRO` |
| `create()` | A new `ItemStack` |
| `isInstance(stack)` | Whether a stack is this type |

### Stripping prefixes

There are two, and they are not the same:

```java
// Strips one known prefix; case-insensitive; returns input unchanged on no match
PrefixHelper.stripPrefix("ORAXEN", "ORAXEN_ruby_sword");   // "ruby_sword"

// Strips whichever REGISTERED namespace matches
CodexItemManager.stripPrefix("ORAXEN_ruby_sword");          // "ruby_sword"
```

Both split at the **first underscore only**, so an id whose own first segment happens to equal the
namespace would be mangled.

### Registering your own provider

`MyItemType` must extend `ItemType`, which is an abstract class requiring `getNamespace`, `getID`,
`getCategory`, `create`, and `isInstance`.

```java
public class MyProvider implements ICodexItemProvider<MyItemType> {   // T extends ItemType
    public static final String NAMESPACE = "MYPLUGIN";

    @Override public String pluginName()   { return "MyPlugin"; }
    @Override public String getNamespace() { return NAMESPACE; }
    @Override public Category getCategory() { return Category.EXTERNAL; }

    @Override public MyItemType getItem(String id) { ... }
    @Override public MyItemType getItem(ItemStack stack) { ... }
    @Override public boolean isCustomItem(ItemStack item) { ... }
    @Override public boolean isCustomItemOfId(ItemStack item, String id) { ... }
}
```

Register it:

```java
CodexEngine.get().getItemManager()
    .registerProvider(MyProvider.NAMESPACE, new MyProvider());
```

Once registered, every Codex-based plugin on the server can reference your items as
`MYPLUGIN_<id>` in its own configs — you do not need to integrate with them individually.

`registerProvider` **uppercases the namespace** on insert and throws `IllegalArgumentException` if it
is already taken. `unregisterProvider(Class)` removes one.

`assertEnabled()` is a default method that checks `isPluginEnabled(pluginName())`. Call it from your
resolution methods to throw `MissingProviderException` when your plugin is absent — but **never
return `null` from `pluginName()`** unless you also override `assertEnabled()`, or it will NPE.
(`VanillaProvider` does exactly that, which is why it gets away with a null name.)

### Checking availability

> ⚠️ **`hasProvider` does not tell you the plugin is installed.** Codex registers the `VANILLA`,
> `ORAXEN`, and `ITEMSADDER` providers eagerly at startup whether or not those plugins are present,
> so `hasProvider("ORAXEN")` returns `true` on a server with no Oraxen. Only `NEXO` is registered
> conditionally, by its hook.

To find out whether items can actually be resolved, attempt the lookup and catch
`MissingProviderException` — that is raised by `assertEnabled()`, which checks the backing plugin is
really enabled:

```java
try {
    ItemType type = items.getItemType("ORAXEN_ruby_sword");
} catch (MissingProviderException e) {
    // Oraxen is not installed or not enabled
}
```

Or check the plugin directly via [[Hooks]]:

```java
if (Hooks.hasPlugin("Oraxen")) { ... }
```

`hasProvider` and `getProviders` remain useful for inspecting what is registered:

```java
Collection<ICodexItemProvider<?>> all = items.getProviders();
```
