package su.nightexpress.nexshop.data.impl;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.economybridge.EconomyBridge;
import su.nightexpress.economybridge.currency.CurrencyId;
import su.nightexpress.nexshop.ShopPlugin;
import su.nightexpress.economybridge.api.Currency;
import su.nightexpress.nexshop.data.DataHandler;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestBank;
import su.nightexpress.nightcore.database.sql.SQLColumn;
import su.nightexpress.nightcore.database.sql.SQLCondition;
import su.nightexpress.nightcore.database.sql.column.ColumnType;
import su.nightexpress.nightcore.util.Lists;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public class ChestDataHandler {

    private static final SQLColumn COLUMN_HOLDER  = SQLColumn.of("holder", ColumnType.STRING);
    private static final SQLColumn COLUMN_BALANCE = SQLColumn.of("balance", ColumnType.STRING);

    private final ShopPlugin plugin;
    private final DataHandler dataHandler;
    private final String      tableChestBank;

    private final Function<ResultSet, ChestBank> funcChestBank;

    public ChestDataHandler(@NotNull ShopPlugin plugin, @NotNull DataHandler dataHandler) {
        this.plugin = plugin;
        this.dataHandler = dataHandler;
        this.tableChestBank = this.dataHandler.getTablePrefix() + "_chestshop_bank";

        this.funcChestBank = resultSet -> {
            try {
                UUID holder = UUID.fromString(resultSet.getString(COLUMN_HOLDER.getName()));

                Map<String, Double> balanceRaw = this.dataHandler.gson().fromJson(resultSet.getString(COLUMN_BALANCE.getName()), new TypeToken<Map<String, Double>>(){}.getType());
                Map<Currency, Double> balanceMap = new HashMap<>();
                balanceRaw.forEach((id, amount) -> {
                    Currency currency = EconomyBridge.getCurrency(CurrencyId.reroute(id));
                    if (currency == null) return;

                    balanceMap.put(currency, amount);
                });

                return new ChestBank(holder, balanceMap);
            }
            catch (SQLException exception) {
                exception.printStackTrace();
                return null;
            }
        };
    }

    public void load() {
        this.dataHandler.createTable(this.tableChestBank, Arrays.asList(COLUMN_HOLDER, COLUMN_BALANCE));
    }

    public void purge() {

    }

    public void synchronize() {
        ChestShopModule module = this.plugin.getChestShop();
        if (module != null) {
            module.getBankMap().clear();
            module.loadBanks();
            //module.loadShopData();
        }
    }

    @NotNull
    public List<ChestBank> getChestBanks() {
        return this.dataHandler.load(this.tableChestBank, this.funcChestBank, Collections.emptyList(), Collections.emptyList(), -1);
    }

    public void createChestBank(@NotNull ChestBank bank) {
        Map<String, Double> map = new HashMap<>();
        bank.getBalanceMap().forEach((cur, amount) -> map.put(cur.getInternalId(), amount));

        this.dataHandler.insert(this.tableChestBank, Arrays.asList(
            COLUMN_HOLDER.toValue(bank.getHolder().toString()),
            COLUMN_BALANCE.toValue(this.dataHandler.gson().toJson(map))
        ));
    }

    public void saveChestBank(@NotNull ChestBank bank) {
        Map<String, Double> map = new HashMap<>();
        bank.getBalanceMap().forEach((cur, amount) -> map.put(cur.getInternalId(), amount));

        this.dataHandler.update(this.tableChestBank, Lists.newList(
            COLUMN_BALANCE.toValue(this.dataHandler.gson().toJson(map))
            ),
            SQLCondition.equal(COLUMN_HOLDER.toValue(bank.getHolder().toString()))
        );
    }

    public void removeChestBank(@NotNull UUID holder) {
        this.dataHandler.delete(this.tableChestBank,
            SQLCondition.equal(COLUMN_HOLDER.toValue(holder.toString()))
        );
    }
}
