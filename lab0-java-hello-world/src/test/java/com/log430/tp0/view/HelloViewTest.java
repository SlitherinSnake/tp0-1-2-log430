package com.log430.tp0.view;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class HelloViewTest {

    @Test
    public void testGetMessageReturnsHelloWorld() {
        String result = HelloView.getMessage();
        assertEquals("Hello World!", result);
    }
}
