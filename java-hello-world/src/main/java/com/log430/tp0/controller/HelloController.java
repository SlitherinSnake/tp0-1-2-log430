package com.log430.tp0.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.log430.tp0.view.HelloView;

import java.io.IOException;

public class HelloController implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = HelloView.getMessage();
        exchange.sendResponseHeaders(200, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.getResponseBody().close();
    }
}
