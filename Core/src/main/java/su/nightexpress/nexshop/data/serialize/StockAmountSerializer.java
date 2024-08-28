package su.nightexpress.nexshop.data.serialize;

import com.google.gson.*;
import su.nightexpress.nexshop.product.stock.StockAmount;

import java.lang.reflect.Type;

public class StockAmountSerializer implements JsonSerializer<StockAmount>, JsonDeserializer<StockAmount> {

    @Override
    public StockAmount deserialize(JsonElement element, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();

        int itemsLeft = object.get("itemsLeft").getAsInt();
        long restockDate = object.get("restockDate").getAsLong();

        return new StockAmount(itemsLeft, restockDate);
    }

    @Override
    public JsonElement serialize(StockAmount amounts, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject object = new JsonObject();

        object.addProperty("itemsLeft", amounts.getItemsLeft());
        object.addProperty("restockDate", amounts.getRestockDate());

        return object;
    }
}
