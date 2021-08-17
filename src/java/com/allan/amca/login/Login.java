package com.allan.amca.login;

import com.allan.amca.data.DataResources;

import java.sql.*;

public class Login {

    private static final Login instance = new Login();

    private Login() {}
    public boolean login(final Long clientID, final String pin) {
        final String URI            = DataResources.getDBUri();
        final String DB_USER        = DataResources.getDBUsername();
        final String DB_PW          = DataResources.getDBPassword();
        final String LOGIN_QUERY    = "SELECT client_id, pin FROM client WHERE client_id = ? AND pin = ?;";
        final ResultSet result;
        boolean resultValid = false;

        try (Connection connection = DriverManager.getConnection(URI, DB_USER, DB_PW)) {
            try (PreparedStatement validateLogin = connection.prepareStatement(LOGIN_QUERY)) {
                validateLogin.setLong(1, clientID);
                validateLogin.setString(2, pin);
                result = validateLogin.executeQuery();
                if (result.next()) {
                    resultValid = true;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return resultValid;
    }

//    Singleton access
    public static Login getInstance() {
        return instance;
    }
}
