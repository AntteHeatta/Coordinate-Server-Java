package com.server;

import java.sql.SQLException;
import com.sun.net.httpserver.BasicAuthenticator;
import org.json.JSONException;
import org.json.JSONObject;

public class UserAuthenticatorDB extends BasicAuthenticator {

    private CoordinateDatabase db = null;

    public UserAuthenticatorDB() {
        super("coordinates");
        db = CoordinateDatabase.getInstance();
    }

    // Checks if the user is valid
    @Override
    public boolean checkCredentials(String username, String password) {

        boolean isValidUser;

        try {
            isValidUser = db.authenticateUser(username, password);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return isValidUser;

    }

    // Utilizes the setUser-method to add user to a database
    public boolean addUser(String username, String password, String email) throws JSONException, SQLException {

        boolean result = db
                .setUser(new JSONObject().put("username", username).put("password", password).put("email", email));

        if (!result) {

            System.out.println("Cannot register user");
            return false;

        }

        return true;

    }

}
