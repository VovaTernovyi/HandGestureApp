package com.example.newdemo;

import org.opencv.core.Mat;

public class CVMat {

    Container container;
    public CVMat(Container container) {
        this.container = container;
    }

    public void releaseCVMats() {
        releaseCVMat(container.sampleColorMat);
        container.sampleColorMat = null;

        if (container.sampleColorMats != null) {
            for (int i = 0; i < container.sampleColorMats.size(); i++) {
                releaseCVMat(container.sampleColorMats.get(i));
            }
        }
        container.sampleColorMats = null;

        if (container.sampleMats != null) {
            for (int i = 0; i < container.sampleMats.length; i++) {
                releaseCVMat(container.sampleMats[i]);
            }
        }
        container.sampleMats = null;

        releaseCVMat(container.rgbMat);
        container.rgbMat = null;
        releaseCVMat(container.bgrMat);
        container.bgrMat = null;
        releaseCVMat(container.interMat);
        container.interMat = null;
        releaseCVMat(container.binMat);
        container.binMat = null;
        releaseCVMat(container.binTmpMat0);
        container.binTmpMat0 = null;
        releaseCVMat(container.binTmpMat3);
        container.binTmpMat3 = null;
        releaseCVMat(container.binTmpMat2);
        container.binTmpMat2 = null;
        releaseCVMat(container.tmpMat);
        container.tmpMat = null;
        releaseCVMat(container.backMat);
        container.backMat = null;
        releaseCVMat(container.difMat);
        container.difMat = null;
        releaseCVMat(container.binDifMat);
        container.binDifMat = null;
    }

    private void releaseCVMat(Mat img) {
        if (img != null) img.release();
    }
}
