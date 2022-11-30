package su.nightexpress.nexshop.data.serialize;

@Deprecated
public class ProductStockDataSerializer /*implements JsonSerializer<ProductStockData>, JsonDeserializer<ProductStockData>*/ {

    /*@Override
    public ProductStockData deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();

        TradeType tradeType = CollectionsUtil.getEnum(object.get("tradeType").getAsString(), TradeType.class);
        if (tradeType == null) return null;

        StockType stockType = CollectionsUtil.getEnum(object.get("stockType").getAsString(), StockType.class);
        if (stockType == null) return null;

        String shopId = object.get("shopId").getAsString();
        String productId = object.get("productId").getAsString();
        int itemsLeft = object.get("itemsLeft").getAsInt();
        long restockDate = object.get("restockDate").getAsLong();

        return new ProductStockData(tradeType, stockType, shopId, productId, itemsLeft, restockDate);
    }

    @Override
    public JsonElement serialize(ProductStockData from, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("tradeType", from.getTradeType().name());
        object.addProperty("stockType", from.getStockType().name());
        object.addProperty("shopId", from.getShopId());
        object.addProperty("productId", from.getProductId());
        object.addProperty("itemsLeft", from.getItemsLeft());
        object.addProperty("restockDate", from.getRestockDate());
        return object;
    }*/
}
