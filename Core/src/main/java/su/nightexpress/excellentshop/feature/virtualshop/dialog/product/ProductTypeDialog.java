package su.nightexpress.excellentshop.feature.virtualshop.dialog.product;

import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.core.Lang;
import su.nightexpress.excellentshop.product.ContentType;
import su.nightexpress.excellentshop.product.ContentTypes;
import su.nightexpress.excellentshop.product.ProductContent;
import su.nightexpress.excellentshop.feature.virtualshop.VirtualShopModule;
import su.nightexpress.excellentshop.feature.virtualshop.product.VirtualProduct;
import su.nightexpress.excellentshop.feature.virtualshop.core.VirtualLang;
import su.nightexpress.nightcore.bridge.dialog.wrap.WrappedDialog;
import su.nightexpress.nightcore.bridge.dialog.wrap.button.WrappedActionButton;
import su.nightexpress.nightcore.locale.entry.ButtonLocale;
import su.nightexpress.nightcore.locale.entry.DialogElementLocale;
import su.nightexpress.nightcore.locale.entry.TextLocale;
import su.nightexpress.nightcore.ui.dialog.Dialogs;
import su.nightexpress.nightcore.ui.dialog.build.*;
import su.nightexpress.nightcore.ui.dialog.wrap.Dialog;
import su.nightexpress.nightcore.util.LowerCase;
import su.nightexpress.nightcore.util.text.night.wrapper.TagWrappers;

import java.util.ArrayList;
import java.util.List;

public class ProductTypeDialog extends Dialog<VirtualProduct> {

    private static final TextLocale          TITLE = VirtualLang.builder("Dialog.Product.ContentType.Title").text(title("Product", "Type"));
    private static final DialogElementLocale BODY  = VirtualLang.builder("Dialog.Product.ContentType.Body").dialogElement(
        400,
        "Select the desired product type.",
        "",
        TagWrappers.SOFT_YELLOW.wrap("→") + " Item " + TagWrappers.GRAY.wrap("(Physical)") + " - Standard buying and selling of a specific item.",
        TagWrappers.SOFT_YELLOW.wrap("→") + " Command " + TagWrappers.GRAY.wrap("(Virtual)") + " - Executes specified commands upon purchase. Cannot be sold.",
        "",
        TagWrappers.SOFT_RED.wrap("All current product settings dependent on the previous type will be " + TagWrappers.UNDERLINED.wrap("OVERWRITTEN") + "!")
    );

    private static final ButtonLocale BUTTON_TYPE = VirtualLang.builder("Dialog.Product.ContentType.Button.Type")
        .button(TagWrappers.SOFT_YELLOW.wrap("→ ") + ShopPlaceholders.GENERIC_TYPE);

    private final VirtualShopModule module;

    public ProductTypeDialog(@NonNull VirtualShopModule module) {
        this.module = module;
    }

    @Override
    @NonNull
    public WrappedDialog create(@NonNull Player player, @NonNull VirtualProduct product) {
        List<WrappedActionButton> buttons = new ArrayList<>();
        WrappedDialog.Builder builder = Dialogs.builder();

        for (ContentType type : ContentType.values()) {
            if (type == ContentType.EMPTY || type == product.getContent().type()) continue;

            String actionId = LowerCase.INTERNAL.apply(type.name());
            ButtonLocale locale = BUTTON_TYPE.replace(str -> str.replace(ShopPlaceholders.GENERIC_TYPE, Lang.CONTENT_TYPE.getLocalized(type)));
            buttons.add(DialogButtons.action(locale).action(DialogActions.customClick(actionId)).build());

            builder.handleResponse(actionId, (viewer, identified, nbtHolder) -> {
                ProductContent content = ContentTypes.create(type, product.getEffectivePreview(), this.module::isItemProviderAllowed);
                product.setContent(content);
                product.getShop().markDirty();
                viewer.callback();
            });
        }

        builder.base(DialogBases.builder(TITLE)
            .body(DialogBodies.plainMessage(BODY))
            .build()
        );
        builder.type(DialogTypes.multiAction(buttons).columns(1).exitAction(DialogButtons.back()).build());

        return builder.build();
    }
}
