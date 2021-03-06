package main.com.allan.amca.transaction;

import main.com.allan.amca.data.Dao;
import main.com.allan.amca.data.DaoFactory;
import main.com.allan.amca.data.DataResources;
import main.com.allan.amca.enums.DaoType;
import main.com.allan.amca.user.Client;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Transaction super class
 * @author allanaranzaso
 */
public abstract class Transaction
        extends TransactionFactory
        implements Transactional {

    private final Dao<Client, Long> accountDao              = DaoFactory.createDao(DaoType.ACCOUNT);
    private static final DataResources res                  = DataResources.getInstance();
    private static final String DB_URL;
    private static final String DB_USER;
    private static final String DB_PASS;
    private static final int    NEG_VALUE                   = 0;
    private int                 transactionID;
    private String              transactionDate;
    private BigDecimal          transactionAmount;

    static {
        res.loadPropsFile();
        DB_URL = res.getDBUrl();
        DB_USER = res.getDBUsername();
        DB_PASS = res.getDBPassword();
    }

    /**
     * Transaction constructor that takes one parameter to set the transaction date.
     * @param transactionDate the day the transaction took place. Is automatically set when the
     *                        TransactionFactory class is instantiated. See TransactionFactory.
     */
    public Transaction(final String transactionDate) {
        this.transactionDate = transactionDate;
    }

    /**
     * @return the date of the transaction
     */
    public String getTransactionDate() {
        return transactionDate;
    }

    /**
     * @return the transaction type
     */
    public String getTransactionType() {
        final String classType = this.getClass().toString().toLowerCase();
        return formatTransactionType(classType);
    }

    private String formatTransactionType(final String toFormat) {
        final String formatType;

        if (toFormat.contains("withdrawal")) {
            formatType = "withdrawal";
        } else if (toFormat.contains("deposit")) {
            formatType = "deposit";
        } else {
            throw new IllegalArgumentException("Transaction type is invalid: " + toFormat);
        }
        return formatType;
    }

    /**
     * @return the transaction dollar amount
     */
    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    /**
     * @return the transaction ID
     */
    public int getTransactionID() {
        return transactionID;
    }

    /**
     * @param transactionAmount the amount to set
     */
    public void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    /**
     * @param date the date of the transaction that took place to set
     */
    public void setTransactionDate(final String date) {
        if (date.isBlank()) {
            throw new IllegalArgumentException("Transaction date is invalid: " + date);
        }
        this.transactionDate = date;
    }

    /**
     * @param type the type of transaction to set
     */
    public void setTransactionType(final String type) {
        if (type.isBlank()) {
            throw new IllegalArgumentException("Transaction type is invalid: " + type);
        }
    }

    /**
     * @param id the transaction ID to set
     */
    public void setTransactionID(final int id) {
        if (id < NEG_VALUE) {
            throw new IllegalArgumentException("Transaction ID is invalid: " + id);
        }
        this.transactionID = id;
    }

    /**
     * Performs the transaction the user has requested.
     * @param clientID the client's ID to access the correct account in the database
     * @param amount the amount to process for the transaction
     * @return true if the query executed successfully. Otherwise, false.
     */
    @Override
    public final boolean performTransaction(final long clientID,
                                            final BigDecimal amount){
        final int BALANCE_PARAM                 = 1;
        final int CLIENT_ID_PARAM               = 2;
        final int TRANSACTION_TYPE_PARAM        = 1;
        final int TRANSACTION_DATE_PARAM        = 2;
        final int TRANSACTION_AMT_PARAM         = 3;
        final int TRANSACTION_CLIENT_ID_PARAM   = 4;
        final int NO_RECORDS        = 0;
        final String UPDATE_QUERY   = "UPDATE accounts SET balance = ? WHERE client_id = ?;";
        final String INSERT_QUERY   = "INSERT INTO Transactions " +
                "(transaction_type, transaction_date, transaction_amount, client_id) " +
                "VALUES(?, ?, ?, ?)";
        final BigDecimal INVALID_AMT    = BigDecimal.valueOf(0.0);
        final String EX_MSG         = "You have entered an invalid amount";
        final BigDecimal currentBalance;
        final BigDecimal newBalance;
        final int recordsUpdated;
        boolean transactionSuccess = false;

        transactionAmount = amount;
        currentBalance = accountDao.retrieve(clientID);

        if (amount.compareTo(INVALID_AMT) < 0) {
            throw new IllegalArgumentException(EX_MSG);
        }

        // Subclasses to implement logic for calculate()
        newBalance = calculate(currentBalance, transactionAmount);

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            try (PreparedStatement transaction = connection.prepareStatement(UPDATE_QUERY)) {
                connection.setAutoCommit(false);
                transaction.setBigDecimal(BALANCE_PARAM, newBalance);
                transaction.setLong(CLIENT_ID_PARAM, clientID);

                recordsUpdated = transaction.executeUpdate();
                if (recordsUpdated > NO_RECORDS) {
                    transactionSuccess = true;
                    try (PreparedStatement write = connection.prepareStatement(INSERT_QUERY)) {
                        write.setString(TRANSACTION_TYPE_PARAM, this.getTransactionType());
                        write.setString(TRANSACTION_DATE_PARAM, this.getTransactionDate());
                        write.setBigDecimal(TRANSACTION_AMT_PARAM, this.getTransactionAmount());
                        write.setLong(TRANSACTION_CLIENT_ID_PARAM, clientID);
                        write.executeUpdate();
                    }
                }
            }
            connection.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return transactionSuccess;
    }

    /**
     * Method for subclasses to override. Performs the appropriate calculation depending on the subclass instantiated.
     * @param currentBalance the balance to calculate. Retrieved from the database
     * @param amount the amount to calculate with account balance.
     * @return the sum after the calculation has been performed.
     */
    protected abstract BigDecimal calculate(BigDecimal currentBalance, BigDecimal amount);

}
