package com.log430.tp0.controller;

import com.sun.net.httpserver.HttpExchange;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit test for the HelloController class.
 */
public class HelloControllerTest {

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
}
