package su.nightexpress.nexshop.data.serialize;

import com.google.gson.*;
import su.nightexpress.nightcore.util.ItemTag;

import java.lang.reflect.Type;

@Deprecated
public class ItemTagSerializer implements JsonSerializer<ItemTag>, JsonDeserializer<ItemTag> {

    @Override
    public ItemTag deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();

        String tag = object.get("tag").getAsString();
        int dataVersion = object.get("dataVersion").getAsInt();

        return new ItemTag(tag, dataVersion);
    }

    @Override
    public JsonElement serialize(ItemTag src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();

        object.addProperty("tag", src.getTag());
        object.addProperty("dataVersion", src.getDataVersion());

        return object;
    }
}
