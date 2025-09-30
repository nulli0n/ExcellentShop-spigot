package su.nightexpress.nexshop.data.serialize;

import com.google.gson.*;
import su.nightexpress.nexshop.product.content.impl.ItemContent;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.nightcore.bridge.item.ItemAdapter;
import su.nightexpress.nightcore.integration.item.ItemBridge;
import su.nightexpress.nightcore.integration.item.adapter.IdentifiableItemAdapter;
import su.nightexpress.nightcore.integration.item.data.ItemIdData;
import su.nightexpress.nightcore.integration.item.impl.AdaptedCustomStack;
import su.nightexpress.nightcore.integration.item.impl.AdaptedVanillaStack;
import su.nightexpress.nightcore.util.ItemTag;

import java.lang.reflect.Type;

public class ItemProductTypeSerializer implements JsonSerializer<ItemContent>, JsonDeserializer<ItemContent> {

    @Override
    public ItemContent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();

        if (object.has("adapter")) {
            String adapterName = object.get("adapter").getAsString();
            ItemAdapter<?> adapter = ItemBridge.getAdapter(adapterName);
            if (!(adapter instanceof IdentifiableItemAdapter itemAdapter)) return null;

            String itemId = object.get("itemId").getAsString();
            int amount = object.get("amount").getAsInt();

            return new ItemContent(new AdaptedCustomStack(itemAdapter, new ItemIdData(itemId, amount)), true);
        }
        else if (object.has("itemTag")) {
            String itemTag = object.get("itemTag").getAsString();
            int dataVersion = object.get("dataVersion").getAsInt();

            return new ItemContent(new AdaptedVanillaStack(new ItemTag(itemTag, dataVersion)), true);
        }

        return null;
    }

    @Override
    public JsonElement serialize(ItemContent type, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();

        AdaptedItem adaptedItem = type.getAdaptedItem();
        if (adaptedItem instanceof AdaptedVanillaStack vanillaStack) {
            object.addProperty("itemTag", vanillaStack.getData().getTag());
            object.addProperty("dataVersion", vanillaStack.getData().getDataVersion());
        }
        else if (adaptedItem instanceof AdaptedCustomStack customStack) {
            object.addProperty("adapter", customStack.getAdapter().getName());
            object.addProperty("itemId", customStack.getData().getItemId());
            object.addProperty("amount", customStack.getAmount());
        }

        return object;
    }
}
