# Bugs to investigate

Findings from a source audit conducted while rewriting the project wiki. Each item is a discrepancy
between what the code appears intended to do and what it actually does.

**Verification key:**

- ✅ — read the code directly and confirmed
- 🔍 — reported by an audit pass with a file:line citation, not independently re-checked

Line numbers were accurate at the time of the audit and may drift.

---

## Critical — feature does not work at all

### 1. ✅ `isCustomItemOfId` always returns false for non-vanilla providers

`codex-plugin/.../items/CodexItemManager.java:154-166`

```java
!id.substring(0, provider.getNamespace().length() + 1)
        .equalsIgnoreCase(provider.getNamespace())
```

`substring(0, len + 1)` produces a string of length `len + 1`, compared against a namespace of length
`len`. Never equal, so the guard always returns `false`. Oraxen, Nexo, and ItemsAdder can never match.

Probably intended `substring(0, len)`, or `id.startsWith(ns + "_")`.

### 2. ✅ `/stuck` is a dead command

`codex-plugin/src/main/resources/plugin.yml:11-15`

`plugin.yml` declares a `stuck` command, but nothing ever assigns it an executor — `UnstuckCommand`
registers only the label `unstuck`. `/stuck` falls through to `JavaPlugin`'s default `onCommand`,
returns `false`, and prints its usage line.

Compounding it: `paper-plugin.yml` has no `commands:` section at all, so on Paper the label does not
exist. The console log line also hardcodes `'/stuck'` regardless of the label used.

Fix is either binding the executor or adding `stuck` to `UnstuckCommand`'s label list.

### 3. ✅ `general.lang` vs `core.lang` — language selection is unreachable

`codex-plugin/.../config/ConfigManager.java:37` reads `general.lang`.
`codex-plugin/.../config/api/IConfigTemplate.java:36,41` injects and reads `core.lang`.

`core.lang` is auto-injected into `config.yml` and parsed into a field with no consumers.
`general.lang` — the key that actually selects the file — is never written to the shipped config. A
server owner following the generated file cannot switch language.

Separately, `CodexEngine.java:206-207` loads the `MessageUtil` set from a hardcoded `messages_en.yml`,
so that half is never localised regardless.

### 4. 🔍 `/board show` always reports failure

`codex-plugin/.../mccore/scoreboard/PlayerBoards.java:166-172`

`showBoard` is missing a `return true` on the success path, so it always returns `false` and the
caller prints "You do not have a scoreboard with that name". The board does switch.

Same method compares against `ChatColor.stripColor(name.toLowerCase())` without lowercasing the
argument, so only an all-lowercase input can match.

### 5. 🔍 Chat module never persists anything

`codex-plugin/.../mccore/chat/Chat.java:58-60`, `mccore/config/Config.java:156-163`

`ChatData` is registered as an `ISavable` against a `data` config, but `Config.save()` is never
called — not on disable, not on a timer. Display names and prefixes set in game are lost on restart.

Related: `CodexEngine.getConfigFile`/`registerConfig` javadoc (`CodexEngine.java:406-410`) claims
registered configs are auto-saved on disable. No such save exists anywhere; the static `configs` map
is written to and never read.

---

## High — silent data loss, crashes, or wrong results

### 6. ✅ `Menu.getPages()` off-by-one makes the last page unreachable

`codex-api/.../manager/api/menu/Menu.java:58-64`

```java
return (slots.lastKey() - 1) / this.inventory.getSize() + 1;
```

Should be `lastKey / size + 1`. They diverge whenever `lastKey` is an exact multiple of the page
size. With 28 entries in a 3-row menu (`lastKey = 27`, size 27) this returns `1`, and `open()`'s
`page % getPages()` then pins the menu to page 0 — entry 28 is permanently invisible.

### 7. 🔍 `PreviousPageButton` renders a blank menu on page 0

`Menu.java:125,253`

`open(int)` does `page % getPages()`. Java's `%` preserves sign, so `-1 % 2 == -1`, every virtual
slot lookup goes negative, and the inventory renders empty. `NextPageButton` wraps correctly; only
the previous-page direction is affected.

### 8. 🔍 Action parameter flags with capitals can never match

`codex-plugin/.../util/actions/params/IParam.java:16-17`

```java
this.flag = flag.toLowerCase();
this.pattern = Pattern.compile("(~)+(" + this.getFlag() + ")+?(:)+(.*?)(;)");
```

