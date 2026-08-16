# Chat Module

> ## ⚠️ Deprecated
>
> The chat module is part of the legacy **mccore** codebase, inherited from ProMCCore. It is
> **deprecated and not recommended for new setups.** It still works and is documented here for
> servers already relying on it, but it is not actively developed and may be removed in a future
> release.
>
> **Use a dedicated chat plugin instead** — EssentialsX Chat, LuckPerms meta prefixes, or any modern
> chat formatter. Set `Features.chat-enabled: false` in [[Configuration]] to disable this module.

Codex includes an optional chat module providing per-player display names and unlockable prefixes.

**Enable or disable it** in `config.yml` (see [[Configuration]]):

```yaml
Features:
  chat-enabled: true
```

> If another plugin already manages chat formatting (EssentialsX Chat, LuckPerms meta prefixes, a
> dedicated chat plugin), turn this off. Running two chat formatters usually results in doubled
> prefixes or one silently overwriting the other.

## Commands

| Command | Permission | Description |
|---|---|---|
| `/chat list` | `general.chat.list` | Displays your unlocked prefixes |
| `/chat name <name>` | `general.chat.name` | Sets your display name |
| `/chat prefix <prefix>` | `general.chat.prefix` | Sets your active prefix |
| `/chat reset` | `general.chat.reset` | Resets your display name to default |

See [[Permissions]] for granting these.

## How prefixes work

Prefixes are **unlockable, not free-form**. `/chat prefix <prefix>` checks the player's own unlocked
prefix list first:

- If the player has unlocked that prefix, it is applied and they are told *"The prefix has been set!"*
- If not, they are told *"You do not have that prefix!"* and nothing changes

Players can see what is available to them with `/chat list`. This means granting
`general.chat.prefix` does **not** let a player set an arbitrary prefix — they can only select from
what they have been given.

## Display names

`/chat name <name>` sets the player's display name; `/chat reset` restores the default. Whether
colour codes are permitted in the supplied name depends on your permission setup and any name
restrictions your other plugins impose.

## Persistence — there is none

> ⚠️ **Chat data does not survive a restart.** `ChatData` is registered as savable against a `data`
> config, but nothing ever calls save — not on disable, not on a timer. A hand-written `data.yml`
> *is* read at startup, so prefixes can be seeded by hand, but anything set in game is lost when the
> server stops.

## Known defects

Beyond the deprecation, this module has several outright bugs:

- **`/chat name` drops the last character** of the name you give it — `/chat name Bob` sets `Bo`.
- **`/chat reset` does not clear your prefix**, only the display name, despite the in-game help text
  claiming otherwise.
- **`/chat prefix` takes exactly one argument**, so prefixes containing spaces cannot be selected.
- **Player data leaks on quit** — the quit handler removes by raw name while the store is keyed by
  lowercase, so entries usually survive the disconnect.

## Interaction with Vault

`Hooks.getPrefix` / `getSuffix` read the **player's** Vault chat prefix and suffix — not their
permission group's. Note these helpers have no callers inside Codex, so anything using them is a
downstream plugin. See [[Hooks]].
