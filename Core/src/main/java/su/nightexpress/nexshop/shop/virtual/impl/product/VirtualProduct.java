package su.nightexpress.nexshop.shop.virtual.impl.product;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.utils.PlayerUtil;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.api.shop.CommandProduct;
import su.nightexpress.nexshop.api.shop.ItemProduct;
import su.nightexpress.nexshop.api.shop.Product;
import su.nightexpress.nexshop.api.shop.ProductPricer;
import su.nightexpress.nexshop.api.type.TradeType;
import su.nightexpress.nexshop.currency.CurrencyManager;
import su.nightexpress.nexshop.shop.virtual.editor.menu.ProductMainEditor;
import su.nightexpress.nexshop.shop.virtual.impl.product.specific.CommandSpecific;
import su.nightexpress.nexshop.shop.virtual.impl.product.specific.ItemSpecific;
import su.nightexpress.nexshop.shop.virtual.impl.product.specific.ProductSpecific;
import su.nightexpress.nexshop.shop.virtual.impl.shop.VirtualShop;
import su.nightexpress.nexshop.shop.virtual.util.Placeholders;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class VirtualProduct<
    P extends VirtualProduct<P, S>,
    S extends VirtualShop<S, P>> extends Product<P, S, VirtualProductStock<P>> {

    protected ProductSpecific specific;
    protected ProductMainEditor editor;
    protected Set<String>     allowedRanks;


    public VirtualProduct(@NotNull String id, @NotNull ProductSpecific specific, @NotNull Currency currency) {
        super(id, currency);
        this.allowedRanks = new HashSet<>();
        this.specific = specific;

        this.placeholderMap
            .add(Placeholders.PRODUCT_ALLOWED_RANKS, () -> String.join("\n", this.getAllowedRanks()));
    }

    @NotNull
    public static <T extends VirtualProduct<T, ?>> T read(@NotNull JYML cfg, @NotNull String path, @NotNull String id, @NotNull Class<T> clazz) {
        String currencyId = cfg.getString(path + ".Currency", CurrencyManager.VAULT);
        Currency currency = ShopAPI.getCurrencyManager().getCurrency(currencyId);
        if (currency == null) {
            throw new IllegalStateException("Invalid currency!");
        }

        ItemStack item = cfg.getItemEncoded(path + ".Content.Item");

        ProductSpecific spec;
        if (item != null && !item.getType().isAir()) {
            spec = new ItemSpecific(item);
            ((ItemSpecific) spec).setRespectItemMeta(cfg.getBoolean(path + ".Item_Meta_Enabled"));
        }
        else {
            ItemStack preview = cfg.getItemEncoded(path + ".Content.Preview");
            if (preview == null) preview = new ItemStack(Material.COMMAND_BLOCK);

            List<String> commands = cfg.getStringList(path + ".Content.Commands");
            spec = new CommandSpecific(preview, commands);
        }

        T product;
        if (clazz == StaticProduct.class) {
            StaticProduct staticProduct = new StaticProduct(id, spec, currency);
            staticProduct.setSlot(cfg.getInt(path + ".Shop_View.Slot", -1));
            staticProduct.setPage(cfg.getInt(path + ".Shop_View.Page", -1));
            staticProduct.setDiscountAllowed(cfg.getBoolean(path + ".Discount.Allowed"));
            product = clazz.cast(staticProduct);
        }
        else if (clazz == RotatingProduct.class) {
            RotatingProduct rotatingProduct = new RotatingProduct(id, spec, currency);
            rotatingProduct.setRotationChance(cfg.getDouble(path + ".Rotation.Chance"));
            product = clazz.cast(rotatingProduct);
        }
        else {
            throw new UnsupportedOperationException("Unsupported product class provided!");
        }
        product.setAllowedRanks(cfg.getStringSet(path + ".Allowed_Ranks"));
        product.setPricer(ProductPricer.read(cfg, path + ".Price"));
        product.setStock(VirtualProductStock.read(cfg, path + ".Stock", clazz));
        return product;
    }

    public void write(@NotNull JYML cfg, @NotNull String path) {
        cfg.remove(path + ".Content");
        if (this.getSpecific() instanceof CommandProduct commandProduct) {
            cfg.setItemEncoded(path + ".Content.Preview", commandProduct.getPreview());
            cfg.set(path + ".Content.Commands", commandProduct.getCommands());
        }
        else if (this.getSpecific() instanceof ItemProduct itemProduct) {
            cfg.setItemEncoded(path + ".Content.Item", itemProduct.getItem());
            cfg.set(path + ".Item_Meta_Enabled", itemProduct.isRespectItemMeta());
        }
        cfg.set(path + ".Allowed_Ranks", this.getAllowedRanks());
        VirtualProductStock.write(this.getStock(), cfg, path + ".Stock");
        ProductPricer pricer = this.getPricer();
        cfg.set(path + ".Currency", this.getCurrency().getId());
        cfg.set(path + ".Price.Type", pricer.getType().name());
        pricer.write(cfg, path + ".Price");
        this.writeAdditionalData(cfg, path);
    }

    protected abstract void writeAdditionalData(@NotNull JYML cfg, @NotNull String path);

    public void clear() {
        if (this.editor != null) {
            this.editor.clear();
            this.editor = null;
        }
    }

    @NotNull
    public ProductMainEditor getEditor() {
        if (this.editor == null) {
            this.editor = new ProductMainEditor(this.getShop().plugin(), this);
        }
        return this.editor;
    }

    @Override
    @NotNull
    public VirtualPreparedProduct<P> getPrepared(@NotNull Player player, @NotNull TradeType buyType, boolean all) {
        return new VirtualPreparedProduct<>(player, this.get(), buyType, all);
    }

    @NotNull
    public ProductSpecific getSpecific() {
        return specific;
    }

    public boolean hasAccess(@NotNull Player player) {
        if (this.getAllowedRanks().isEmpty()) return true;

        Set<String> ranks = PlayerUtil.getPermissionGroups(player);
        return ranks.stream().anyMatch(rank -> this.getAllowedRanks().contains(rank));
    }

    @Override
    public void delivery(@NotNull Player player, int count) {
        this.getSpecific().delivery(player, count);
    }

    @Override
    public void take(@NotNull Player player, int count) {
        this.getSpecific().take(player, count);
    }

    @Override
    public int count(@NotNull Player player) {
        return this.getSpecific().count(player);
    }

    @Override
    @NotNull
    public ItemStack getPreview() {
        return this.getSpecific().getPreview();
    }

    @Override
    public int getUnitAmount() {
        return this.getSpecific().getUnitAmount();
    }

    @Override
    public boolean hasSpace(@NotNull Player player) {
        return this.getSpecific().hasSpace(player);
    }

    @NotNull
    public Set<String> getAllowedRanks() {
        return allowedRanks;
    }

    public void setAllowedRanks(@NotNull Set<String> allowedRanks) {
        this.allowedRanks = allowedRanks;
    }
}