The flag is lowercased, then compiled into a case-sensitive pattern, and `Parametized.java:62-63`
gates on a case-sensitive `contains`. Params registered as `"fadeIn"`, `"fadeOut"`
(`ActionsManager.java:194,196`) become `fadein`/`fadeout`, so `~fadeIn: 10;` is silently ignored.

Either register them lowercase, or make the match case-insensitive.

### 9. 🔍 The GUI editor writes action params that cannot be parsed back

`codex-plugin/.../manager/editor/object/IEditorActionsMain.java:285,319,156`

Two problems in the same area:

- `ActionBuilder.inject()` writes `prefix + mapParam.toString()` rather than the built param string.
- Both editor paths key params by `param.getKey().toLowerCase()` (e.g. `titles_fade_in`) instead of
  `param.getFlag()` (`fadein`).

Editor-produced lines for the titles and progress-bar params will not round-trip.

### 10. 🔍 `VAULT_BALANCE` condition passes when Vault is absent

`codex-plugin/.../util/actions/conditions/list/Condition_VaultBalance.java:109`, and
`IConditionValidator.java:222-225`

`validate()` returns `null` with no Vault; the caller logs an error and treats a null predicate as
**passed**. A balance gate silently becomes a no-op rather than failing closed. Same path is taken
when `~amount:` is missing.

### 11. 🔍 `getGroupValueDouble` / `getGroupValueLong` truncate comparisons

`codex-plugin/.../hooks/Hooks.java:57,103`

The comparator returns `(int)(val2 - val1)`, so `2.5` and `2.1` compare equal. The comparator is also
non-transitive when `isNegaBetter` is set, which `Stream.sorted` can reject outright with
"Comparison method violates its general contract" on larger maps.

Related: group map keys are compared case-sensitively (`Hooks.java:51,74,97`) against values that were
lowercased upstream (`VaultHK.java:141-142`), so a key of `"VIP"` never matches.

### 12. 🔍 `/unstuck` NPEs and permanently wedges a player with no recorded positions

`codex-plugin/.../commands/UnstuckCommand.java:137`

`locs.get(uuid).getFirst()` is unguarded. A player who has not moved enough to record a position gets
an NPE inside the scheduled task, which leaves them in the `warmingUp` map forever — every later
`/unstuck` reports "already warming up".

### 13. 🔍 `ArmorListener` fires the same event object twice on cancel paths

`codex-plugin/.../listeners/ArmorListener.java:582-590`, `:627-632`

Both the DRAG and BROKE paths call `callEvent(armorEquipEvent)` a second time inside
`if (armorEquipEvent.isCancelled())`. Listeners see the same instance twice and `ignoreCancelled`
behaves unpredictably. Looks like a bad merge — the inner block appears redundant.

### 14. 🔍 `ArmorListener.inventoryClick` NPE on empty slots

`ArmorListener.java:177,198,368`

`Objects.requireNonNull(ArmorType.matchType(e.getCurrentItem()))` — `matchType` returns `null` for
air/null (`ArmorType.java:24`), so shift-clicking an empty slot throws.

### 15. 🔍 `ItemsAdderProvider.isCustomItemOfId` NPE

`codex-api/.../api/items/providers/ItemsAdderProvider.java:56-58`

`CustomStack.isInRegistry(id)` validates the *id*, then `CustomStack.byItemStack(item)` is
dereferenced with no null check — NPE for any non-ItemsAdder stack. Oraxen and Nexo guard this
correctly. Currently masked by bug #1, which prevents the line being reached via `CodexItemManager`.

### 16. 🔍 `NexoProvider.getItem(String)` never returns null

`codex-api/.../api/items/providers/NexoProvider.java:33-38`

It fabricates a `NexoItemType` for any id, so `MissingItemException` is unreachable for Nexo and
`create()` later returns `null` (`:83-90`), pushing an NPE into callers. Suggest
`return NexoItems.exists(id) ? new NexoItemType(id) : null;`.

---

## Medium — misleading behaviour, leaks, dead code

### 17. ✅ `config.yml` documents the wrong permission node

`codex-plugin/src/main/resources/config.yml:34` says `core.oninteract.bypass`.
`codex-plugin/.../action/CommandBlock.java:68` checks `general.oninteract.bypass`.

The comment has been wrong long enough that it propagated into the old wiki.

### 18. 🔍 `paper-plugin.yml` `load-before` block is inert

`codex-plugin/src/main/resources/paper-plugin.yml:33-37`

Two issues: `load-before` is not part of Paper's `paper-plugin.yml` schema (ordering is expressed via
`dependencies.server.<Plugin>.load: BEFORE`, already used at `:19-32`), and each entry misspells
`bootstrap` as `boostrap`. The intended Fabled/Divinity ordering is probably not happening.

