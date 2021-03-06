package main.com.allan.amca.transaction;

import main.com.allan.amca.enums.TransactionType;


import java.util.Calendar;
import java.util.HashMap;

/**
 * Factory class to create transactions. I used a HashMap for this one to give the implementation a try!
 * The HashMap will allow for faster lookup and performance... I guess?
 * @author allanaranzaso
 * @version 1.0
 */
public class TransactionFactory {

    protected static final HashMap<TransactionType, Transaction> transactionMap;
    private static final int TRANSACTION_MONTH  = Calendar.getInstance().get(Calendar.MONTH);
    private static final int TRANSACTION_DAY    = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    private static final int TRANSACTION_YEAR   = Calendar.getInstance().get(Calendar.YEAR);
    private static final int TRANSACTION_HOUR   = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    private static final int TRANSACTION_MIN    = Calendar.getInstance().get(Calendar.MINUTE);
    private static final int TRANSACTION_SEC    = Calendar.getInstance().get(Calendar.SECOND);
    private static final String currentDate;

    static {
        transactionMap = new HashMap<>();
        currentDate = formatTime();
        transactionMap.put(TransactionType.DEPOSIT, new Deposit(currentDate));
        transactionMap.put(TransactionType.WITHDRAWAL, new Withdrawal(currentDate));
    }

    /**
     * Static factory method to create the appropriate transaction.
     * @param type the type of transaction to create.
     * @return the transaction object. Null if the transaction type does not exist.
     */
    public static Transaction createTransaction(final TransactionType type) {
        final Transaction transaction;

        transaction = transactionMap.get(type);

        return transaction;
    }

    /**
     * @return the current date and time formatted for input into the database
     */
    private static String formatTime() {
        return TRANSACTION_YEAR + "-" + TRANSACTION_MONTH + "-" + TRANSACTION_DAY + " " +
                TRANSACTION_HOUR + ":" + TRANSACTION_MIN + ":" + TRANSACTION_SEC;
    }
}
