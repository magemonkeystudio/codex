# Developer Getting Started

Codex is published to the MageMonkey Maven repository at
[repo.travja.dev](https://repo.travja.dev).

> ### The artifact is `codex`; the plugin is `CodexCore`
>
> These deliberately differ and it trips up every new integrator. The Maven artifactId is **`codex`**,
> but the Bukkit plugin name — what you put in `depend:`, what names the data folder, and what
> prefixes the permission nodes — is **`CodexCore`**.

## Maven

```xml
<repositories>
    <repository>
        <id>magemonkey-snapshots</id>
        <url>https://repo.travja.dev/snapshots</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>studio.magemonkey</groupId>
        <artifactId>codex</artifactId>
        <version>1.2.0-R0.6-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

Release builds go to `https://repo.travja.dev/releases`. Check the repository for the current version.

`codex` shades `codex-api`, `codex-core`, and `codex-bungee` into the final jar, so depending on
`codex` alone gives you everything.

### The module layout

| Module | Artifact | Contains |
|---|---|---|
| `codex-api` | `codex-api` | Item types, events, menus, `*UT` utilities, compat interfaces |
| `codex-core` | `codex-core` | Attribute/buff/damage registries, `NamespaceResolver`, `MigrationUtil` |
| `codex-plugin` | **`codex`** | The Bukkit plugin: `CodexEngine`, `CodexPlugin`, commands, hooks, actions engine, `JYML` |
| `codex-nms` | `codex-nms-v*` | Per-version NMS implementations (aggregator pom, versioned separately) |
| `codex-bungee` | `codex-bungee` | BungeeCord companion |

> Depending on `codex-api` alone is rarely what you want. It does **not** contain `CodexPlugin`,
> `CodexEngine`, `JYML`, or `IConfigTemplate` — those live in `codex-plugin`. `codex-api` is the
> right choice only if you genuinely need nothing but item types, events, and utilities.

## Declaring the dependency

```yaml
depend: [ CodexCore ]
```

`depend: [ ProMCCore ]` also resolves, because Codex declares `provides: ProMCCore`.

## Accessing the engine

```java
CodexEngine engine = CodexEngine.get();
```

| Accessor | Returns |
|---|---|
| `getItemManager()` | `CodexItemManager` — see [[Item Providers]] |
| `getMenuManager()` | `MenuManager` — see [[Menus and GUIs]] |
| `getActionsManager()` | `ActionsManager` — see [[Actions Engine]] |
| `getHooksManager()` | `HookManager` — see [[Hooks]] |
| `getPacketManager()` | `PacketManager` |
| `getCraftManager()` | `CraftManager` |
| `getVault()` | `VaultHK` |
| `getWorldGuard()` | `WorldGuardHK` |
| `getCitizens()` | `CitizensHK` |
| `getMythicMobs()` | `IMythicHook` |
| `getNexo()` | `NexoHK` |
| `getMessageUtil()` | `AbstractMessageUtil` |

> **Null-check the hook getters.** Only Vault and Nexo are hooked eagerly; WorldGuard, Citizens and
> MythicMobs are hooked exclusively from the plugin-enable path, so they can be `null` even when the
> plugin is installed. See [[Hooks]].

## Extending `CodexPlugin`

`CodexPlugin<P extends CodexPlugin<P>>` gives you the command framework, config templates, language
handling, and editor support.

**Six abstract methods must be implemented** — the first four are the ones people remember:

```java
public class MyPlugin extends CodexPlugin<MyPlugin> {

    private MyConfig cfg;
    private MyLang   lang;

    @Override
    public void enable() { /* startup */ }

    @Override
    public void disable() { /* shutdown */ }

    @Override
    public void registerCommands(@NotNull IGeneralCommand<MyPlugin> mainCommand) {
        mainCommand.addSubCommand(new MyCommand<>(this));
    }

    @Override
    public void registerEditor() {
        // assign this.editorHandler here if you have an editor
    }

    @Override
    public void setConfig() {
        this.cfg = new MyConfig(this);
        this.cfg.setup();
    }

    @Override
    public void registerHooks() {
        // registerHook(...) calls; runs FIRST, before configs exist
    }

    @Override
    @NotNull
    public IConfigTemplate cfg() { return this.cfg; }

    @Override
    @NotNull
    public CoreLang lang() { return this.lang; }
}
```

`cfg()` and `lang()` are abstract too, with fixed return types — `lang()` must return `CoreLang` or a
subclass. `registerHooks()` runs before configuration is loaded, so do not read config from it.

> `hasEditor()` is **not** an override — it is concrete, returning `editorHandler != null`. Assign
> the protected `editorHandler` field in `registerEditor()` and the `editor` subcommand appears
> automatically. Overriding `hasEditor()` to `true` without assigning the field registers a command
> that throws `IllegalStateException` when run.

### What you get automatically

Codex registers a main command using the labels from your config's `core.command-aliases`, and
attaches:

| Subcommand | Permission | Condition |
|---|---|---|
| `help` | `<plugin>.user` | always; also the default for a bare `/<label>` |
| `reload` | `<plugin>.admin` | always |
| `editor` | `<plugin>.cmd.editor` | only if `hasEditor()` |
| `about` | none | only on non-engine plugins |

`<plugin>` is your plugin's declared name, lowercased with spaces and hyphens removed. `My-Plugin`
gives `myplugin.admin`. Note the transformation only *removes* characters — it does not shorten, which
is why the engine's own nodes are `codexcore.*`.

Two things to watch:

- `registerCommands` runs **before** `reload` and `about` are attached, and `addSubCommand` overwrites
  by label — a subcommand of yours named `reload` is silently replaced.
- `core.command-aliases` is split on `,` with no trimming, and an **empty value disables the plugin**
  with "Could not register plugin commands!".

`CodexDataPlugin` is the database-backed variant if you need persistence.

## Configuration files

Use **`JYML`** (in `codex-plugin`). It extends `YamlConfiguration` and adds an `addMissing` pattern
for evolving configs without clobbering user edits.

```java
try {
    JYML cfg = JYML.loadOrExtract(this, "items.yml");
    cfg.addMissing("my.new.setting", true);
    cfg.saveChanges();
} catch (InvalidConfigurationException e) {
    error("Failed to load items.yml: " + e.getMessage());
}
```

`loadOrExtract` takes a `CodexPlugin<?>` — not a `JavaPlugin` — and throws a **checked**
`InvalidConfigurationException`, as do `reload()` and the constructors.

| Method | Purpose |
|---|---|
| `loadOrExtract(CodexPlugin<?>, String)` | Load, extracting the bundled default if absent |
| `loadAll(String path, boolean deep)` | Load every YAML file under a directory |
| `addMissing(String, Object)` | Add only if absent; returns whether it was added |
| `saveChanges()` | Save only if something changed |
| `getSection(String)` | Child keys at a path |
| `getStringSet` / `getIntArray` / `setIntArray` | Typed accessors |
| `getLocation` / `getItem` / `getGuiItem` | Richer types |
| `remove` / `save` / `getFile` / `reload` | Housekeeping |

`setLocation(path, loc)` is deprecated — plain `set(path, location)` already serialises correctly.

Pair `JYML` with `IConfigTemplate` for a structured config class; `CoreConfig` is the reference
implementation.

> ### ⚠️ Avoid `getConfigFile` / `registerConfig`
>
> ```java
> // Returns the legacy mccore Config type
> Config config = CodexEngine.get().getConfigFile(myPlugin, "items");
> ```
>
> Three reasons to skip it: it returns `studio.magemonkey.codex.mccore.config.Config`; the constructor
> **appends `.yml` itself**, so passing `"items.yml"` yields `items.yml.yml`; and its javadoc promises
> auto-save on disable, which **is not implemented** — registered configs are placed in a static map
> that is never read back. Use `JYML`.

## ⚠️ Legacy: the `mccore` package

Everything under `studio.magemonkey.codex.mccore` is deprecated **for third-party use**. It is the
codebase inherited from ProMCCore.

Two caveats on that word. Nothing there carries an `@Deprecated` annotation at class or package level,
so you get no compiler warning — treat it as project policy. And Codex itself still runs parts of it:
`mccore.chat` and `mccore.scoreboard` are enabled by default, and `mccore.config.Config` backs
`getConfigFile`.

| Legacy package | Use instead |
|---|---|
| `mccore.commands` — `ConfigurableCommand`, its own `CommandManager` | `codex.commands.api` — `IGeneralCommand`, `ISubCommand` |
| `mccore.config` — `Config`, `CommentedConfig`, `LanguageConfig` | `JYML` and `IConfigTemplate` |
| `mccore.items` — `ItemManager`, `InventoryManager` | `CodexItemManager` — see [[Item Providers]] |
| `mccore.chat` | A dedicated chat plugin — see [[Chat Module]] |
| `mccore.scoreboard` | A dedicated scoreboard plugin — see [[Scoreboard Module]] |
| `mccore.util` — `TextFormatter`, `TextSizer`, `TextSplitter`, `MobManager` | `codex.util` — see [[Utilities]] |
| `mccore.gui` — `MapMenu`, `MapImage`, map-item rendering | **No replacement.** Unused, and `codex.manager.api.menu` is inventory GUIs — a different thing entirely. |
| `mccore.sql` | Your own persistence layer |

`mccore.gui`, `mccore.items` and `mccore.commands` have no callers outside their own packages — they
are dead code rather than merely deprecated.

> **Import trap:** `mccore.commands.CommandManager` and `codex.commands.CommandManager` share a simple
> name. The live interfaces are in `codex.commands.api`; only `CommandManager`, `CommandRegister` and
> `UnstuckCommand` sit directly in `codex.commands`.

Two config settings belong to this layer and are documented as deprecated in [[Configuration]]:
`file-timings`, and the `Features` toggles. `Settings.command-cooldown-message` applies only to
mccore commands too — the current framework has no cooldown mechanism.

## Where to go next

- [[Actions Engine]] — the YAML action system
- [[Compat and NMS]] — **read before touching any version-specific API**
- [[Menus and GUIs]] — paged inventory menus
- [[Item Providers]] — resolving items across Oraxen, Nexo, and ItemsAdder
- [[Events]] — what Codex fires
- [[Utilities]] — helpers that save writing your own
