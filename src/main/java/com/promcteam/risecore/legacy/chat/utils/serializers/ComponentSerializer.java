package com.promcteam.risecore.legacy.chat.utils.serializers;

import com.google.gson.*;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

import java.lang.reflect.Type;
import java.util.HashSet;


public class ComponentSerializer implements JsonDeserializer<BaseComponent>
{
    public static final Gson                                gson                 = (new GsonBuilder()).registerTypeAdapter(BaseComponent.class, new ComponentSerializer()).registerTypeAdapter(TextComponent.class, new TextComponentSerializer()).registerTypeAdapter(TranslatableComponent.class, new TranslatableComponentSerializer()).create();
    public static final ThreadLocal<HashSet<BaseComponent>> serializedComponents = new ThreadLocal<>();

    public static BaseComponent[] parse(String json)
    {
        return json.startsWith("[") ? gson.fromJson(json, BaseComponent[].class) : new BaseComponent[]{(BaseComponent) gson.fromJson(json, BaseComponent.class)};
    }

    public static String toString(BaseComponent component)
    {
        return gson.toJson(component);
    }

    public static String toString(BaseComponent... components)
    {
        return gson.toJson(new TextComponent(components));
    }

    @Override
    public BaseComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
    {
        if (json.isJsonPrimitive())
        {
            return new TextComponent(json.getAsString());
        }
        else
        {
            JsonObject object = json.getAsJsonObject();
            return object.has("translate") ? (BaseComponent) context.deserialize(json, TranslatableComponent.class) : (BaseComponent) context.deserialize(json, TextComponent.class);
        }
    }
}
