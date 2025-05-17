package com.log430.tp0;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class HelloWorld {

    public static void main(String[] args) throws IOException {
        //1. Application function in terminal aka locally
        System.out.println("Hello World!");

        //2. Application show message on web port 8080
        HttpServer httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        httpServer.createContext("/", new HelloHandler());
        httpServer.setExecutor(null);
        httpServer.start();

        System.out.println("Web server started at: http://localhost:8080");
    }

    static class HelloHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange httpExchange) {
            try {
                //Response is what is being shown on the website
                String response = "Hello World!";
                httpExchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } catch (IOException e) {
            }
        }
    }
}
