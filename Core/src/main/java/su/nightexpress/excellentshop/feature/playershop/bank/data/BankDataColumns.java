package su.nightexpress.excellentshop.feature.playershop.bank.data;


import su.nightexpress.excellentshop.data.DataHandler;
import su.nightexpress.nightcore.db.column.Column;

import java.util.Map;
import java.util.UUID;

public class BankDataColumns {

    public static final Column<UUID>                BANK_HOLDER  = Column.uuidType("holder").primaryKey().build();
    public static final Column<Map<String, Double>> BANK_BALANCE = Column.jsonMap("balance", DataHandler.GSON, String.class, Double.class).build();

}
