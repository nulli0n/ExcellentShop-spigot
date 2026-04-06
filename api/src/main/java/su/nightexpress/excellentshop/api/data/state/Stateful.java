package su.nightexpress.excellentshop.api.data.state;

public interface Stateful {

    boolean isDirty();

    boolean isClean();

    boolean isRemoved();

    void markDirty();

    void markClean();

    void markRemoved();
}
