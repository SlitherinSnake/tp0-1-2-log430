package com.log430.tp0;

import com.log430.tp0.controller.HelloController;
import com.log430.tp0.view.HelloView;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import org.junit.Test;

import java.io.*;
import java.net.InetSocketAddress;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Combined unit and integration tests for HelloController and WebServer.
 */
public class HelloWorldTest {

    @Test
    public void testGetMessageReturnsHelloWorld() {
        String result = HelloView.getMessage();
        assertEquals("Hello World!", result);
    }

    /**
     * The test will test that HelloHandler correctly returns the response expected which is "Hello World!".
     */
    @Test
    public void testHandleReturnsResponseExpected() throws IOException {
        HelloController controller = new HelloController();

        HttpExchange httpExchange = mock(HttpExchange.class);
        OutputStream mockOutputStream = spy(new ByteArrayOutputStream());

        when(httpExchange.getResponseBody()).thenReturn(mockOutputStream);

        controller.handle(httpExchange);

        String response = mockOutputStream.toString();
        assertEquals("Hello World!", response);
        verify(httpExchange).sendResponseHeaders(200, "Hello World!".length());
        verify(mockOutputStream).close();
    }

    /**
     * Test that the HTTP server starts manually on a specific port.
     */
    @Test
    public void testManualHttpServerStartsSuccessfully() {
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
