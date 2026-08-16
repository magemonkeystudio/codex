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

Placeholder replacement across lists, including multi-line expansion. **These return a new list and
do not mutate the input** — assign the result:

```java
lore = StringUT.replace(lore, "%description%", descriptionLines);
lore = StringUT.replace(lore, "%name%", name);
```

> Two traps. An empty replacement list is substituted as the literal `["[]"]`. And a third overload,
> `replace(String placeholder, List<String> r, String... orig)`, takes its arguments in a different
> order — check which one you are calling.

## `MsgUT` — sending messages

```java
MsgUT.sendActionBar(player, "&eStatus text");
MsgUT.sendTitles(player, title, subtitle, fadeIn, stay, fadeOut);
MsgUT.sound(player, Sound.ENTITY_PLAYER_LEVELUP);
MsgUT.sound(location, "entity.player.levelup");

MsgUT.sendWithJSON(sender, jsonOrPlainText);
boolean isJson = MsgUT.isJSON(text);
```

> `isJSON` does **not** detect a serialized chat component — it literally checks whether the string
> contains `json:`. Both methods deal with Codex's own inline markup,
> `{json:~hint:…;~url:…;}text{end-json}`, which `sendWithJSON` expands into a clickable/hoverable
> component for players and strips to plain text for console. Raw chat-component JSON is not parsed.

Title timings are in ticks. Note `sendTitles` is marked `@Deprecated` in source.

For the string-name `sound` overloads, prefer the enum form (`ENTITY_PLAYER_LEVELUP`) — the name is
upper-cased before lookup, and an unknown sound is swallowed silently.

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

PlayerUT.setExp(player, 5000L);              // ADDS 5000 XP to the current total
PlayerUT.setTotalExperience(player, 5000);   // sets the total to exactly 5000
```

> ⚠️ Despite the name, **`setExp` adds rather than sets** — it reads the current total, adds your
> amount, and writes the result back. Negative amounts subtract. Use `setTotalExperience` for an
> absolute value; it throws `IllegalArgumentException` on negatives.

Both work in total experience rather than levels, which is the calculation Bukkit does not give you.

## `LocUT` — locations

```java
String raw = LocUT.serialize(location);
Location loc = LocUT.deserialize(raw);

List<String> rawList = LocUT.serialize(locations);
List<Location> locs  = LocUT.deserialize(rawList);

Location ground = LocUT.getFirstGroundBlock(loc);
```

Use these for storing locations in config — the round trip is stable, preserving yaw and pitch. The
serialized format is `x,y,z,pitch,yaw,world`.

> Both `serialize` and `deserialize` are `@Nullable` — a null world, or a string without exactly six
> fields, yields `null`. Check the result.

`getFirstGroundBlock(loc)` walks downward until it finds a solid block and returns the position above
it. Note this is **not** what `/unstuck` uses — that replays recently-recorded standing positions
(see [[Commands]]).

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

> ⚠️ **All three are gated**, not just `log` — `warn` and `err` are equally invisible unless `debug`
> is enabled in [[Configuration]]. They also write to `System.out` with a `[RiseDebugger]` prefix
> rather than to the plugin logger.

Use these for diagnostics a server owner should be able to switch on. For anything that must always
reach console, use `plugin.warn(...)` / `plugin.error(...)` instead.

## Other helpers

| Class | Purpose |
|---|---|
| `SoundUT` | Resolve sounds by name across versions |
| `EnumUT` | Version-safe enum name resolution |
| `EffectUT` | Particle spawning and line drawing |
| `ItemUT` | Item stack helpers (in `codex-plugin`, not `codex-api`) |
| `CollectionsUT` | Collection utilities, plus `getEnum`/`getEnums` name resolution |
| `DataUT` | Persistent data container helpers, incl. types Bukkit lacks (`UUID`, `BOOLEAN`, array types) |
| `RangeUtil` | Random value from a `DoubleRange`, and percentage chance rolls |
| `ClickText` | Build clickable chat components |
| `Reflex` | Reflection helpers |
| `Zipper` | A single `createBackupZip(dir)` helper |
| `SerializationBuilder` / `DeserializationWorker` | Config serialisation |

`SoundUT.getSound(name)` and `EnumUT.getName(value)` exist because several Bukkit types **stopped
being enums** and became registry-backed `Keyed` values — not because constants were renamed.
`getSound` reflects `valueOf` and falls back to `Enum.valueOf`, returning `Keyed`; `EnumUT.getName`
reflectively reads `name()` off a value whose type may or may not still be an enum. Use them instead
of a direct `Sound.valueOf(...)` or `.name()` call, which breaks across versions.

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

Plus `NamespaceResolver`, which resolves `PotionEffectType`s and `Enchantment`s from a list of
candidate names so they survive Mojang's renames, and `MigrationUtil` for config and data-folder
migration.

The expression evaluator's entry point is `Evaluator.eval(String expression, int method)` — method
`0` uses the internal parser, `1` or `2` use the bundled javaluator (falling back to the internal one
on failure). It lives in `codex-api`.
