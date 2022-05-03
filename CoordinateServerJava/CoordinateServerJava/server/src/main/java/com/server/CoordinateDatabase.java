package com.server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.json.JSONArray;
import org.json.JSONObject;
import java.sql.Statement;
import java.util.Base64;
import java.security.SecureRandom;
import org.apache.commons.codec.digest.Crypt;

public class CoordinateDatabase {

    private Connection dbConnection;
    private static CoordinateDatabase dbInstance;
    final private SecureRandom secureRandom = new SecureRandom();

    private String dbName = "CoordinateDB.db";

    public static synchronized CoordinateDatabase getInstance() {

        if (null == dbInstance) {

            dbInstance = new CoordinateDatabase();
        }

        return dbInstance;

    }

    public CoordinateDatabase() {
        try {
            open(dbName);
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    // Opens the database. If database exists, connects to it. Else, it creates it.
    public void open(String dbName) throws SQLException {

        File f = new File(dbName);

        if (f.exists()) {

            System.out.println("Connection to database successful");

            String database = "jdbc:sqlite:" + dbName;
            dbConnection = DriverManager.getConnection(database);

        } else {

            intializeDatabase(dbName);

        }

    }

    // Creates a database
    private boolean intializeDatabase(String dbName) throws SQLException {

        String database = "jdbc:sqlite:" + dbName;
        dbConnection = DriverManager.getConnection(database);

        if (null != dbConnection) {

            String createBasicDB = "create table users (username varchar(50) NOT NULL, password varchar(50) NOT NULL, email varchar(50), salt varchar(50), primary key(username))";
            String coordinateTable = "create table coordinates (username varchar(50) NOT NULL, longitude double NOT NULL, latitude double NOT NULL, sent long NOT NULL, description varchar(1024) NOT NULL)";

            Statement coordinateStatement;
            coordinateStatement = dbConnection.createStatement();
            coordinateStatement.executeUpdate(createBasicDB);
            coordinateStatement.executeUpdate(coordinateTable);
            coordinateStatement.close();

            System.out.println("Database created");
            return true;

        }

        return false;
    }

    // Closes the database
    public void closeDB() throws SQLException {
        if (null != dbConnection) {
            dbConnection.close();

            System.out.println("DB closed");

            dbConnection = null;
        }
    }

    // Adds coordinates to the database
    public void addCoordinates(JSONObject coordinates) throws SQLException {

        String setCoordinateString = "insert into coordinates " + "VALUES('" + coordinates.getString("username") + "','"
                + coordinates.getDouble("longitude") + "','"
                + coordinates.getDouble("latitude") + "','" + coordinates.getLong("sent") + "','"
                + coordinates.getString("description") + "')";

        Statement coordinateStatement;
        coordinateStatement = dbConnection.createStatement();
        coordinateStatement.executeUpdate(setCoordinateString);
        coordinateStatement.close();

    }

    // Reads the coordinates from the database and returns them as an JSONArray
    public JSONArray sendCoordinatesToClient() throws SQLException {

        Statement stmt = dbConnection.createStatement();
        String query = "select username, longitude, latitude, sent, description from coordinates";
        ResultSet rs = stmt.executeQuery(query);
        ResultSetMetaData rsmd = rs.getMetaData();

        JSONArray arr = new JSONArray();

        while (rs.next()) {

            int columns = rsmd.getColumnCount();

            JSONObject jsonObject = new JSONObject();

            for (int i = 1; i <= columns; i++) {

                String username = rs.getString("username");
                jsonObject.put("username", username);

                double longitude = rs.getDouble("longitude");
                jsonObject.put("longitude", longitude);

                double latitude = rs.getDouble("latitude");
                jsonObject.put("latitude", latitude);

                long sent = rs.getLong("sent");
                Instant instantTime = Instant.ofEpochMilli(sent);
                ZonedDateTime zoneDate = ZonedDateTime.ofInstant(instantTime, ZoneOffset.UTC);
                jsonObject.put("sent", zoneDate);

                String description = rs.getString("description");
                if (!"nodata".equals(description)) {
                    jsonObject.put("description", description);
                }
            }

            arr.put(jsonObject);
        }

        return arr;

    }

    // Adds the user into the database
    public boolean setUser(JSONObject user) throws SQLException {

        if (checkIfUserExists(user.getString("username"))) {
            return false;
        }

        String password = user.getString("password");

        byte bytes[] = new byte[13];

        secureRandom.nextBytes(bytes);

        String saltBytes = new String(Base64.getEncoder().encode(bytes));

        String salt = "$6$" + saltBytes;

        String hashedPassword = Crypt.crypt(password, salt);

        String setUserString = "insert into users " +
                "VALUES('" + user.getString("username") + "','" + hashedPassword + "','"
                + user.getString("email") + "','" + salt + "')";

        Statement createStatement;
        createStatement = dbConnection.createStatement();
        createStatement.executeUpdate(setUserString);
        createStatement.close();

        return true;
    }

    // Checks if user exists and return a boolean value
    public boolean checkIfUserExists(String givenUserName) throws SQLException {

        Statement queryStatement = null;
        ResultSet rs;

        String checkUser = "select username from users where username = '" + givenUserName + "'";

        queryStatement = dbConnection.createStatement();
        rs = queryStatement.executeQuery(checkUser);

        if (rs.next()) {

            return true;

        } else {

            return false;

        }
    }

    // Authenticates user by checking the database for matching username and
    // password
    public boolean authenticateUser(String givenUserName, String givenPassword) throws SQLException {

        Statement queryStatement = null;
        ResultSet rs;

        String getMessagesString = "select username, password from users where username = '" + givenUserName + "'";

        queryStatement = dbConnection.createStatement();
        rs = queryStatement.executeQuery(getMessagesString);

        if (rs.next() == false) {

            System.out.println("cannot find such user");
            return false;

        } else {

            String hashedPass = rs.getString("password");

            if (hashedPass.equals(Crypt.crypt(givenPassword, hashedPass))) {
                return true;

            } else {

                System.out.println("User authentication failed");
                return false;
            }

        }

    }
}
