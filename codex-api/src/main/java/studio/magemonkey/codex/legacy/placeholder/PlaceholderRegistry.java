package studio.magemonkey.codex.legacy.placeholder;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import studio.magemonkey.codex.api.VersionManager;
import studio.magemonkey.codex.util.EnumUT;

public final class PlaceholderRegistry {
    public static final PlaceholderType<Location>    LOCATION    = PlaceholderType.create("location", Location.class);
    public static final PlaceholderType<Block>       BLOCK       = PlaceholderType.create("block", Block.class);
    public static final PlaceholderType<Chunk>       CHUNK       = PlaceholderType.create("chunk", Chunk.class);
    public static final PlaceholderType<Enchantment> ENCHANTMENT =
            PlaceholderType.create("enchantment", Enchantment.class);
    public static final PlaceholderType<Command>     COMMAND     = PlaceholderType.create("command", Command.class);
    public static final PlaceholderType<Plugin>      PLUGIN      = PlaceholderType.create("plugin", Plugin.class);
    public static final PlaceholderType<JavaPlugin>  JAVA_PLUGIN =
            PlaceholderType.create("javaPlugin", JavaPlugin.class, PLUGIN);
    public static final PlaceholderType<ItemStack>   ITEM        = PlaceholderType.create("item", ItemStack.class);

    public static final PlaceholderType<CommandSender> SENDER        =
            PlaceholderType.create("sender", CommandSender.class);
    public static final PlaceholderType<Entity>        ENTITY        = PlaceholderType.create("entity", Entity.class);
    public static final PlaceholderType<LivingEntity>  LIVING_ENTITY =
            PlaceholderType.create("livingEntity", LivingEntity.class, ENTITY);
    public static final PlaceholderType<Player>        PLAYER        =
            PlaceholderType.create("player", Player.class, ENTITY, SENDER);

//    public static final PlaceholderType<DarkRisePlugin> RISE_PLUGIN     = PlaceholderType.create("risePlugin", DarkRisePlugin.class);
//    public static final PlaceholderType<DarkRiseCore>   CORE            = PlaceholderType.create("core", DarkRiseCore.class, RISE_PLUGIN);
//    public static final PlaceholderType<BlockLocation>  BLOCK_LOCATION  = PlaceholderType.create("blockLocation", BlockLocation.class);
//    public static final PlaceholderType<PlayerLocation> PLAYER_LOCATION = PlaceholderType.create("playerLocation", PlayerLocation.class);

    public static final PlaceholderType<World> WORLD = PlaceholderType.create("world", World.class);

    private PlaceholderRegistry() {
    }

