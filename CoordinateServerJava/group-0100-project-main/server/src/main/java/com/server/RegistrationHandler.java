package com.server;

import java.util.stream.Collectors;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

public class RegistrationHandler implements HttpHandler {

    final private UserAuthenticatorDB userAuthenticator;

    RegistrationHandler(UserAuthenticatorDB auth) {
        this.userAuthenticator = auth;
    }

    // Handles POST-requests to registen a user
    @Override
    public void handle(HttpExchange t) throws IOException {

        JSONObject obj = null;
        final Headers headers = t.getRequestHeaders();
        String contentType = "";

        final OutputStream outputStream = t.getResponseBody();

        try {

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

                    String userDetails = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                            .lines()
                            .collect(Collectors.joining("\n"));

                    stream.close();

                    if (userDetails == null || userDetails.length() == 0) {

                        String errorNoDetails = "No user credentials";
                        t.sendResponseHeaders(403, errorNoDetails.length());

                        outputStream.write(errorNoDetails.getBytes());
                        outputStream.flush();
                        outputStream.close();
                    } else {

                        try {

                            obj = new JSONObject(userDetails);

                        } catch (JSONException e) {

                            String errorJSONParseError = "JSON parse error, faulty user JSON";
                            t.sendResponseHeaders(403, errorJSONParseError.length());

                            outputStream.write(errorJSONParseError.getBytes());
                            outputStream.flush();
                            outputStream.close();

                        }

                        if (obj.getString("username").length() == 0 || obj.getString("password").length() == 0) {

                            String errorNPUC = "No proper user credentials";
                            t.sendResponseHeaders(403, errorNPUC.length());

                            outputStream.write(errorNPUC.getBytes());
                            outputStream.flush();
                            outputStream.close();

                        } else {

                            Boolean result = userAuthenticator.addUser(obj.getString("username"),
                                    obj.getString("password"), obj.getString("email"));
                            if (result == false) {

                                String errorUserAlreadyRegistered = "User already registered";
                                t.sendResponseHeaders(403, errorUserAlreadyRegistered.length());

                                outputStream.write(errorUserAlreadyRegistered.getBytes());
                                outputStream.flush();
                                outputStream.close();

                            } else {

                                String msgUserRegistered = "User registered successfully";
                                t.sendResponseHeaders(200, msgUserRegistered.length());

                                outputStream.write(msgUserRegistered.getBytes());
                                outputStream.flush();
                                outputStream.close();
                            }

                        }
                    }

                } else {
                    String errorNoJSON = "Content type not application/json";
                    t.sendResponseHeaders(403, errorNoJSON.length());

                    outputStream.write(errorNoJSON.getBytes());
                    outputStream.flush();
                    outputStream.close();
                }

            } else {

                String errorMessage = "Not supported";
                t.sendResponseHeaders(400, errorMessage.length());

                outputStream.write(errorMessage.getBytes());
                outputStream.flush();
                outputStream.close();
            }

        } catch (

        Exception e) {
            System.out.println(e.getStackTrace());
            String errorCatch = "Error";
            t.sendResponseHeaders(400, errorCatch.length());

            outputStream.write(errorCatch.getBytes());
            outputStream.flush();
            outputStream.close();
        }
    }
}