package su.nightexpress.excellentshop.data.serialize;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import su.nightexpress.excellentshop.product.content.ItemContent;
import su.nightexpress.nightcore.bridge.item.AdaptedItem;
import su.nightexpress.nightcore.integration.item.bukkit.BukkitItem;
import su.nightexpress.nightcore.util.ItemTag;

public class ItemProductTypeSerializer implements JsonSerializer<ItemContent>, JsonDeserializer<ItemContent> {

    @Override
    public ItemContent deserialize(JsonElement json, Type typeOfT,
                                   JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();

        /* if (object.has("adapter")) {
            String adapterName = object.get("adapter").getAsString();
            ItemProvider adapter = ItemBridge.get().getProvider(adapterName);
            if (!(adapter instanceof BaseIdentifiableItemProvider itemAdapter)) return null;
        
            String itemId = object.get("itemId").getAsString();
            int amount = object.get("amount").getAsInt();
        
            return new ItemContent(new BaseProviderDataItem<ItemIdData>(new ItemIdData(itemId, amount)), true);
        }
        else  */if (object.has("itemTag")) {
            String itemTag = object.get("itemTag").getAsString();
            int dataVersion = object.get("dataVersion").getAsInt();

            return new ItemContent(new BukkitItem(new ItemTag(itemTag, dataVersion)), true);
        }

        return null;
    }

    @Override
    public JsonElement serialize(ItemContent type, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();

        AdaptedItem adaptedItem = type.getAdaptedItem();
        if (adaptedItem instanceof BukkitItem vanillaStack) {
            object.addProperty("itemTag", vanillaStack.getData().getTag());
            object.addProperty("dataVersion", vanillaStack.getData().getDataVersion());
        }
        /* else if (adaptedItem instanceof AdaptedCustomStack customStack) {
            object.addProperty("adapter", customStack.getAdapter().getName());
            object.addProperty("itemId", customStack.getData().getItemId());
            object.addProperty("amount", customStack.getAmount());
        } */

        return object;
    }
}
