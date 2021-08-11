package com.allan.amca.user;

import com.allan.amca.data.DaoAbstract;
import com.allan.amca.data.Resources;
import org.jetbrains.annotations.NotNull;

import java.sql.*;

public class UserDaoImpl extends DaoAbstract<Client, Long> {
//    Resources
    private static final String DB_URI          = Resources.getDBUri();
    private static final String DB_USER         = Resources.getDBUsername();
    private static final String DB_PW           = Resources.getDBPassword();

    /**
     * Table name
     */
    private static final String TABLE_CLIENT                = "client";

    /**
     * Table columns
     */
    private static final String COLUMN_CLIENT_ID            = "client_id";
    private static final String COLUMN_CLIENT_FIRSTNAME     = "first_name";
    private static final String COLUMN_CLIENT_LASTNAME      = "last_name";
    private static final String COLUMN_CLIENT_PIN           = "pin";

//    PrepareStatement parameters for Client object
    private static final int NO_RECORDS                     = 0;
    private static final int FIRST_NAME_PARAM               = 1;
    private static final int LAST_NAME_PARAM                = 2;
    private static final int PIN_PARAM                      = 3;
    private static final int CLIENT_ID_PARAM                = 4;

//    DML commands
    private static final String ADD_NEW_USER   = "INSERT INTO " + TABLE_CLIENT;
    private static final UserDaoImpl instance = new UserDaoImpl();
    public UserDaoImpl(){}

    public static UserDaoImpl newInstance() {
        return instance;
    }
    /**
     *  Creates database on start if it doesn't already exist
      */
    public void onCreate() {
        final String DATABASE_NAME               = "Clients";
        final String CREATE_CLIENT_DB            = "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME;
        final String CREATE_CLIENT_TABLE = "CREATE TABLE " + TABLE_CLIENT + "("
                + COLUMN_CLIENT_ID + " INT NOT NULL,"
                + COLUMN_CLIENT_FIRSTNAME + " VARCHAR(30) NOT NULL,"
                + COLUMN_CLIENT_LASTNAME + " VARCHAR(40) NOT NULL,"
                + COLUMN_CLIENT_PIN + " TEXT NOT NULL,"
                + "PRIMARY KEY (" + COLUMN_CLIENT_ID + ")" + ");";

        try (Connection connection = DriverManager.getConnection(DB_URI, DB_USER, DB_PW)) {
            try (PreparedStatement createDB = connection.prepareStatement(CREATE_CLIENT_DB);
                PreparedStatement createTable = connection.prepareStatement(CREATE_CLIENT_TABLE)) {
                connection.setAutoCommit(false);

                createDB.execute();
                createTable.execute();

                DatabaseMetaData metaData = connection.getMetaData();
                System.out.printf("The driver name is: %s \n", metaData.getDriverName());
                System.out.println("A new database has been successfully created");
            }
            connection.commit();
        } catch (SQLException ex) {
            System.err.println("Message: " + ex.getMessage());
            System.err.println("Cause: " + ex.getCause());
        }
    }

