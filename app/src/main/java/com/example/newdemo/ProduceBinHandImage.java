package com.example.newdemo;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import static com.example.newdemo.Container.SAMPLE_NUM;

public class ProduceBinHandImage implements ProduceImage {

    private Container container;

    public ProduceBinHandImage(Container container) {
        this.container = container;
    }

    @Override
    public void produce(Mat imgIn, Mat imgOut) {
        for (int i = 0; i < SAMPLE_NUM; i++) {
            container.lowerBound.set(new double[]{
                    container.mAvgColor[i][0] - container.cLower[i][0],
                    container.mAvgColor[i][1] - container.cLower[i][1],
                    container.mAvgColor[i][2] - container.cLower[i][2]});
            container.upperBound.set(new double[]{
                    container.mAvgColor[i][0] + container.cUpper[i][0],
                    container.mAvgColor[i][1] + container.cUpper[i][1],
                    container.mAvgColor[i][2] + container.cUpper[i][2]});

            Core.inRange(imgIn, container.lowerBound, container.upperBound, container.sampleMats[i]);
        }
        imgOut.release();
        container.sampleMats[0].copyTo(imgOut);

        for (int i = 1; i < SAMPLE_NUM; i++) {
            Core.add(imgOut, container.sampleMats[i], imgOut);
        }
        Imgproc.medianBlur(imgOut, imgOut, 3);
    }
}