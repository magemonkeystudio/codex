# Chat Module

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

## Persistence

Player chat data — display names and unlocked prefixes — is stored by Codex per player and survives
restarts.

## Interaction with Vault

When Vault is present, Codex can read permission-group prefixes and suffixes through it. See
[[Hooks]] for the group-value helpers available to developers.
