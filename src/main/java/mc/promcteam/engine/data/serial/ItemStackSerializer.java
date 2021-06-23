package mc.promcteam.engine.data.serial;

import com.google.gson.*;
import org.bukkit.inventory.ItemStack;
import mc.promcteam.engine.utils.ItemUT;

import java.lang.reflect.Type;

public class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack item, Type type, JsonSerializationContext context) {
        JsonObject o = new JsonObject();
        try {
            o.addProperty("data64", ItemUT.toBase64(item));
        } catch (Exception e) {
            System.err.println("Could not convert to Base64!");
            e.printStackTrace();
        }

        return o;
    }

    @Override
    public ItemStack deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject o = json.getAsJsonObject();
        ItemStack item = ItemUT.fromBase64(o.get("data64").getAsString());

        return item;
    }

}
