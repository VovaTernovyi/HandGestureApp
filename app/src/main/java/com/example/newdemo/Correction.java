package com.example.newdemo;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import static com.example.newdemo.Container.SAMPLE_NUM;

public class Correction {

    AppContainer appContainer;

    public Correction(AppContainer container) {
        appContainer = container;
    }

    void boundariesCorrection() {
        for (int i = 1; i < SAMPLE_NUM; i++) {
            for (int j = 0; j < 3; j++) {
                appContainer.container.cLower[i][j] = appContainer.container.cLower[0][j];
                appContainer.container.cUpper[i][j] = appContainer.container.cUpper[0][j];
                appContainer.container.cBackLower[i][j] = appContainer.container.cBackLower[0][j];
                appContainer.container.cBackUpper[i][j] = appContainer.container.cBackUpper[0][j];
            }
        }

        for (int i = 0; i < SAMPLE_NUM; i++) {
            for (int j = 0; j < 3; j++) {
                if (appContainer.container.mAvgColor[i][j] - appContainer.container.cLower[i][j] < 0) {
                    appContainer.container.cLower[i][j] = appContainer.container.mAvgColor[i][j];
                }
                if (appContainer.container.mAvgColor[i][j] + appContainer.container.cUpper[i][j] > 255) {
                    appContainer.container.cUpper[i][j] = 255 - appContainer.container.mAvgColor[i][j];
                }
                if (appContainer.container.mAvgBackColor[i][j] - appContainer.container.cBackLower[i][j] < 0) {
                    appContainer.container.cBackLower[i][j] = appContainer.container.mAvgBackColor[i][j];
                }
                if (appContainer.container.mAvgBackColor[i][j] + appContainer.container.cBackUpper[i][j] > 255) {
                    appContainer.container.cBackUpper[i][j] = 255 - appContainer.container.mAvgBackColor[i][j];
                }
            }
        }
    }

    Rect makeBoundingBox(Mat img) {
        appContainer.mHandGesture.contours.clear();
        Imgproc.findContours(img, appContainer.mHandGesture.contours, appContainer.mHandGesture.hie,
                Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        appContainer.mHandGesture.findBiggestContour();

        if (appContainer.mHandGesture.cMaxId > -1) {
            appContainer.mHandGesture.boundingRect =
                    Imgproc.boundingRect(appContainer.mHandGesture.contours.get(appContainer.mHandGesture.cMaxId));
        }
        if (appContainer.mHandGesture.detectIsHand(appContainer.container.rgbaMat)) {
            return appContainer.mHandGesture.boundingRect;
        } else {
            return null;
        }
    }
}
