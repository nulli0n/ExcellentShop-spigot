package su.nightexpress.excellentshop.feature.playershop.bank.data;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import su.nightexpress.excellentshop.feature.playershop.bank.BankManager;
import su.nightexpress.excellentshop.data.DataHandler;
import su.nightexpress.excellentshop.feature.playershop.bank.Bank;
import su.nightexpress.nightcore.db.statement.condition.Wheres;
import su.nightexpress.nightcore.db.statement.template.SelectStatement;
import su.nightexpress.nightcore.db.table.Table;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class BankDataManager {

    //private final ChestShopModule module;
    private final BankManager bankManager;
    private final DataHandler dataHandler;

    private final Table bankTable;

    public BankDataManager(/*@NonNull ChestShopModule module, */@NonNull BankManager bankManager, @NonNull DataHandler dataHandler) {
        //this.module = module;
        this.bankManager = bankManager;
        this.dataHandler = dataHandler;

        this.bankTable = Table.builder(dataHandler.getTablePrefix() + "_player_banks")
            .withColumn(BankDataColumns.BANK_HOLDER)
            .withColumn(BankDataColumns.BANK_BALANCE)
            .build();
    }

    public void init() {
        this.dataHandler.createTable(this.bankTable);

        this.dataHandler.addTableSync(this.bankTable, resultSet -> {
            try {
                Bank bank = BankDataQueries.BANK_MAPPER.map(resultSet);
                if (bank != null) {
                    this.bankManager.loadBank(bank);
                }
            }
            catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    @NotNull
    public List<Bank> getBanks() {
        return this.dataHandler.selectAny(this.bankTable, SelectStatement.builder(BankDataQueries.BANK_MAPPER).build());
    }

    public void upsertBanks(@NotNull Collection<Bank> banks) {
        this.dataHandler.insert(this.bankTable, BankDataQueries.BANK_INSERT, banks);
    }

    public void deleteBanks(@NonNull Collection<Bank> banks) {
        this.dataHandler.delete(this.bankTable, banks, Wheres.whereUUID(BankDataColumns.BANK_HOLDER, Bank::getHolder));
    }
}
