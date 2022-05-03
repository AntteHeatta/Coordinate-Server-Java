package com.server;

import java.security.KeyStore;
import java.util.concurrent.Executors;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import java.io.Console;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.*;

public class Server {

    Server() {
    }

    public static void main(String[] args) throws Exception {

        boolean power = true;
        Console inputConsole = System.console();
        String quitString;
        final CoordinateDatabase coordinateDatabase = CoordinateDatabase.getInstance();

        try {

            UserAuthenticatorDB auth = new UserAuthenticatorDB();
            // create the https server to port 8001 with default logger
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
            SSLContext sslContext = coordinateServerSSLContext();

            HttpContext context = server.createContext("/coordinates", new CoordinatesHandler());
            context.setAuthenticator(auth);

            HttpContext registration = server.createContext("/registration",
                    new RegistrationHandler(auth));

            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {

                public void configure(HttpsParameters params) {

                    InetSocketAddress remote = params.getClientAddress();

                    SSLContext c = getSSLContext();

                    SSLParameters sslparams = c.getDefaultSSLParameters();

                    params.setSSLParameters(sslparams);
                }
            });

            server.setExecutor(Executors.newCachedThreadPool());
            server.start();

            quitString = inputConsole.readLine();

            while (power = true) {

                if ("/quit".equals(quitString)) {

                    System.out.println("pysahtyyy");
                    power = false;
                    server.stop(3);
                    coordinateDatabase.closeDB();
                    break;

                } else {

                    power = true;
                    quitString = inputConsole.readLine();

                }
            }

        } catch (FileNotFoundException e) {

            System.out.println("Certificate not found!");
            e.printStackTrace();

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    private static SSLContext coordinateServerSSLContext() throws Exception {

        char[] passphrase = /*"Keystore.jks password here"*/.toCharArray();

        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(/*"Filepath to keystore.jks here"*/), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ssl;
    }

}
