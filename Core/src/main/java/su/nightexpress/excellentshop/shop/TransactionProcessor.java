package su.nightexpress.excellentshop.shop;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class TransactionProcessor {

    private final ExecutorService queue;

    public TransactionProcessor() {
        this.queue = Executors.newSingleThreadExecutor();
    }

    public void shutdown() {
        this.queue.shutdown();
    }

    @NonNull
    public CompletableFuture<Boolean> queueTransaction(@NonNull Supplier<Boolean> supplier) {
        return CompletableFuture.supplyAsync(supplier, this.queue);
    }
}
