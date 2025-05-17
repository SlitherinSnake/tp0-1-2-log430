package com.log430.tp0;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Unit test for the HelloWorld Class.
 */
public class HelloWorldTest {

    /**
     * The first test will test that 
     * HelloHandler correctly returns the response expected which is "Hello World!".
     */
    @Test
    public void testHandleReturnsResponseExpected() {
        HelloWorld.HelloHandler helloHandler = new HelloWorld.HelloHandler();

        HttpExchange httpExchange = mock(HttpExchange.class);
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        when(httpExchange.getResponseBody()).thenReturn(os);

        helloHandler.handle(httpExchange); 

        String response = os.toString();
        assertEquals("Hello World!", response);
    }

    /**
     * The second test will test that 
     * the HTTP server starts successfully on a random port designed.
     */
    @Test
    public void testHttpServerStartsSuccesfully() {
        try {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress(4040),0);
            httpServer.createContext("/", new HelloWorld.HelloHandler());
            httpServer.setExecutor(null);
            httpServer.start();

            assertTrue("The port designed should be > 0", httpServer.getAddress().getPort() > 0);
            
            httpServer.stop(0);
        } catch (IOException e) {
            fail("Server couldn't start properly:" + e.getMessage());
        }
    }

    //KEKW  Goated test
    @Test
    public void testShouldAnswerWithTrue() {
        assertTrue(true);
    }
}
