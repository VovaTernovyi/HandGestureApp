package com.example.newdemo;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;

import static com.example.newdemo.MainActivity.ADD_MODE;
import static com.example.newdemo.MainActivity.BACKGROUND_MODE;
import static com.example.newdemo.MainActivity.DETECTION_MODE;
import static com.example.newdemo.MainActivity.SAMPLE_MODE;
import static com.example.newdemo.MainActivity.TEST_MODE;
import static com.example.newdemo.MainActivity.TRAIN_REC_MODE;
import static com.example.newdemo.MainActivity.TAG;

public class Storage {

    private Container container;
    private Context context;

    public Storage(Container container, Context context) {
        this.container = container;
        this.context = context;
    }

    boolean savePicture() {
        Mat img;
        if ((container.mMode == BACKGROUND_MODE) || (container.mMode == SAMPLE_MODE) ||
                (container.mMode == TRAIN_REC_MODE) || (container.mMode == ADD_MODE) ||
                (container.mMode == TEST_MODE)) {
            Imgproc.cvtColor(container.rgbaMat, container.bgrMat, Imgproc.COLOR_RGBA2BGR, 3);
            img = container.bgrMat;
        } else if (container.mMode == DETECTION_MODE) {
            img = container.binMat;
        } else {
            img = null;
        }

        if (img != null) {
            if (!isExternalStorageWritable()) {
                Log.i(TAG,"External storage is not writable!");
                return false;
            }

            File path;
            String filename;

            if (container.mMode != ADD_MODE) {
                path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                filename = "image_" + container.mImgNum + ".jpg";
            } else {
                path = container.mStoreFolder;
                filename = container.mCurLabel + ".jpg";
            }

            container.mImgNum++;
            File file = new File(path, filename);

            filename = file.toString();
            Boolean bool = Highgui.imwrite(filename, img);

            if (bool) {
                Log.d(TAG, "Succeed writing image to" + filename);
            } else {
                Log.d(TAG, "Fail writing image to external storage");
            }
            return bool;
        }
        return false;
    }

    /**Checks if external storage is available for read and write */
    boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    void initLabel() {
        File file[] = container.mStoreFolder.listFiles();
        int maxLabel = 0;
        for (int i = 0; i < file.length; i++) {
            String fullName = file[i].getName();

            final int dotId = fullName.lastIndexOf('.');
            if (dotId > 0) {
                String name = fullName.substring(0, dotId);
                String extName = fullName.substring(dotId + 1);
                if (extName.equals("jpg")) {
                    int curName = Integer.valueOf(name);
                    if (curName > maxLabel) maxLabel = curName;
                }
            }
        }
        container.mCurLabel = maxLabel;
        container.mCurMaxLabel = container.mCurLabel;
    }
}