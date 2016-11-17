package com.example.newdemo;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import static com.example.newdemo.Container.SAMPLE_NUM;

public class ProduceBinBackImage implements ProduceImage {

    private Container container;

    public ProduceBinBackImage(Container container) {
        this.container = container;
    }

    @Override
    public void produce(Mat imgIn, Mat imgOut) {
        for (int i = 0; i < SAMPLE_NUM; i++) {
            container.lowerBound.set(new double[] {
                    container.mAvgBackColor[i][0] - container.cBackLower[i][0],
                    container.mAvgBackColor[i][1] - container.cBackLower[i][1],
                    container.mAvgBackColor[i][2] - container.cBackLower[i][2]});
            container.upperBound.set(new double[] {
                    container.mAvgBackColor[i][0] + container.cBackUpper[i][0],
                    container.mAvgBackColor[i][1] + container.cBackUpper[i][1],
                    container.mAvgBackColor[i][2] + container.cBackUpper[i][2]});
            Core.inRange(imgIn, container.lowerBound, container.upperBound, container.sampleMats[i]);
        }
        imgOut.release();
        container.sampleMats[0].copyTo(imgOut);

        for (int i = 1; i < SAMPLE_NUM; i++) {
            Core.add(imgOut, container.sampleMats[i], imgOut);
        }
        Core.bitwise_not(imgOut, imgOut);
        Imgproc.medianBlur(imgOut, imgOut, 7);
    }
}