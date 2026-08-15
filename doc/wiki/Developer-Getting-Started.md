# Developer Getting Started

Codex is published to the MageMonkey Maven repository at
[repo.travja.dev](https://repo.travja.dev).

## Maven

```xml
<repository>
    <id>magemonkey-snapshots</id>
    <url>https://repo.travja.dev/snapshots</url>
</repository>
```

```xml
<dependency>
    <groupId>studio.magemonkey</groupId>
    <artifactId>codex</artifactId>
    <version>1.2.0-R0.6-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

Release builds are published to `https://repo.travja.dev/releases` under the same coordinates. Check
the repository for the current version — the snippet above reflects the version in the source tree at
the time of writing.

### The `codex-api` artifact

Codex is split into modules. If you only need the API surface — item types, events, utilities, the
compat interfaces — you can depend on the lighter artifact instead:

```xml
<dependency>
    <groupId>studio.magemonkey</groupId>
    <artifactId>codex-api</artifactId>
    <version>1.2.0-R0.6-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

| Module | Contains |
|---|---|
| `codex-api` | Item types, events, menus, utilities, compat interfaces |
| `codex-core` | Attribute/buff/damage registries, migration helpers |
| `codex-plugin` | The Bukkit plugin itself, commands, hooks, actions engine |
| `codex-nms` | Per-version NMS implementations |
| `codex-bungee` | BungeeCord companion plugin |

## Declaring the dependency

In your `plugin.yml`:

```yaml
depend: [ Codex ]
```

Codex declares `provides: ProMCCore`, so `depend: [ ProMCCore ]` also resolves — useful if you are
maintaining an older plugin.

## Accessing the engine

```java
CodexEngine engine = CodexEngine.get();
```

From there:

| Accessor | Returns |
|---|---|
| `getItemManager()` | Item provider registry — see [[Item Providers]] |
| `getMenuManager()` | Menu tracking — see [[Menus and GUIs]] |
| `getActionsManager()` | Actions engine — see [[Actions Engine]] |
| `getHooksManager()` | Hook registry — see [[Hooks]] |
| `getPacketManager()` | Packet handling |
| `getCraftManager()` | Recipe helpers |
| `getVault()` / `getWorldGuard()` / `getMythicMobs()` / `getCitizens()` / `getNexo()` | Individual hooks |
| `getMessageUtil()` | Message formatting |

## Extending `CodexPlugin`

The bigger integration point is `CodexPlugin`. Extending it — as Fabled and Divinity do — gives you
the command framework, config templates, language handling, and editor support for free.

```java
public class MyPlugin extends CodexPlugin<MyPlugin> {

    @Override
    public void enable() {
        // your startup logic
    }

    @Override
    public void disable() {
        // your shutdown logic
    }

    @Override
    public void registerCommands(@NotNull IGeneralCommand<MyPlugin> mainCommand) {
        mainCommand.addSubCommand(new MyCommand<>(this));
    }

    @Override
    public void registerEditor() {
        // register your editor, if you have one
    }
}
```

### What you get automatically

Codex registers a main command for your plugin using the labels from your own config's
`core.command-aliases`, and attaches:

| Subcommand | Permission | Condition |
|---|---|---|
| `help` | `<plugin>.user` | always |
| `reload` | `<plugin>.admin` | always |
| `editor` | `<plugin>.cmd.editor` | only if `hasEditor()` returns true |
| `about` | none | only on non-engine plugins |

`<plugin>` is your plugin's name, lowercased with spaces and hyphens removed. A plugin named
`My-Plugin` produces `myplugin.admin`.

### Useful overrides

| Method | Purpose |
|---|---|
| `enable()` / `disable()` | Your lifecycle hooks |
| `registerCommands(mainCommand)` | Attach your subcommands |
| `registerEditor()` | Set up your editor GUI |
| `hasEditor()` | Return true to get the `editor` subcommand |
| `cfg()` / `lang()` | Your config and language templates |

## Configuration files

Codex provides a config wrapper with comment preservation and an `addMissing` pattern that lets you
add new keys across versions without clobbering user edits:

```java
cfg.addMissing("my.new.setting", true);
```

Register additional config files through the engine:

```java
Config config = CodexEngine.get().getConfigFile(myPlugin, "items.yml");
CodexEngine.get().registerConfig(config);
```

## Where to go next

- [[Actions Engine]] — the YAML action system, if your plugin exposes configurable effects
- [[Compat and NMS]] — **read this before touching any version-specific API**
- [[Menus and GUIs]] — paged inventory menus
- [[Item Providers]] — resolving items across Oraxen, Nexo, and ItemsAdder
- [[Events]] — what Codex fires that you can listen to
- [[Utilities]] — helpers that save writing your own
