package su.nightexpress.nexshop.api.shop;

@Deprecated
public interface IVirtualShop /*extends Shop*/ {

//    @NotNull File getFile();
//
//    boolean load();
//
//    boolean hasPermission(@NotNull Player player);
//
//    void performRotation();
//
//    void performRotation(@NotNull Rotation rotation);
//
//    @NotNull Set<Discount> getDiscounts();
//
//    @NotNull NightItem getIcon();
//
//    void setIcon(@NotNull NightItem icon);
//
//    @NotNull String getDefaultLayout();
//
//    void setDefaultLayout(@NotNull String layoutName);
//
//    @NotNull String getLayout(int page);
//
//    void setLayout(int page, @Nullable String layoutName);
//
//    @NotNull List<String> getDescription();
//
//    void setDescription(@NotNull List<String> description);
//
//    boolean isPermissionRequired();
//
//    void setPermissionRequired(boolean permissionRequired);
//
//    int getMainMenuSlot();
//
//    void setMainMenuSlot(int mainMenuSlot);
//
//    default boolean isMainMenuSlotDisabled() {
//        return this.getMainMenuSlot() < 0;
//    }
//
//    int getPages();
//
//    void setPages(int pages);

//    default boolean hasDiscount() {
//        return this.getDiscountPlain() != 0D;
//    }
//
//    default boolean hasDiscount(@NotNull IVirtualProduct product) {
//        return this.getDiscountPlain(product) != 0D;
//    }
//
//    default double getDiscountModifier() {
//        return 1D - this.getDiscountPlain() / 100D;
//    }
//
//    default double getDiscountModifier(@NotNull IVirtualProduct product) {
//        return 1D - this.getDiscountPlain(product) / 100D;
//    }
//
//    default double getDiscountPlain() {
//        return Math.min(100D, this.getDiscounts().stream().mapToDouble(Discount::getDiscountPlain).sum());
//    }
//
//    default double getDiscountPlain(@NotNull IVirtualProduct product) {
//        return product.isDiscountAllowed() ? this.getDiscountPlain() : 0D;
//    }

    //void addProduct(@NotNull VirtualProduct product);

//    void addRotation(@NotNull Rotation rotation);
//
//    void removeRotation(@NotNull Rotation rotation);
//
//    @NotNull Set<Rotation> getRotations();
//
//    @Nullable Rotation getRotationById(@NotNull String id);

//    @NotNull Map<String, ? extends IVirtualProduct> getProductMap();
//
//    @NotNull Collection<? extends IVirtualProduct> getProducts();
//
//    @NotNull Collection<? extends IVirtualProduct> getValidProducts();

//    @Nullable VirtualProduct getProductById(@NotNull String id);
//
//    @Nullable VirtualProduct getBestProduct(@NotNull ItemStack item, @NotNull TradeType tradeType);
//
//    @Nullable VirtualProduct getBestProduct(@NotNull ItemStack item, @NotNull TradeType tradeType, @Nullable Player player);
}
