package su.nightexpress.nexshop.shop.chest.editor;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorInputHandler;
import su.nightexpress.nexshop.ExcellentShop;

public abstract class ChestEditorInputHandler<T> implements EditorInputHandler<ChestEditorType, T> {

    protected ExcellentShop plugin;

    public ChestEditorInputHandler(@NotNull ExcellentShop plugin) {
        this.plugin = plugin;
    }
}
