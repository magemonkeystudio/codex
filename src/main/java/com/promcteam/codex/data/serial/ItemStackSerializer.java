package com.promcteam.codex.data.serial;

import com.google.gson.*;
import com.promcteam.codex.CodexEngine;
import com.promcteam.codex.util.ItemUT;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;

public class ItemStackSerializer implements JsonSerializer<ItemStack>, JsonDeserializer<ItemStack> {

    @Override
    public JsonElement serialize(ItemStack item, Type type, JsonSerializationContext context) {
        JsonObject o = new JsonObject();
        try {
            o.addProperty("data64", ItemUT.toBase64(item));
        } catch (Exception e) {
            CodexEngine.get().getLogger().warning("Could not convert to Base64!");
            e.printStackTrace();
        }

        return o;
    }

    @Override
    public ItemStack deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject o    = json.getAsJsonObject();
        ItemStack  item = ItemUT.fromBase64(o.get("data64").getAsString());

        return item;
    }

}
