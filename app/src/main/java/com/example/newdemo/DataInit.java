package com.example.newdemo;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.ArrayList;

import static com.example.newdemo.Container.SAMPLE_NUM;

public class DataInit {

    private Container container;

    public DataInit(Container container) {
        this.container = container;
    }

    /**Just initialize boundaries of the first sample*/
    void initCLowerUpper(double cl1, double cu1, double cl2, double cu2, double cl3, double cu3) {
        container.cLower[0][0] = cl1;
        container.cUpper[0][0] = cu1;
        container.cLower[0][1] = cl2;
        container.cUpper[0][1] = cu2;
        container.cLower[0][2] = cl3;
        container.cUpper[0][2] = cu3;
    }

    void initCBackLowerUpper(double cl1, double cu1, double cl2, double cu2, double cl3, double cu3) {
        container.cBackLower[0][0] = cl1;
        container.cBackUpper[0][0] = cu1;
        container.cBackLower[0][1] = cl2;
        container.cBackUpper[0][1] = cu2;
        container.cBackLower[0][2] = cl3;
        container.cBackUpper[0][2] = cu3;
    }

    void initOnCameraViewStarted() {
        if (container.sampleColorMat == null) container.sampleColorMat = new Mat();
        if (container.sampleColorMats == null) container.sampleColorMats = new ArrayList<>();
        if (container.sampleMats == null) {
            container.sampleMats = new Mat[SAMPLE_NUM];
            for (int i = 0; i < SAMPLE_NUM; i++) {
                container.sampleMats[i] = new Mat();
            }
        }

        if (container.rgbMat == null) container.rgbMat = new Mat();
        if (container.bgrMat == null) container.bgrMat = new Mat();
        if (container.interMat == null) container.interMat = new Mat();
        if (container.binMat == null) container.binMat = new Mat();
        if (container.binTmpMat == null) container.binTmpMat = new Mat();
        if (container.binTmpMat2 == null) container.binTmpMat2 = new Mat();
        if (container.binTmpMat0 == null) container.binTmpMat0 = new Mat();
        if (container.binTmpMat3 == null) container.binTmpMat3 = new Mat();
        if (container.tmpMat == null) container.tmpMat = new Mat();
        if (container.backMat == null) container.backMat = new Mat();
        if (container.difMat == null) container.difMat = new Mat();
        if (container.binDifMat == null) container.binDifMat = new Mat();

        container.mColorsRGB = new Scalar[] {
                new Scalar(255, 0, 0, 255),
                new Scalar(0, 255, 0, 255),
                new Scalar(0, 0, 255, 255)
        };
    }

    /**PreSampling hand colors.
     * Output is avgColor, which is essentially a 7 by 3 matrix
     * storing the colors sampled by seven squares
     */
    void preSampleHand(Mat img) {
        int cols = img.cols();
        int rows = img.rows();
        container.squareLen = rows / 20;
        Scalar color = container.mColorsRGB[2];  //Blue Outline

        container.mSamplePoints[0][0].x = cols / 2;
        container.mSamplePoints[0][0].y = rows / 4;
        container.mSamplePoints[1][0].x = cols * 5 / 12;
        container.mSamplePoints[1][0].y = rows * 5 / 12;
        container.mSamplePoints[2][0].x = cols * 7 / 12;
        container.mSamplePoints[2][0].y = rows * 5 / 12;
        container.mSamplePoints[3][0].x = cols / 2;
        container.mSamplePoints[3][0].y = rows * 7 / 12;
        container.mSamplePoints[4][0].x = cols / 1.5;
        container.mSamplePoints[4][0].y = rows * 7 / 12;
        container.mSamplePoints[5][0].x = cols * 4 / 9;
        container.mSamplePoints[5][0].y = rows * 3 / 4;
        container.mSamplePoints[6][0].x = cols * 5 / 9;
        container.mSamplePoints[6][0].y = rows * 3 / 4;

        for (int i = 0; i < SAMPLE_NUM; i++) {
            container.mSamplePoints[i][1].x = container.mSamplePoints[i][0].x + container.squareLen;
            container.mSamplePoints[i][1].y = container.mSamplePoints[i][0].y + container.squareLen;
        }

        for (int i = 0; i < SAMPLE_NUM; i++) {
            Core.rectangle(img, container.mSamplePoints[i][0], container.mSamplePoints[i][1], color, 1);
        }

        for (int i = 0; i < SAMPLE_NUM; i++) {
            for (int j = 0; j < 3; j++) {
                container.mAvgColor[i][j] = (container.interMat.
                        get((int) (container.mSamplePoints[i][0].y + container.squareLen / 2),
                                (int) (container.mSamplePoints[i][0].x + container.squareLen / 2)))[j];
            }
        }
    }

    /**PreSampling background colors.
     * Output is avgBackColor, which is essentially a 7 by 3 matrix
     * storing the colors sampled by seven squares
     */
    void preSampleBack(Mat img) {
        int cols = img.cols();
        int rows = img.rows();
        container.squareLen = rows / 20;
        Scalar color = container.mColorsRGB[2];  //Blue Outline

        container.mSamplePoints[0][0].x = cols / 6;
        container.mSamplePoints[0][0].y = rows / 3;
        container.mSamplePoints[1][0].x = cols / 6;
        container.mSamplePoints[1][0].y = rows * 2 / 3;
        container.mSamplePoints[2][0].x = cols / 2;
        container.mSamplePoints[2][0].y = rows / 6;
        container.mSamplePoints[3][0].x = cols / 2;
        container.mSamplePoints[3][0].y = rows / 2;
        container.mSamplePoints[4][0].x = cols / 2;
        container.mSamplePoints[4][0].y = rows * 5 / 6;
        container.mSamplePoints[5][0].x = cols * 5 / 6;
        container.mSamplePoints[5][0].y = rows / 3;
        container.mSamplePoints[6][0].x = cols * 5 / 6;
        container.mSamplePoints[6][0].y = rows * 2 / 3;

        for (int i = 0; i < SAMPLE_NUM; i++) {
            container.mSamplePoints[i][1].x = container.mSamplePoints[i][0].x + container.squareLen;
            container.mSamplePoints[i][1].y = container.mSamplePoints[i][0].y + container.squareLen;
        }

        for (int i = 0; i < SAMPLE_NUM; i++) {
            Core.rectangle(img, container.mSamplePoints[i][0], container.mSamplePoints[i][1], color, 1);
        }

        for (int i = 0; i < SAMPLE_NUM; i++) {
            for (int j = 0; j < 3; j++) {
                container.mAvgBackColor[i][j] = (container.interMat.
                        get((int) (container.mSamplePoints[i][0].y + container.squareLen / 2),
                                (int) (container.mSamplePoints[i][0].x + container.squareLen / 2)))[j];
            }
        }
    }
}