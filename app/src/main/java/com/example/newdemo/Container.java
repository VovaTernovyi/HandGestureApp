package com.example.newdemo;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.example.newdemo.MainActivity.BACKGROUND_MODE;
import static com.example.newdemo.MainActivity.DATA_COLLECTION_MODE;

public class Container {
    protected static final int SAMPLE_NUM = 7;

    Point[][] mSamplePoints;
    double[][] mAvgColor;
    double[][] mAvgBackColor;

    double[] channelsPixel;
    ArrayList<ArrayList<Double>> mAverChans;

    double[][] cLower;
    double[][] cUpper;
    double[][] cBackLower;
    double[][] cBackUpper;

    Scalar lowerBound;
    Scalar upperBound;
    Scalar mColorsRGB[];

    Mat interMat;
    Mat[] sampleMats;
    Mat sampleColorMat;
    List<Mat> sampleColorMats;
    Mat binMat;
    Mat binTmpMat;
    Mat binTmpMat2;
    Mat binTmpMat0;
    Mat binTmpMat3;
    Mat tmpMat;
    Mat backMat;
    Mat difMat;
    Mat binDifMat;
    Mat rgbaMat;
    Mat rgbMat;
    Mat bgrMat;

    String mStoreFolderName;
    File mStoreFolder;
    //Stores string representation of features to be written to train_data.txt
    ArrayList<String> mFeaStrs;

    int squareLen;
    int mCurLabel;
    int mSelectedLabel;
    int mCurMaxLabel;
    int mSelectedMappedLabel;
    //Initial mode is BACKGROUND_MODE to preSample the colors of the hand
    int mMode;
    int mChooserMode;

    int mImgNum;
    int mGesFrameCount;
    int appTestFrameCount;
    boolean isPictureSaved;
    String diagResult = null;

    public Container() {
        mSamplePoints = null;
        mAvgColor = null;
        mAvgBackColor = null;

        channelsPixel = new double[4];
        mAverChans = new ArrayList<>();

        cLower = new double[SAMPLE_NUM][3];
        cUpper = new double[SAMPLE_NUM][3];
        cBackLower = new double[SAMPLE_NUM][3];
        cBackUpper = new double[SAMPLE_NUM][3];

        lowerBound = new Scalar(0, 0, 0);
        upperBound = new Scalar(0, 0, 0);

        interMat = null;
        mColorsRGB = null;
        sampleMats = null;
        sampleColorMat = null;
        sampleColorMats = null;
        binMat = null;
        binTmpMat = null;
        binTmpMat2 = null;
        binTmpMat0 = null;
        binTmpMat3 = null;
        tmpMat = null;
        backMat = null;
        difMat = null;
        binDifMat = null;
        rgbaMat = null;
        rgbMat = null;
        bgrMat = null;

        mStoreFolder = null;
        mStoreFolderName = null;

        mMode = BACKGROUND_MODE;
        mChooserMode = DATA_COLLECTION_MODE;
        mFeaStrs = new ArrayList<>();
        diagResult = null;
        appTestFrameCount = 0;
        isPictureSaved = false;

        mCurLabel = 0;
        mSelectedLabel = -2;
        mCurMaxLabel = 0;
        mSelectedMappedLabel = -2;
    }
}
