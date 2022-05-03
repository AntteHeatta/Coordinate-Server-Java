package com.server;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.json.JSONObject;

public class UserCoordinate {

    public LocalDateTime sent;
    public String username;
    public double longitude;
    public double latitude;
    public String description;

    private CoordinateDatabase db = CoordinateDatabase.getInstance();

    public UserCoordinate() {
    }

    public UserCoordinate(String username, double longitude, double latitude, LocalDateTime sent, String description) {
        this.username = username;
        this.longitude = longitude;
        this.latitude = latitude;
        this.sent = sent;
        this.description = description;

    }

    // Utilizes the addCoordinates-method from the CoordinateDatabase-class to add
    // coordinates to the database
    public void addCoordinates(String username, double longitude, double latitude, LocalDateTime sent,
            String description)
            throws SQLException {

        long epoch = sent.toInstant(ZoneOffset.UTC).toEpochMilli();

        db.addCoordinates(new JSONObject().put("username", username).put("longitude", longitude)
                .put("latitude", latitude).put("sent", epoch).put("description", description));

    }

    public void setSent(LocalDateTime sent) {
        this.sent = sent;
    }

    long dateAsInt() {
        return sent.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    void setSent(long epoch) {
        this.sent = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
    }

    public LocalDateTime getSent() {
        return this.sent;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
