# Utilities

Codex ships a set of static helper classes, conventionally suffixed `UT`. They live in
`studio.magemonkey.codex.util` and are available from the `codex-api` artifact — see
[[Developer Getting Started]].

## `StringUT` — text and colour

The one you will use most.

```java
String colored = StringUT.color("&aHello &#ff0088World");   // & codes + hex
List<String> lore = StringUT.color(rawLore);                 // works on lists and sets

StringUT.colorOff(text);          // strip colour
StringUT.colorRaw(text);          // raw colour handling
StringUT.colorFix(text);          // repair broken colour sequences
StringUT.colorSensitiveStrip(s);  // strip while preserving colour state
```

Hex colours use `&#RRGGBB` (matching `StringUT.HEX_PATTERN`).

Safe parsing with fallbacks — no exception handling needed:

```java
double d = StringUT.getDouble(input, 0.0);
double positive = StringUT.getDouble(input, 0.0, false);  // reject negatives
int    i = StringUT.getInteger(input, 1);
int[]  arr = StringUT.getIntArray("1,2,3");
```

Formatting and tab completion:

```java
StringUT.capitalizeFully("iron sword");        // "Iron Sword"
StringUT.capitalizeFirstLetter("iron sword");  // "Iron sword"
StringUT.oneSpace(messy);                      // collapse repeated spaces
StringUT.noSpace(text);                        // remove spaces

// Filter completions by what the user has typed so far
List<String> matches = StringUT.getByFirstLetters(arg, allOptions);
```

`getByFirstLetters` is exactly what you want in a `TabCompleter`.

Placeholder replacement across lists, including multi-line expansion:

```java
StringUT.replace(lore, "%description%", descriptionLines);
StringUT.replace(lore, "%name%", name);
```

## `MsgUT` — sending messages

```java
MsgUT.sendActionBar(player, "&eStatus text");
MsgUT.sendTitles(player, title, subtitle, fadeIn, stay, fadeOut);
MsgUT.sound(player, Sound.ENTITY_PLAYER_LEVELUP);
MsgUT.sound(location, "entity.player.levelup");

MsgUT.sendWithJSON(sender, jsonOrPlainText);
boolean isJson = MsgUT.isJSON(text);
```

`sendWithJSON` detects whether the string is a JSON chat component and sends it appropriately, so you
can accept either form from config. Title timings are in ticks.

Action bar rendering respects the `action-bar-legacy` setting — see [[Configuration]].

## `NumberUT` — numbers

```java
NumberUT.format(1234.5678);           // default formatting
NumberUT.format(value, "#.##");       // custom pattern
NumberUT.round(value);
NumberUT.round(value, "#.##");
NumberUT.toRoman(4);                  // "IV"
```

`toRoman` is handy for enchantment-style level display.

## `PlayerUT` — players

```java
PlayerUT.execCmd(player, "spawn");
List<String> names = PlayerUT.getPlayerNames();
String ip = PlayerUT.getIP(player);
PlayerUT.setExp(player, 5000L);
```

`setExp` sets total experience rather than level, which is the calculation Bukkit does not give you.

## `LocUT` — locations

```java
String raw = LocUT.serialize(location);
Location loc = LocUT.deserialize(raw);

List<String> rawList = LocUT.serialize(locations);
List<Location> locs  = LocUT.deserialize(rawList);

Location ground = LocUT.getFirstGroundBlock(loc);
```

Use these for storing locations in config — the round trip is stable. `getFirstGroundBlock` finds a
safe standing position, the same logic behind `/stuck` (see [[Commands]]).

## `EntityUT` — entities

```java
double max  = EntityUT.getAttribute(entity, Attribute.GENERIC_MAX_HEALTH);
double base = EntityUT.getAttributeBase(entity, Attribute.GENERIC_MAX_HEALTH);
ItemStack[] gear = EntityUT.getEquipment(entity);
```

The distinction between total and base attribute values matters when displaying "how much of this
comes from gear" — see also [[Compat and NMS]].

## `FileUT` — files

```java
FileUT.copy(inputStream, targetFile);
FileUT.mkdir(folder);
FileUT.create(file);

List<File> files   = FileUT.getFiles(path, true);   // true = recurse
List<File> folders = FileUT.getFolders(path);
```

## `Debugger` — logging

```java
Debugger.setDebug(true);
Debugger.log("Loaded 42 items");
Debugger.warn("Config key missing, using default");
Debugger.err("Failed to parse item definition");
```

`log` output only appears when debug is enabled, which is driven by `debug` in [[Configuration]].
Prefer `Debugger.log` over `System.out.println` — server owners can then turn your diagnostics on and
off.

## Other helpers

| Class | Purpose |
|---|---|
| `SoundUT` | Resolve sounds by name across versions |
| `EnumUT` | Version-safe enum name resolution |
| `EffectUT` | Potion and particle effect helpers |
| `ItemUT` | Item stack helpers (in `codex-plugin`, not `codex-api`) |
| `CollectionsUT` | Collection utilities |
| `DataUT` | Persistent data container helpers |
| `RangeUtil` | Numeric range parsing and checks |
| `ClickText` | Build clickable chat components |
| `Reflex` | Reflection helpers |
| `Zipper` | Zip archive handling |
| `SerializationBuilder` / `DeserializationWorker` | Config serialisation |

`SoundUT.getSound(name)` and `EnumUT.getName(value)` exist because Mojang renames enum constants
between versions — resolve by name through these rather than calling `valueOf` directly.

## Expression evaluation

Codex bundles a small expression evaluator under `util.eval`, used for formula-driven values in
configs — attribute scaling, damage formulas, and similar. Plugins built on Codex expose it wherever
they accept a formula string rather than a fixed number.

## Registries

`codex-core` provides shared registries for cross-plugin data:

| Registry | Holds |
|---|---|
| `AttributeRegistry` | Custom attributes |
| `BuffRegistry` | Buff definitions |
| `DamageRegistry` | Damage types |

Plus `NamespaceResolver` for namespaced key handling and `MigrationUtil` for config migration.