### 19. 🔍 Config keys with no defaults silently change behaviour when removed

- `unstuck.warmup` / `unstuck.cooldown` — `UnstuckCommand.java:121,141,145` read with no default and
  are not `addMissing`-injected, so deleting them yields `0`: instant teleport, no cooldown.
- `removeBoatOnExit` — `BoatListener.java:15` reads with no default, so deleting it turns the feature
  off even though the shipped file says `true`.

### 20. 🔍 `CommandBlock.permission` cannot be set from config

`CommandBlock.java:37,71-73`

The field is checked at invoke time but the map constructor never reads it, so it is always `null`. A
`permission:` key in an `onInteract` entry does nothing.

### 21. 🔍 `EquipMethod.DEATH` is declared but never fired

`codex-api/.../api/armor/ArmorEquipEvent.java:138`

No death handler exists in `ArmorListener`. Either wire it up or remove the constant.

### 22. 🔍 `EngineServerPacketEvent` is never fired

Only `EnginePlayerPacketEvent` is ever constructed (`PacketManager.java:93-94`). Also note only
**outbound** packets are intercepted — the injected handler overrides `write()` and not
`channelRead()`, so inbound client packets never produce an event.

### 23. 🔍 All `IEvent` subclasses share one static `HandlerList`

`codex-api/.../api/events/IEvent.java:9`

`protected static final HandlerList handlers` with a `final getHandlers()`. Every descendant
registers into the same list, so a listener for any one of them is walked for all of them. Given
these fire per outbound packet, this is a measurable hot path.

### 24. 🔍 Leaks

- `Menu.ACTIVE_MENUS` — quitting during a `fakeClose()` window leaves the `Player` key in the static
  map forever (`Menu.java:164-167`); and `onClose()` reopens `parentMenu` for a player who is
  leaving (`:179`), re-inserting an entry that is never removed.
- `ArmorListener.inventoryDropPlayers` — a static `Set<UUID>` whose entries are only removed if a
  `PlayerDropItemEvent` follows (`:154,371,646-649`). If the click is cancelled downstream, the
  player's next real drop is silently swallowed.
- `Chat.players` — quit handler removes by raw name while the store is keyed by lowercase
  (`Chat.java:52-56` vs `ChatListener.java:66-68`), so entries usually survive disconnect.

### 25. 🔍 `PacketManager.removePlayer` has no null guard

`PacketManager.java:72-75` — `this.getChannel(player).pipeline()` is called from `PlayerQuitEvent`,
where the connection may already be torn down.

### 26. 🔍 `assertEnabled()` NPEs on a null `pluginName()`

`codex-api/.../api/items/providers/ICodexItemProvider.java:10-14` passes `pluginName()` straight to
`isPluginEnabled(String)`. `VanillaProvider.pluginName()` returns `null` and only escapes by
overriding `assertEnabled()`. A trap for third-party providers.

### 27. 🔍 `VanillaProvider.isCustomItem` makes the manager-level check meaningless

`VanillaProvider.java:54-56` returns `item != null`, so
`CodexItemManager.isCustomItem` returns `true` for every non-null stack. Same reason
`getMainItemType` is effectively never `null`.

### 28. 🔍 `GOTO` has no recursion guard

`codex-plugin/.../util/actions/actions/IActionExecutor.java:76` re-enters
`ActionManipulator.process` with no depth limit. A self-referencing section recurses to
`StackOverflowError`.

### 29. 🔍 Target group names are unmatchable if they contain capitals

`IActionExecutor.java:57-66` lowercases `~target:` values before lookup, but
`ActionManipulator.java:126,133` stores the group key verbatim from `~name:`. A group declared
`~name: Nearby;` can never be selected.

### 30. 🔍 `debug` toggles a logger with no call sites

`CodexEngine.java:304` feeds the config value to `Debugger.setDebug`. The only `Debugger.*` call in
the repository is `DelayedCommand.java:33`, inside a constructor that config deserialization never
uses. The setting is effectively inert, which makes it useless for troubleshooting.

### 31. 🔍 `locale.world-names` has no consumers

`CoreConfig.java:23-35` builds the map and exposes `getWorldName(String)`, which is never called
anywhere in the repo.

---

## Low — hygiene, staleness, dead code

### 32. ✅ README points at the wrong GitHub org

`README.md:1-2` uses `magemonkeystudios` (plural). The repository is `magemonkeystudio/codex`. It
resolves today only via GitHub's rename redirect — both build badges and the contributor image depend
on that.

