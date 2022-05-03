package com.server;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONArray;
import org.json.JSONObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class CoordinatesHandler implements HttpHandler {

    final private CoordinateDatabase db = CoordinateDatabase.getInstance();
    final private UserCoordinate uc = new UserCoordinate();

    // Handles POST and GET-requests
    @Override
    public void handle(HttpExchange t) throws IOException {
        final OutputStream outputStream = t.getResponseBody();
        final Headers headers = t.getRequestHeaders();
        String contentType = "";
        JSONObject responseCoordinates = null;

        if (t.getRequestMethod().equalsIgnoreCase("POST")) {
            if (headers.containsKey("Content-Type")) {

                contentType = headers.get("Content-Type").get(0);

            } else {

                String noContentType = "No contect type";
                t.sendResponseHeaders(403, noContentType.length());

                outputStream.write(noContentType.getBytes());
                outputStream.flush();
                outputStream.close();

            }

            if (contentType.equalsIgnoreCase("application/json")) {

                InputStream stream = t.getRequestBody();

                String text = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.joining("\n"));

                if (text == null || text.length() == 0) {

                    t.sendResponseHeaders(400, -1);
                    outputStream.flush();
                    outputStream.close();
                    stream.close();

                } else {

                    try {
                        responseCoordinates = new JSONObject(text);

                    } catch (Exception e) {

                        String errorJSONParseError = "JSON parse error, faulty user JSON";
                        t.sendResponseHeaders(403, errorJSONParseError.length());

                        outputStream.write(errorJSONParseError.getBytes());
                        outputStream.flush();
                        outputStream.close();

                    }

                    if (responseCoordinates.getString("username").length() == 0) {

                        String errorNPC = "No proper coordinates";
                        t.sendResponseHeaders(403, errorNPC.length());

                        outputStream.write(errorNPC.getBytes());
                        outputStream.flush();
                        outputStream.close();

                    } else {

                        try {

                            ZonedDateTime timeFromJson = ZonedDateTime.parse(responseCoordinates.getString("sent"));

                            LocalDateTime ldtTime = timeFromJson.toLocalDateTime();

                            double checkLongitude = responseCoordinates.isNull("longitude") ? null
                                    : responseCoordinates.getDouble("longitude");

                            double checkLatitude = responseCoordinates.isNull("latitude") ? null
                                    : responseCoordinates.getDouble("latitude");

                            try {

                                String description;

                                try {

                                    description = responseCoordinates.getString("description");

                                    if (description.length() <= 1024 && description.length() != 0) {

                                        description = responseCoordinates.getString("description");

                                    } else {

                                        description = "nodata";

                                    }

                                } catch (Exception e) {

                                    description = "nodata";

                                }

                                uc.addCoordinates(responseCoordinates.getString("username"), checkLongitude,
                                        checkLatitude, ldtTime, description);

                                String coordinatesAdded = "Coordinates added successfully";
                                t.sendResponseHeaders(200, coordinatesAdded.length());

                                outputStream.write(coordinatesAdded.getBytes());
                                outputStream.flush();
                                outputStream.close();

                            } catch (SQLException e) {

                                System.out.println("\nDB add failed\n");
                                e.printStackTrace();

                            }
                        } catch (Exception e) {
                            String errorWrongDatatype = "Datatype of coordinates is not double";
                            t.sendResponseHeaders(400, errorWrongDatatype.length());
                            outputStream.write(errorWrongDatatype.getBytes());
                            outputStream.flush();
                            outputStream.close();

                        }

                    }

                }
            } else {

                t.sendResponseHeaders(400, -1);
                System.out.println("Ei ole app/json");

            }

        } else if (t.getRequestMethod().equalsIgnoreCase("GET"))

        {

            if (headers.containsKey("Content-Type")) {

                contentType = headers.get("Content-Type").get(0);

            } else {

                String noContentType = "No contect type";
                t.sendResponseHeaders(403, noContentType.length());

                outputStream.write(noContentType.getBytes());
                outputStream.flush();
                outputStream.close();

            }

            if (contentType.equalsIgnoreCase("application/json")) {

                try {

                    JSONArray msgtest = db.sendCoordinatesToClient();

                    if (msgtest.isEmpty()) {

                        String errorMessage = "No coordinates";
                        t.sendResponseHeaders(200, errorMessage.length());

                        outputStream.write(errorMessage.getBytes());
                        outputStream.flush();
                        outputStream.close();

                    } else {

                        try {

                            System.out.println("\nSending coordinates to client");
                            JSONArray msg = db.sendCoordinatesToClient();

                            String sentData = msg.toString();

                            byte[] bytes = sentData.getBytes("UTF-8");
                            t.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
                            t.sendResponseHeaders(200, bytes.length);

                            outputStream.write(bytes);
                            outputStream.flush();
                            outputStream.close();

                        } catch (SQLException e) {

                            e.printStackTrace();
                            System.out.println("Sending failed to client");

                        }

                        outputStream.flush();
                        outputStream.close();
                    }
                } catch (Exception e) {

                    e.printStackTrace();

                }

            } else {
                String errorMessage = "Not supported";
                t.sendResponseHeaders(400, errorMessage.length());

                outputStream.write(errorMessage.getBytes());
                outputStream.flush();
                outputStream.close();

            }

        }
    }
}
