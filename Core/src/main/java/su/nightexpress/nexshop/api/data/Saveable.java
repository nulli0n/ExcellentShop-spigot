package su.nightexpress.nexshop.api.data;

public interface Saveable {

    boolean isSaveRequired();

    void setSaveRequired(boolean saveRequired);
}
