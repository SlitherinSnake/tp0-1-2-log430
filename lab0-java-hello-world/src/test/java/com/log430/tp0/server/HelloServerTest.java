package com.log430.tp0.server;

import com.log430.tp0.controller.HelloController;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.Assert.*;

/**
 * Unit tests for the WebServer class.
 */
public class HelloServerTest {

    /**
     * Test that the HTTP server starts manually on a specific port.
     */
    @Test
    public void testHttpServerStartsSuccessfully() {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(4040), 0);
            httpServer.createContext("/", new HelloController());
            httpServer.setExecutor(null);
            httpServer.start();

            assertTrue("The port should be > 0", httpServer.getAddress().getPort() > 0);

            httpServer.stop(0);
        } catch (IOException e) {
            fail("Server couldn't start properly: " + e.getMessage());
        }
    }
}
