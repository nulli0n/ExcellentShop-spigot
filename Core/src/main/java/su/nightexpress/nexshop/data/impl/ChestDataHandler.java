package su.nightexpress.nexshop.data.impl;

import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.data.sql.SQLColumn;
import su.nexmedia.engine.api.data.sql.SQLCondition;
import su.nexmedia.engine.api.data.sql.column.ColumnType;
import su.nightexpress.nexshop.ShopAPI;
import su.nightexpress.nexshop.api.currency.Currency;
import su.nightexpress.nexshop.data.DataHandler;
import su.nightexpress.nexshop.shop.chest.ChestShopModule;
import su.nightexpress.nexshop.shop.chest.impl.ChestPlayerBank;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public class ChestDataHandler {

    private static final SQLColumn COLUMN_HOLDER  = SQLColumn.of("holder", ColumnType.STRING);
    private static final SQLColumn COLUMN_BALANCE = SQLColumn.of("balance", ColumnType.STRING);

    private final DataHandler dataHandler;
    private final String      tableChestBank;

    private final Function<ResultSet, ChestPlayerBank> funcChestBank;

    public ChestDataHandler(@NotNull DataHandler dataHandler) {
        this.dataHandler = dataHandler;
        this.tableChestBank = this.dataHandler.getTablePrefix() + "_chestshop_bank";

        this.funcChestBank = resultSet -> {
            try {
                UUID holder = UUID.fromString(resultSet.getString(COLUMN_HOLDER.getName()));

                Map<String, Double> balanceRaw = this.dataHandler.gson().fromJson(resultSet.getString(COLUMN_BALANCE.getName()), new TypeToken<Map<String, Double>>(){}.getType());
                Map<Currency, Double> balanceMap = new HashMap<>();
                balanceRaw.forEach((id, amount) -> {
                    Currency currency = this.dataHandler.plugin().getCurrencyManager().getCurrency(id);
                    if (currency == null) return;

                    balanceMap.put(currency, amount);
                });

                return new ChestPlayerBank(holder, balanceMap);
            }
            catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        };
    }

    public void load() {
        this.dataHandler.createTable(this.tableChestBank, Arrays.asList(COLUMN_HOLDER, COLUMN_BALANCE));
    }

    public void purge() {
        // TODO
    }

    public void synchronize() {
        ChestShopModule module = ShopAPI.getChestShop();
        if (module != null) {
            module.getBankMap().clear();
            module.loadBanks();
        }
    }

    @NotNull
    public List<ChestPlayerBank> getChestBanks() {
        return this.dataHandler.load(this.tableChestBank, this.funcChestBank, Collections.emptyList(), Collections.emptyList(), -1);
    }

    public void createChestBank(@NotNull ChestPlayerBank bank) {
        Map<String, Double> map = new HashMap<>();
        bank.getBalanceMap().forEach((cur, amount) -> map.put(cur.getId(), amount));

        this.dataHandler.insert(this.tableChestBank, Arrays.asList(
            COLUMN_HOLDER.toValue(bank.getHolder().toString()),
            COLUMN_BALANCE.toValue(this.dataHandler.gson().toJson(map))
        ));
    }

    public void saveChestBank(@NotNull ChestPlayerBank bank) {
        Map<String, Double> map = new HashMap<>();
        bank.getBalanceMap().forEach((cur, amount) -> map.put(cur.getId(), amount));

        this.dataHandler.update(this.tableChestBank, Arrays.asList(
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