### 33. ✅ README version is stale

`README.md:18` says `1.2.0-R0.5-SNAPSHOT`; `pom.xml:14` is `1.2.0-R0.6-SNAPSHOT`.

### 34. 🔍 `codex-nms` module version drift

`codex-nms/pom.xml:8-9,24-25` — artifact version `1.2.0-R0.1-SNAPSHOT` with parent
`codex-parent:1.2.0-R0.4-SNAPSHOT`, while the rest of the tree is at `R0.6-SNAPSHOT`.

### 35. 🔍 `OraxenProvider` is deprecated but still registered eagerly

`codex-api/.../api/items/providers/OraxenProvider.java:10` carries
`@Deprecated(forRemoval = true, since = "Dec 2024")`, yet `CodexItemManager.init()` registers it
unconditionally alongside the non-deprecated providers.

### 36. 🔍 Dead code

- `mccore/gui/**` (map-image rendering: `MapMenu`, `MapImage`, `MapScene`, …) — no references outside
  its own package.
- `mccore/items/**` (`ItemManager`, `InventoryManager`) — same.
- `mccore/commands/**` (`ConfigurableCommand`, its own `CommandManager`, `CommandListener`,
  `SenderType`, `IFunction`, `LogFunction`) and `legacy/command/RiseCommand.java` — no callers in main
  source.

Note `mccore.commands.CommandManager` shares a simple name with the live
`codex.commands.CommandManager`, which is an easy import mistake.

### 37. 🔍 `messages_cn.yml` is partly stale

`codex-plugin/src/main/resources/lang/messages_cn.yml:2-37` — the whole `CodexCore:` root does not
match any current key path (`CoreLang` maps to `Codex.Command.Usage`, not `CodexCore.Command.Usage`).
It also lacks `Error.Internal` and all `Codex.Editor.*` keys, and carries a dead
`CodexCore.Statistics` section.

### 38. 🔍 Sentinel is an undeclared integration

`Hooks.java:147-153` hard-references `SentinelTrait` and gates NPC PvP on it; `sentinel` is a
`provided` dependency in `pom.xml:105-109`. It appears in no `softdepend` list and has no `Hooks`
constant.

### 39. 🔍 `ArmorUtil` silently no-ops before 1.19.4

`VersionManager.java:37-41` installs an empty anonymous implementation when the version module ships
no `ArmorUtilImpl`. Trim calls succeed and do nothing on 1.16.5 / 1.17 / 1.18.2.

### 40. 🔍 `Version` enum can throw before the friendly error

`codex-api/.../core/Version.java:50-61` — on modern Paper the static initializer throws
`IllegalStateException("Unexpected version: …")`, surfacing as `ExceptionInInitializerError` before
`VersionManager`'s "Please upgrade to the latest minor version" message can be shown.

### 41. 🔍 `getDestinationSlots` return value discarded

`ArmorListener.java:220` calls `getDestinationSlots(currentItem, top);` and ignores the result. Every
other call site does `destinationSlots.addAll(...)`.

### 42. 🔍 `/chat` defects

- `/chat name` truncates the last character — `name.substring(0, name.length() - 2)` after appending
  a trailing space (`NameCommand.java:59-63`). `/chat name Bob` sets `Bo`.
- `/chat reset` does not clear the prefix, though `ChatCommander.usage()` claims it does
  (`ChatCommander.java:64` vs `ResetCommand.java:54-58`).
- `/chat prefix` takes exactly one argument (`PrefixCommand.java:54`), so prefixes containing spaces
  cannot be selected. `hasPrefix` also does not strip colour, unlike the sibling methods
  (`ChatData.java:258-265` vs `:246,290`).

### 43. 🔍 `BoardManager.enableScoreboard()` overwrites every online player's scoreboard

`BoardManager.java:200-208` — triggered the first time a below-name objective or team is used. A
likely conflict source with other scoreboard plugins.

---

## Suggested triage

If you only fix a handful, these are the ones with user-visible impact and small diffs:

1. **#1** `isCustomItemOfId` off-by-one — one character, restores a public API
2. **#2** `/stuck` — add the label to `UnstuckCommand`
3. **#6** `getPages()` off-by-one — one character, fixes silent data loss in menus
4. **#8** action param flag casing — makes `~fadeIn:` work as every example implies
5. **#17** the `config.yml` comment — one line, stops the wrong node propagating further
6. **#3** add `general.lang` to the shipped config, or make `core.lang` the effective key
