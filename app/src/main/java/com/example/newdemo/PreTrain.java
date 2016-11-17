package com.example.newdemo;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import static com.example.newdemo.MainActivity.TAG;

public class PreTrain {

    protected Container container;
    private Storage storage;
    private Context context;

    public PreTrain(Container container, Context context) {
        this.container = container;
        this.context = context;
        this.storage = new Storage(container, context);
    }

    /**All the trained gestures jpg files and SVM training model, train_data.txt
     * are stored in ExternalStorageDirectory/MyDataSet
     * If MyDataSet doesn't exist, then it will be created in this function
     * */
    public void preTrain() {
        if (!storage.isExternalStorageWritable()) {
            Toast.makeText(context, "External storage is not writable!", Toast.LENGTH_SHORT).show();
        } else if (container.mStoreFolder == null) {
            container.mStoreFolderName = Environment.getExternalStorageDirectory() + "/MyDataSet";
            container.mStoreFolder = new File(container.mStoreFolderName);
            boolean success = true;
            if (!container.mStoreFolder.exists()) {
                success = container.mStoreFolder.mkdir();
            }
            if (success) {
                Log.i(TAG, "Success");
            } else {
                Toast.makeText(context, "Failed to create directory " + container.mStoreFolderName, Toast.LENGTH_SHORT).show();
                container.mStoreFolder = null;
                container.mStoreFolderName = null;
            }
        }
        if (container.mStoreFolder != null) {
            storage.initLabel();
        }
    }
}