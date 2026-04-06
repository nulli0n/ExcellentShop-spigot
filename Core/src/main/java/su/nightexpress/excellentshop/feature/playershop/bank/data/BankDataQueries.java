package su.nightexpress.excellentshop.feature.playershop.bank.data;

import su.nightexpress.excellentshop.feature.playershop.bank.Bank;
import su.nightexpress.excellentshop.data.DataHandler;
import su.nightexpress.nexshop.util.BalanceHolder;
import su.nightexpress.nightcore.db.statement.RowMapper;
import su.nightexpress.nightcore.db.statement.template.InsertStatement;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BankDataQueries {

    public static final RowMapper<Bank> BANK_MAPPER = resultSet -> {
        UUID holder = BankDataColumns.BANK_HOLDER.readOrThrow(resultSet);
        Map<String, Double> balanceRaw = BankDataColumns.BANK_BALANCE.read(resultSet).orElse(new HashMap<>());

        BalanceHolder balanceHolder = new BalanceHolder();
        balanceRaw.forEach(balanceHolder::set);

        return new Bank(holder, balanceHolder);
    };

    public static final InsertStatement<Bank> BANK_INSERT = InsertStatement.builder(Bank.class)
        .updateOnConflict()
        .setUUID(BankDataColumns.BANK_HOLDER, Bank::getHolder)
        .setString(BankDataColumns.BANK_BALANCE, bank -> DataHandler.GSON.toJson(bank.getAccount().getBalanceMap()))
        .build();

    /*public static final UpdateStatement<Bank> BANK_UPDATE = UpdateStatement.builder(Bank.class)
        .setString(BankDataColumns.BANK_BALANCE, bank -> DataHandler.GSON.toJson(bank.getAccount().getBalanceMap()))
        .build();*/
}
