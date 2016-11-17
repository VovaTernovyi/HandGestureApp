package com.example.newdemo;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class ProduceBinImage implements ProduceImage {

    private AppContainer appContainer;
    private Correction correction;
    private ProduceBinHandImage produceBinHandImage;
    private ProduceBinBackImage produceBinBackImage;

    public ProduceBinImage(AppContainer appContainer) {
        this.appContainer = appContainer;
        produceBinHandImage = new ProduceBinHandImage(appContainer.container);
        produceBinBackImage = new ProduceBinBackImage(appContainer.container);
        this.correction = new Correction(appContainer);
    }

    @Override
    public void produce(Mat imgIn, Mat imgOut) {
        int colNum = imgIn.cols();
        int rowNum = imgIn.rows();
        int boxExtension = 0;

        correction.boundariesCorrection();
        produceBinHandImage.produce(imgIn, appContainer.container.binTmpMat);
        produceBinBackImage.produce(imgIn, appContainer.container.binTmpMat2);

        Core.bitwise_and(appContainer.container.binTmpMat, appContainer.container.binTmpMat2,
                appContainer.container.binTmpMat);
        appContainer.container.binTmpMat.copyTo(appContainer.container.tmpMat);
        appContainer.container.binTmpMat.copyTo(imgOut);

        Rect roiRect = correction.makeBoundingBox(appContainer.container.tmpMat);

        if (roiRect != null) {
            roiRect.x = Math.max(0, roiRect.x - boxExtension);
            roiRect.y = Math.max(0, roiRect.y - boxExtension);
            roiRect.width = Math.min(roiRect.width + boxExtension, colNum);
            roiRect.height = Math.min(roiRect.height + boxExtension, rowNum);

            Mat roi1 = new Mat(appContainer.container.binTmpMat, roiRect);
            Mat roi3 = new Mat(imgOut, roiRect);
            imgOut.setTo(Scalar.all(0));

            roi1.copyTo(roi3);

            Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
            Imgproc.dilate(roi3, roi3, element, new Point(-1, -1), 2);

            Imgproc.erode(roi3, roi3, element, new Point(-1, -1), 2);
        }
    }
}
