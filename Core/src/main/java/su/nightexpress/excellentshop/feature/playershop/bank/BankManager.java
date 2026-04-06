package su.nightexpress.excellentshop.feature.playershop.bank;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import su.nightexpress.excellentshop.feature.playershop.ChestShopModule;
import su.nightexpress.excellentshop.feature.playershop.bank.data.BankDataManager;
import su.nightexpress.excellentshop.feature.playershop.core.ChestLang;
import su.nightexpress.excellentshop.ShopPlaceholders;
import su.nightexpress.excellentshop.ShopPlugin;
import su.nightexpress.excellentshop.data.DataHandler;
import su.nightexpress.nightcore.bridge.currency.Currency;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.Players;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BankManager extends AbstractManager<ShopPlugin> {

    private final ChestShopModule module;
    private final BankDataManager dataManager;

    private final Map<UUID, Bank> bankByIdMap;

    private boolean loaded;

    public BankManager(@NonNull ShopPlugin plugin, @NonNull ChestShopModule module, @NonNull DataHandler dataHandler) {
        super(plugin);
        this.module = module;
        this.dataManager = new BankDataManager(this, dataHandler);
        this.bankByIdMap = new ConcurrentHashMap<>();
    }

    @Override
    protected void onLoad() {
        this.dataManager.init();

        this.addListener(new BankListener(this.plugin, this));

        this.addAsyncTask(this::saveBanks, 5); // TODO Config

        this.plugin.runTaskAsync(this::loadBanks);
    }

    @Override
    protected void onShutdown() {
        this.saveBanks();

        this.bankByIdMap.clear();
        this.loaded = false;
    }

    public boolean isLoaded() {
        return this.loaded;
    }

    public void handlePlayerJoin(@NonNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (!this.hasBank(playerId)) {
            this.createBankAndLoad(playerId);
        }
    }

    public void loadBanks() {
        this.dataManager.getBanks().forEach(this::loadBank);

        // Ensure all online players have banks created.
        Players.getOnline().stream().map(Entity::getUniqueId).filter(Predicate.not(this::hasBank)).forEach(this::createBankAndLoad);

        this.loaded = true;
        this.module.info("Loaded %s banks.".formatted(this.bankByIdMap.size()));
    }

    @NonNull
    private Bank createBankAndLoad(@NonNull UUID playerId) {
        Bank bank = Bank.create(playerId);
        bank.markDirty();
        this.loadBank(bank);
        return bank;
    }

    public void loadBank(@NonNull Bank bank) {
        this.bankByIdMap.put(bank.getHolder(), bank);
    }

    public void saveBanks() {
        if (!this.loaded) return;

        Set<Bank> banks = this.getBanks().stream().filter(Bank::isDirty).peek(Bank::markClean).collect(Collectors.toSet());
        // TODO Delete bans marked for removal

        this.dataManager.upsertBanks(banks);
    }

    public boolean hasBank(@NonNull UUID ownerId) {
        return this.bankByIdMap.containsKey(ownerId);
    }

    public void depositToBank(@NonNull Player player, @NonNull Currency currency, double amount) {
        this.depositToBank(player, player.getUniqueId(), currency, amount);
    }

    public void depositToBank(@NonNull Player player, @NonNull UUID target, @NonNull Currency currency, double amount) {
        Bank bank = this.getBankById(target);
        if (bank == null) {
            this.module.sendPrefixed(ChestLang.BANK_ERROR_NOT_EXIST, player);
            return;
        }

        this.depositToBank(player, bank, currency, amount);
    }

    public void depositToBank(@NonNull Player player, @NonNull Bank bank, @NonNull Currency currency, double amount) {
        if (!this.module.isAvailableCurrency(player, currency)) {
            this.module.sendPrefixed(ChestLang.BANK_ERROR_INVALID_CURRENCY, player);
            return;
        }
        if (amount == 0) return;

        double balance = currency.queryBalance(player);
        double depositAmount = amount < 0 ? balance : amount;

        if (balance < depositAmount) {
            this.module.sendPrefixed(ChestLang.BANK_DEPOSIT_ERROR_NOT_ENOUGH, player);
            return;
        }

        currency.withdrawAsync(player, amount).whenCompleteAsync((result, throwable) -> {
            if (throwable != null) {
                throwable.printStackTrace();
                return;
            }
            if (!result) return;

            bank.getAccount().store(currency, depositAmount);
            bank.markDirty();

            this.module.sendPrefixed(ChestLang.BANK_DEPOSIT_SUCCESS, player, builder -> builder
                .with(ShopPlaceholders.GENERIC_AMOUNT, () -> currency.format(depositAmount))
            );
        }, this.plugin::runTask);
    }

    public boolean withdrawFromBank(@NonNull Player player, @NonNull Currency currency, double amount) {
        return this.withdrawFromBank(player, player.getUniqueId(), currency, amount);
    }

    public boolean withdrawFromBank(@NonNull Player player, @NonNull UUID target, @NonNull Currency currency, double amount) {
        Bank bank = this.getBankById(target);
        if (bank == null) {
            this.module.sendPrefixed(ChestLang.BANK_ERROR_NOT_EXIST, player);
            return false;
        }

        return this.withdrawFromBank(player, bank, currency, amount);
    }

    public boolean withdrawFromBank(@NonNull Player player, @NonNull Bank bank, @NonNull Currency currency, double amount) {
        if (amount == 0) return false;

        double withdrawAmount = amount < 0D ? bank.getAccount().query(currency) : amount;

        if (!bank.getAccount().has(currency, withdrawAmount)) {
            this.module.sendPrefixed(ChestLang.BANK_WITHDRAW_ERROR_NOT_ENOUGH, player);
            return false;
        }

        currency.deposit(player, withdrawAmount);
        bank.getAccount().remove(currency, withdrawAmount);
        bank.markDirty();

        this.module.sendPrefixed(ChestLang.BANK_WITHDRAW_SUCCESS, player, replacer -> replacer
            .with(ShopPlaceholders.GENERIC_AMOUNT, () -> currency.format(withdrawAmount))
        );
        return true;
    }

    @NonNull
    public Map<UUID, Bank> getBankByIdMap() {
        return Map.copyOf(this.bankByIdMap);
    }

    @NonNull
    public Set<Bank> getBanks() {
        return Set.copyOf(this.bankByIdMap.values());
    }

    @Nullable
    public Bank getBankById(@NonNull UUID ownerId) {
        return this.bankByIdMap.get(ownerId);
    }
}