    /** Adds client to the database
     * @param user the client object to add to the database
     * @return true if the query was successful and client was added.
     *  Otherwise, false if query did not execute. Client was not added in this case.
     */
    @Override
    protected boolean addRecord(@NotNull final Client user)  {
        final String ADD_USER       = ADD_NEW_USER + " (first_name, last_name, pin) " +
                                        "VALUES (?, ?, ?);";
        boolean clientAdded         = false;
        final int recordsInserted;

        try (Connection connection = DriverManager.getConnection(DB_URI, DB_USER, DB_PW)) {
            try (PreparedStatement addClient = connection.prepareStatement(ADD_USER)) {
                connection.setAutoCommit(false);
                addClient.setString(FIRST_NAME_PARAM, user.getFirstName());
                addClient.setString(LAST_NAME_PARAM, user.getLastName());
                addClient.setInt(PIN_PARAM, user.getPIN());

                recordsInserted = addClient.executeUpdate();
                if (recordsInserted > NO_RECORDS) {
                    clientAdded = true;
                }
            }
            connection.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return clientAdded;
    }

    /**
    * Retrieve client from the database
    * @param toRetrieve the client ID of the client to retrieve. Must be 16 digits long
    * @return the client object from the database. Returns null if client does not exist
    */
    @Override
    protected Client readRecord(final Long toRetrieve) {
        long clientID = toRetrieve.longValue();
        return getClientFromDatabase(clientID);
    }

    /**
     * Checks the database to see if the client you are looking for exists.
     * @param idToCheck the client ID of the client you want to check
     * @return true if client exists in the database. Otherwise, false
     */
    public boolean checkIfClientExists(final Number idToCheck) {
        long clientID = idToCheck.longValue();
        boolean clientExists = false;
        Client client = getClientFromDatabase(clientID);

        if (client != null) {
            clientExists = true;
        }
        return clientExists;
    }

    /**
     * Private method to retrieve a client from the database. Uses the resultSet from the getClient method to retrieve
     * the client
     * @param clientID The client's ID you want to retrieve from the database. Must be 16 digits long.
     * @return A client from the database
     */
    private Client getClientFromDatabase(final long clientID) {
        final ResultSet resultSet;
        final String SELECT_CLIENT_QUERY = "SELECT * FROM client WHERE client_id = ?";

        try (Connection connection = DriverManager.getConnection(DB_URI, DB_USER, DB_PW)) {
            try (PreparedStatement getClient = connection.prepareStatement(SELECT_CLIENT_QUERY)) {
                getClient.setLong(1, clientID);
                resultSet = getClient.executeQuery();
                if (resultSet.next()) {
                    return getClient(resultSet);
                }
                resultSet.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Private method to utilize the ResultSet and return a client from the database
     * @param resultSet the object that the SQL query will return. Never null
     * @return Client that was retrieved from the database
     * @throws SQLException if the query is invalid or if the database has issues
     */
    @NotNull
    private Client getClient(ResultSet resultSet) throws SQLException {
        final int CLIENT_ID_COL     = 1;
        final int FIRST_NAME_COL    = 2;
        final int LAST_NAME_COL     = 3;
        final int PIN_COL           = 4;

        final long clientID;
        final String firstName;
        final String lastName;
        final int    pin;
        final Client client;

        clientID = resultSet.getLong(CLIENT_ID_COL);
        firstName = resultSet.getString(FIRST_NAME_COL);
        lastName = resultSet.getString(LAST_NAME_COL);
        pin = resultSet.getInt(PIN_COL);

        client = UserFactory.createUser(firstName, lastName, pin);
        client.setClientID(clientID);

        return client;
    }

    /**
     * Update the selected client from the database
     * @param user the client object you wish to update in the database. Can pass null if you are passing in the client ID
     * @param id the ID of the client you wish to update. Can pass null if you are passing in a client object
     * @return false if the execute statement is an update count. Otherwise, true if execute statement returned a ResultSet
     */
    @Override
    protected boolean executeUpdate(final Client user, final Long id)  {
        final int recordsUpdated;
        final String UPDATE_USER = "UPDATE " + TABLE_CLIENT + " " +
                                    "SET " + COLUMN_CLIENT_FIRSTNAME + " = ?, "
                                    + COLUMN_CLIENT_LASTNAME + " = ?, "
                                    + COLUMN_CLIENT_PIN + " = ? WHERE client_id = ?;";
        boolean clientIsUpdated = false;

        try (Connection connection = DriverManager.getConnection(DB_URI, DB_USER, DB_PW)) {
            try (PreparedStatement updateClient = connection.prepareStatement(UPDATE_USER)) {
                connection.setAutoCommit(false);

                updateClient.setString(FIRST_NAME_PARAM, user.getFirstName());
                updateClient.setString(LAST_NAME_PARAM, user.getLastName());
                updateClient.setInt(PIN_PARAM, user.getPIN());
                updateClient.setLong(CLIENT_ID_PARAM, user.getClientID());

                recordsUpdated = updateClient.executeUpdate();
                if (recordsUpdated > NO_RECORDS) {
                    clientIsUpdated = true;
                    System.out.println("Client successfully updated");
                }
            }
            connection.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
         return clientIsUpdated;
    }

    /**
     * Delete the selected client from the database
     * @param clientID the client ID you wish to delete from the database
     * @return false if the execute statement is an update count. Otherwise, true if execute statement returned a ResultSet
     */
    @Override
    protected boolean deleteRecord(@NotNull final Client clientID) {
        final String DELETE_USER = "DELETE FROM client WHERE client_id = ?;";
        final int recordsDeleted;
        boolean clientDeleted    = false;

        try (Connection connection = DriverManager.getConnection(DB_URI, DB_USER, DB_PW)) {
            try (PreparedStatement deleteClient = connection.prepareStatement(DELETE_USER)) {
                connection.setAutoCommit(false);

                deleteClient.setLong(1, clientID.getClientID());
                recordsDeleted = deleteClient.executeUpdate();

                if (recordsDeleted > NO_RECORDS) {
                    clientDeleted = true;
                    System.out.printf("Client with ID: %d successfully deleted \n", clientID);
                }
            }
            connection.commit();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return clientDeleted;
    }
}