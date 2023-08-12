package su.nightexpress.nexshop.data.rotation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.shop.Shop;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ShopRotationStorage {

    private static final Map<String, ShopRotationData> DATAS = new HashMap<>();

    @NotNull
    public static CompletableFuture<Void> loadData(@NotNull Shop<?, ?> shop) {
        return loadData(shop.getId());
    }

    @NotNull
    private static CompletableFuture<Void> loadData(@NotNull String shopId) {
        DATAS.remove(shopId);
        return CompletableFuture.runAsync(() -> {
            ShopRotationData data = ShopAPI.getDataHandler().getVirtualDataHandler().getShopRotationData(shopId);
            if (data == null) return;

            addData(data);
        });
    }

    private static void addData(@NotNull ShopRotationData rotationData) {
        DATAS.put(rotationData.getShopId(), rotationData);
    }

    private static void removeData(@NotNull VirtualShop<?, ?> shop) {
        DATAS.remove(shop.getId());
    }

    @Nullable
    public static ShopRotationData getData(@NotNull String shopId) {
        return DATAS.get(shopId);
    }

    public static void createData(@NotNull ShopRotationData rotationData) {
        if (getData(rotationData.getShopId()) != null) return;

        addData(rotationData);

        ShopAPI.PLUGIN.runTaskAsync(task -> ShopAPI.getDataHandler().getVirtualDataHandler().createShopRotationData(rotationData));
    }

    public static void saveData(@NotNull ShopRotationData rotationData) {
        ShopAPI.PLUGIN.runTaskAsync(task -> ShopAPI.getDataHandler().getVirtualDataHandler().saveShopRotationData(rotationData));
    }

    public static void deleteData(@NotNull VirtualShop<?, ?> shop) {
        if (getData(shop.getId()) == null) return;

        removeData(shop);

        ShopAPI.PLUGIN.runTaskAsync(task -> ShopAPI.getDataHandler().getVirtualDataHandler().removeShopRotationData(shop));
    }
}