    public static void load() {
//        BLOCK_LOCATION.registerItem("x", BlockLocation::getX);
//        BLOCK_LOCATION.registerItem("y", BlockLocation::getY);
//        BLOCK_LOCATION.registerItem("z", BlockLocation::getZ);
        BLOCK.registerItem("z", Block::getZ);
        BLOCK.registerItem("x", Block::getX);
        BLOCK.registerItem("y", Block::getY);
        BLOCK.registerItem("type", b -> b.getType().name().toLowerCase());
        BLOCK.registerItem("biome", b -> EnumUT.getName(b.getBiome()).toLowerCase());
        BLOCK.registerItem("blockPower", Block::getBlockPower);
        BLOCK.registerItem("tmperature", b -> ((int) (b.getTemperature() * 10)) / 10);
        BLOCK.registerItem("humidity", b -> ((int) (b.getHumidity() * 10)) / 10);
        BLOCK.registerItem("lightFromBlocks", Block::getLightFromBlocks);
        BLOCK.registerItem("lightFromSky", Block::getLightFromSky);
        BLOCK.registerItem("lightLevel", Block::getLightLevel);
        BLOCK.registerItem("chunkX", (block) -> block.getChunk().getX());
        BLOCK.registerItem("chunkZ", (block) -> block.getChunk().getZ());
        BLOCK.registerItem("world", (block) -> block.getWorld().getName());
//        PLAYER_LOCATION.registerItem("x", (location) -> ((int) (location.getX() * 10)) / 10);
//        PLAYER_LOCATION.registerItem("y", (location) -> ((int) (location.getY() * 10)) / 10);
//        PLAYER_LOCATION.registerItem("z", (location) -> ((int) (location.getZ() * 10)) / 10);

//        RISE_PLUGIN.registerItem("name", DarkRisePlugin::getName);

        SENDER.registerItem("name", CommandSender::getName);
        ENTITY.registerItem("name", Entity::getName);
        ENTITY.registerItem("entityId", Entity::getEntityId);
        ENTITY.registerItem("type", e -> e.getType().name().toLowerCase());
        ENTITY.registerItem("uuid", e -> e.getUniqueId().toString());
        LIVING_ENTITY.registerItem("health", LivingEntity::getHealth);
        LIVING_ENTITY.registerItem("maxHealth", LivingEntity::getMaxHealth);
        LIVING_ENTITY.registerItem("remainingAir", LivingEntity::getRemainingAir);
        LIVING_ENTITY.registerItem("health", LivingEntity::getHealth);
        LOCATION.registerItem("x", Location::getBlockX);
        LOCATION.registerItem("y", Location::getBlockY);
        LOCATION.registerItem("z", Location::getBlockZ);
        LOCATION.registerItem("yaw", Location::getYaw);
        LOCATION.registerItem("pitch", Location::getPitch);
        LOCATION.registerItem("exactX", Location::getX);
        LOCATION.registerItem("exactY", Location::getY);
        LOCATION.registerItem("exactZ", Location::getZ);
        LOCATION.registerItem("world", l -> l.getWorld().getName());
        WORLD.registerItem("name", w -> w.getName());
        WORLD.registerItem("difficulty", w -> w.getDifficulty().name().toLowerCase());
        WORLD.registerItem("environment", w -> w.getEnvironment().name().toLowerCase());
        WORLD.registerItem("seed", w -> w.getSeed());
        WORLD.registerItem("uuid", w -> w.getUID().toString());
        WORLD.registerItem("type", w -> w.getWorldType().name().toLowerCase());
        WORLD.registerItem("folder", w -> w.getWorldFolder().getPath());
        ITEM.registerItem("amount", ItemStack::getAmount);
        ITEM.registerItem("durability", ItemStack::getDurability);
        ITEM.registerItem("material", i ->
        {
            TextComponent textComponent = new TextComponent(i.getType().name().toLowerCase());
            textComponent.setHoverEvent(VersionManager.getNms().getHoverEvent(i));
            return textComponent;
        });
        ITEM.registerItem("displayName", i ->
        {
            if (!i.hasItemMeta()) {
                return "";
            }
            TextComponent textComponent =
                    new TextComponent(i.getItemMeta().hasDisplayName() ? i.getItemMeta().getDisplayName() : "");
            textComponent.setHoverEvent(VersionManager.getNms().getHoverEvent(i));
            return textComponent;
        });
        ITEM.registerItem("lore", i -> i.getItemMeta().getLore());

        PLAYER.registerItem("xpLevel", Player::getLevel);
        PLAYER.registerItem("exp", Player::getExp);
        PLAYER.registerItem("totalExperience", Player::getTotalExperience);
        PLAYER.registerItem("expToLevel", HumanEntity::getExpToLevel);

        ENCHANTMENT.registerItem("id", e -> e/*.getId()*/.getKey().getKey()); //getKey used in 1.13+
        ENCHANTMENT.registerItem("name", e -> e/*.getName()*/.getKey().getKey());
        ENCHANTMENT.registerItem("maxLevel", Enchantment::getMaxLevel);
        ENCHANTMENT.registerItem("startLevel", Enchantment::getStartLevel);
        ENCHANTMENT.registerItem("target", e -> e.getItemTarget().name().toLowerCase());

        COMMAND.registerItem("name", Command::getName);
        COMMAND.registerItem("description", Command::getDescription);
        COMMAND.registerItem("label", Command::getLabel);
        COMMAND.registerItem("permission", Command::getPermission);
        COMMAND.registerItem("permissionMessage", Command::getPermissionMessage);
        COMMAND.registerItem("usage", Command::getUsage);
        COMMAND.registerItem("aliases", Command::getAliases);

        PLUGIN.registerItem("name", Plugin::getName);
        PLUGIN.registerItem("description", Plugin::getDescription);
        PLUGIN.registerItem("folder", p -> p.getDataFolder().toPath());
        JAVA_PLUGIN.registerItem("authors", p -> p.getDescription().getAuthors());
        JAVA_PLUGIN.registerItem("depend", p -> p.getDescription().getDepend());
        JAVA_PLUGIN.registerItem("fullName", p -> p.getDescription().getFullName());
        JAVA_PLUGIN.registerItem("main", p -> p.getDescription().getMain());
        JAVA_PLUGIN.registerItem("website", p -> p.getDescription().getWebsite());
        JAVA_PLUGIN.registerItem("version", p -> p.getDescription().getVersion());
        JAVA_PLUGIN.registerItem("prefix", p -> p.getDescription().getPrefix());
        JAVA_PLUGIN.registerItem("softDepend", p -> p.getDescription().getSoftDepend());
        JAVA_PLUGIN.registerItem("loadBefore", p -> p.getDescription().getLoadBefore());

        BLOCK.registerChild("chunk", CHUNK, Block::getChunk);
        BLOCK.registerChild("world", WORLD, Block::getWorld);
        BLOCK.registerChild("blockLocation", LOCATION, b -> new Location(b.getWorld(), b.getX(), b.getY(), b.getZ()));
        CHUNK.registerChild("world", WORLD, Chunk::getWorld);
//        RISE_PLUGIN.registerChild("core", CORE, p -> ((DarkRiseCore) p.getCore()));
        PLAYER.registerChild("world", WORLD, Player::getWorld);
        ENTITY.registerChild("location", LOCATION, Entity::getLocation);
        ENTITY.registerChild("loc", LOCATION, Entity::getLocation);
        LOCATION.registerChild("world", WORLD, Location::getWorld);
    }
}