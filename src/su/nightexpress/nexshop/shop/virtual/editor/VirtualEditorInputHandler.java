package su.nightexpress.nexshop.shop.virtual.editor;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.editor.EditorInputHandler;
import su.nightexpress.nexshop.ExcellentShop;

public abstract class VirtualEditorInputHandler<T> implements EditorInputHandler<VirtualEditorType, T> {

    protected ExcellentShop plugin;

    public VirtualEditorInputHandler(@NotNull ExcellentShop plugin) {
        this.plugin = plugin;
    }
}
