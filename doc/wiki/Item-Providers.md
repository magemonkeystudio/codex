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
| `ORAXEN` | Oraxen | Plain item id |
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

This trips people up because prefix *stripping* inside each provider is case-insensitive, so the
inconsistency is not obvious. Always write the namespace in capitals.

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

The two exceptions are worth distinguishing in your error messages — "install Oraxen" and "check your
item id" are very different fixes for a server owner.

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
ItemType type = items.getMainItemType(stack);      // best match, may be null
Set<ItemType> all = items.getItemTypes(stack);     // every provider that claims it

boolean custom = items.isCustomItem(stack);
boolean isRuby = items.isCustomItemOfId(stack, "ruby_sword");
```

`getItemTypes` returns a set because more than one provider can recognise the same stack. Use
`getMainItemType` unless you specifically need all matches.

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
| `getID()` | The item id within that provider |
| `getNamespacedID()` | Combined `NAMESPACE_id` (plain id for vanilla) |
| `getCategory()` | `VANILLA`, `MOD`, `EXTERNAL`, or `PRO` |
| `create()` | A new `ItemStack` |
| `isInstance(stack)` | Whether a stack is this type |

### Stripping prefixes

```java
String id = PrefixHelper.stripPrefix("ORAXEN", "ORAXEN_ruby_sword");  // "ruby_sword"
```

Returns the input unchanged if the prefix does not match, so it is safe to call unconditionally.

### Registering your own provider

```java
public class MyProvider implements ICodexItemProvider<MyItemType> {
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

`assertEnabled()` is provided as a default method; call it from your resolution methods to throw
`MissingProviderException` when your plugin is not enabled.

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
