package com.log430.tp0.server;

import com.sun.net.httpserver.HttpServer;
import com.log430.tp0.controller.HelloController;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HelloServer {
    public static void start() {
        try {
            System.out.println("Hello World!");

            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/", new HelloController());
            server.setExecutor(null);
            server.start();

            System.out.println("Web server started at http://localhost:8080");
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}
