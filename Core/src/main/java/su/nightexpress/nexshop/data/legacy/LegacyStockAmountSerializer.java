package su.nightexpress.nexshop.data.legacy;

import com.google.gson.*;

import java.lang.reflect.Type;

public class LegacyStockAmountSerializer implements JsonSerializer<LegacyStockAmount>, JsonDeserializer<LegacyStockAmount> {

    @Override
    public LegacyStockAmount deserialize(JsonElement element, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();

        int itemsLeft = object.get("itemsLeft").getAsInt();
        long restockDate = object.get("restockDate").getAsLong();

        return new LegacyStockAmount(itemsLeft, restockDate);
    }

    @Override
    public JsonElement serialize(LegacyStockAmount amounts, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = new JsonObject();

        object.addProperty("itemsLeft", amounts.getItemsLeft());
        object.addProperty("restockDate", amounts.getRestockDate());

        return object;
    }
}
