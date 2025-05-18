package com.log430.tp0.view;

import com.log430.tp0.model.HelloModel;

public class HelloView {
    public static String getMessage() {
        HelloModel model = new HelloModel("Hello World!");
        return model.getMessage();
    }
}
