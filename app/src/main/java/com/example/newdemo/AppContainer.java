package com.example.newdemo;

import android.content.Context;

public class AppContainer {

    Container container;
    Gesture gesture;
    HandGesture mHandGesture;
    DataInit dataInit;
    Storage storage;
    PreTrain preTrain;
    Context appContext;
    CVMat cvMat;

    public AppContainer(Context context) {
        this.container = new Container();
        this.gesture = new Gesture(container, context);
        this.mHandGesture = null;
        this.dataInit = new DataInit(container);
        this.storage = new Storage(container, context);
        this.appContext = context;
        this.preTrain = new PreTrain(container, appContext);
        this.cvMat = new CVMat(container);
    }
}
