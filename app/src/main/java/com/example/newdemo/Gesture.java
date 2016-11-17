package com.example.newdemo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import static com.example.newdemo.MainActivity.ADD_MODE;
import static com.example.newdemo.MainActivity.DATASET_NAME;
import static com.example.newdemo.MainActivity.TAG;

public class Gesture {

    private FileWriter mFileWriter = null;
    private Container container;
    private Context context;
    private Storage storage;

    public Gesture(Container container, Context context) {
        this.container = container;
        this.context = context;
        this.storage = new Storage(container, context);
    }

    /**Called when user clicks "Add Gesture" button
     * Prepare train_data.txt file and set the mode to be ADD_MODE
     * */
    void addNewGesture() {
        if (container.mStoreFolder != null) {
            File myFile = new File(container.mStoreFolderName + DATASET_NAME);
            if (!myFile.exists()) {
                try {
                    myFile.createNewFile();
                } catch (Exception e) {
                    Toast.makeText(context, "Failed to create dataSet at "
                            + myFile, Toast.LENGTH_SHORT).show();
                }
            }

            try {
                mFileWriter = new FileWriter(myFile, true);
                container.mFeaStrs.clear();
                if (container.mSelectedLabel == -2)
                    container.mCurLabel = container.mCurMaxLabel + 1;
                else {
                    container.mCurLabel++;
                    container.mSelectedLabel = -2;
                }
                container.mGesFrameCount = 0;
                container.mMode = ADD_MODE;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i(TAG, "File not found. Did you" +
                        " add a WRITE_EXTERNAL_STORAGE permission to the manifest?");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**Write the strings of features to the file train_data.txt
     * Save the screenshot of the gesture
     * */
    void doAddNewGesture() {
        try {
            for (int i = 0; i < container.mFeaStrs.size(); i++) {
                mFileWriter.write(container.mFeaStrs.get(i));
            }
            mFileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        storage.savePicture();

        if (container.mCurLabel > container.mCurMaxLabel) {
            container.mCurMaxLabel = container.mCurLabel;
        }
    }

    void doDeleteGesture(int label) {
        // TODO delete gesture
    }
}
