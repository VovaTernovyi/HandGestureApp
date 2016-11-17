package com.example.newdemo;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.lang.Math;

import static com.example.newdemo.Container.SAMPLE_NUM;

public class MainActivity extends ActionBarActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
	//Just for debugging
	public static final String TAG = "HAND_GESTURE_APP";
	//Mode that preSamples hand colors
	public static final int SAMPLE_MODE = 0;
	//Mode that generates binary image
	public static final int DETECTION_MODE = 1;
	//Mode that displays color image together with contours, fingertips,
	//defect points and so on.
	public static final int TRAIN_REC_MODE = 2;
	//Mode that preSamples background colors
	public static final int BACKGROUND_MODE = 3;
	//Mode that is started when user clicks the 'Add Gesture' button.
	public static final int ADD_MODE = 4;
	//Mode that is started when user clicks the 'Test' button.
	public static final int TEST_MODE = 5;
	//Mode that is started when user clicks 'App Test' in the menu.
	public static final int APP_TEST_MODE = 6;
	//Mode that is started when user clicks 'Data Collection' in the menu.
	public static final int DATA_COLLECTION_MODE = 0;
	//Frame interval between two launching events
	public static final int APP_TEST_DELAY_NUM = 10;
	//Number of frames collected for each gesture in the training set
	private static final int GES_FRAME_MAX = 10;
	//Color Space used for hand segmentation
	private static final int COLOR_SPACE = Imgproc.COLOR_RGB2Lab;
	//Number of frames used for prediction
	private static final int FRAME_BUFFER_NUM = 1;

	// onActivityResult request
	private static final int REQUEST_CODE = 6384;
	private static final int REQUEST_SELECTED_APP = 1111;

	public static final String DATASET_NAME = "/train_data.txt";

	//Stores the mapping results from gesture labels to app intents
	private HashMap<Integer, Intent> mTable = new HashMap<>();

	private List<AppInfo> mlistAppInfo = null;
	private MyCameraView mOpenCvCameraView;
	private MenuItem[] mResolutionMenuItems;
	private SubMenu mResolutionMenu;

	private List<android.hardware.Camera.Size> mResolutionList;

	private Handler mHandler = new Handler();
	private int[][] indices = new int[FRAME_BUFFER_NUM][];
	private float[][] values = new float[FRAME_BUFFER_NUM][];

	public final Object sync = new Object();

	File mSdCardDir = Environment.getExternalStorageDirectory();
	File mSdFile = new File(mSdCardDir, "AppMap.txt");

	protected AppContainer appContainer;
	protected ImageFacade imageFacade;
	public Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar my_toolbar = (Toolbar) findViewById(R.id.my_toolbar);
		setSupportActionBar(my_toolbar);

		try {
			FileInputStream fis = new FileInputStream(mSdFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			while (true) {
				try {
					int key = ois.readInt();
					String value = (String) ois.readObject();

					Intent intent = Intent.parseUri(value, 0);
					mTable.put(key, intent);
				} catch (IOException e) {
					break;
				}
			}
			ois.close();
			Log.e("ReadFile", "read succeeded......");
		} catch (Exception ex) {
			Log.e("ReadFile", "read ended......");
		}

		appContainer = new AppContainer(context);
		imageFacade = new ImageFacade(appContainer);

		mOpenCvCameraView = (MyCameraView) findViewById(R.id.HandGestureApp);
		mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
		mOpenCvCameraView.setCvCameraViewListener(this);

		appContainer.container.mSamplePoints = new Point[SAMPLE_NUM][2];
		for (int i = 0; i < SAMPLE_NUM; i++) {
			for (int j = 0; j < 2; j++) {
				appContainer.container.mSamplePoints[i][j] = new Point();
			}
		}

		appContainer.container.mAvgColor = new double[SAMPLE_NUM][3];
		appContainer.container.mAvgBackColor = new double[SAMPLE_NUM][3];

		for (int i = 0; i < 3; i++) {
			appContainer.container.mAverChans.add(new ArrayList<Double>());
		}

		appContainer.dataInit.initCLowerUpper(50, 50, 10, 10, 10, 10);
		appContainer.dataInit.initCBackLowerUpper(50, 50, 3, 3, 3, 3);

		imageFacade.produceBinImage = new ProduceBinImage(appContainer);

		SharedPreferences numbers = getSharedPreferences("Numbers", 0);
		appContainer.container.mImgNum = numbers.getInt("imgNum", 0);

		Log.i(TAG, "Created!");
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS: {
					Log.i("Android Tutorial", "OpenCV loaded successfully");
					System.loadLibrary("HandGestureApp");

					try {
						System.loadLibrary("signal");
					} catch (UnsatisfiedLinkError ule) {
						Log.e(TAG, "Hey, could not load native library signal");
					}

					mOpenCvCameraView.enableView();

					mOpenCvCameraView.setOnTouchListener(new OnTouchListener() {
						//Called when user touch the view screen
						//Mode flow: BACKGROUND_MODE --> SAMPLE_MODE --> DETECTION_MODE <--> TRAIN_REC_MODE
						public boolean onTouch(View v, MotionEvent event) {
							// ... Respond to touch events
							int action = MotionEventCompat.getActionMasked(event);

							switch (action) {
								case (MotionEvent.ACTION_DOWN):
									Log.d(TAG, "Action was DOWN");
									String toastStr = null;
									if (appContainer.container.mMode == SAMPLE_MODE) {
										appContainer.container.mMode = DETECTION_MODE;
										toastStr = "Sampling Finished!";
									} else if (appContainer.container.mMode == DETECTION_MODE) {
										appContainer.container.mMode = TRAIN_REC_MODE;
										setButtonsVisible();
										toastStr = "Binary Display Finished!";
										appContainer.preTrain.preTrain();
									} else if (appContainer.container.mMode == TRAIN_REC_MODE) {
										appContainer.container.mMode = DETECTION_MODE;
										setButtonsVisible();//invisible?????
										toastStr = "train finished!";
									} else if (appContainer.container.mMode == BACKGROUND_MODE) {
										toastStr = "First background sampled!";
										appContainer.container.rgbaMat.copyTo(appContainer.container.backMat);
										appContainer.container.mMode = SAMPLE_MODE;
									}

									Toast.makeText(getApplicationContext(), toastStr, Toast.LENGTH_LONG).show();
									return false;
								case (MotionEvent.ACTION_MOVE):
									Log.d(TAG, "Action was MOVE");
									return true;
								case (MotionEvent.ACTION_UP):
									Log.d(TAG, "Action was UP");
									return true;
								case (MotionEvent.ACTION_CANCEL):
									Log.d(TAG, "Action was CANCEL");
									return true;
								case (MotionEvent.ACTION_OUTSIDE):
									Log.d(TAG, "Movement occurred outside bounds " +
											"of current screen element");
									return true;
								default:
									return true;
							}
						}
					});

				}
				break;
				default: {
					super.onManagerConnected(status);
				}
				break;
			}
		}
	};

	/**Things triggered by clicking any items in the menu start here*/
	@Deprecated
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_save:
				appContainer.container.isPictureSaved = true;
				appContainer.storage.savePicture();
				return true;
			case R.id.data_collection:
				callDataCollection();
				return true;
			case R.id.app_test:
				if (appContainer.container.mMode == APP_TEST_MODE) {
					appContainer.container.mMode = TRAIN_REC_MODE;
					Toast.makeText(getApplicationContext(), "App testing ends!", Toast.LENGTH_LONG).show();
				} else {
					appContainer.container.mMode = APP_TEST_MODE;
					Toast.makeText(getApplicationContext(), "App testing begins!", Toast.LENGTH_LONG).show();
					appContainer.container.appTestFrameCount = 0;
				}
				return true;
		}

		if (item.getGroupId() == 2) {
			int id = item.getItemId();
			Camera.Size resolution = mResolutionList.get(id);
			mOpenCvCameraView.setResolution(resolution);
			resolution = mOpenCvCameraView.getResolution();
			String caption = Integer.valueOf(resolution.width).toString() + "x" +
					Integer.valueOf(resolution.height).toString();
			Toast.makeText(this, caption, Toast.LENGTH_SHORT).show();

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**This function deals with the results returned by file chooser triggered
	 * by clicking "Data Collection" in the menu
	 * RequestCode indicates which activity the request correspond to
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CODE: // Responding to the activity started by "Data Collection"
				if (resultCode == RESULT_OK) {
					if (data != null) {
						// Get the URI of the selected file
						final Uri uri = data.getData();
						Log.i(TAG, "Uri = " + uri.toString());
						try {
							final String path = FileUtils.getPath(this, uri);

							int slashId = path.lastIndexOf('/');
							int slashDot = path.lastIndexOf('.');
							String selectedLabelStr = path.substring(slashId + 1, slashDot);

							if (appContainer.container.mChooserMode == DATA_COLLECTION_MODE) {
								appContainer.container.mSelectedLabel = Integer.valueOf(selectedLabelStr);
								if (appContainer.container.mSelectedLabel != -2) {
									showDialog(this,"Add or Delete", "Selected Label is " +
													appContainer.container.mSelectedLabel + "," +
											"\nAdd to this gesture or delete it?", "Add", "Delete", "Cancel");
								}
							}
						} catch (Exception e) {
							Log.e("FileSelectorTestActiv", "File select error", e);
						}
					}
				}
				break;

			case REQUEST_SELECTED_APP: //Responding to the activity started in callAppList()
				if (resultCode == RESULT_OK) {
					if (appContainer.container.mSelectedMappedLabel != -2) {
						int position = data.getIntExtra("Position", -1);
						if (position != -1) {
							mTable.put(appContainer.container.mSelectedMappedLabel,
									mlistAppInfo.get(position).getIntent());
							Log.i(TAG, "selected Label = " + appContainer.container.mSelectedMappedLabel +
									"Position = " + position);
						}
					}
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**Initialize menu and resolution list.*/
	@Deprecated
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		mResolutionMenu = menu.addSubMenu("Resolution");
		mResolutionList = mOpenCvCameraView.getResolutionList();
		mResolutionMenuItems = new MenuItem[mResolutionList.size()];

		ListIterator<Camera.Size> resolutionItr = mResolutionList.listIterator();
		int idx = 0;
		while (resolutionItr.hasNext()) {
			Camera.Size element = resolutionItr.next();
			mResolutionMenuItems[idx] = mResolutionMenu.add(2, idx, Menu.NONE,
					Integer.valueOf(element.width).toString() + "x" +
							Integer.valueOf(element.height).toString());
			idx++;
		}
		checkCameraParameters();
		return true;
	}

	@Override
	public void onPause() {
		Log.i(TAG, "Paused!");
		super.onPause();
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.disableView();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
		Log.i(TAG, "Resumed!");
	}

	@Deprecated
	@Override
	public void onDestroy() {
		Log.i(TAG, "Destroyed!");
		appContainer.cvMat.releaseCVMats();

		super.onDestroy();
		if (mOpenCvCameraView != null) {
			mOpenCvCameraView.disableView();
		}
		SharedPreferences numbers = getSharedPreferences("Numbers", 0);
		SharedPreferences.Editor editor = numbers.edit();
		editor.putInt("imgNum", appContainer.container.mImgNum);
		editor.apply();//commit

		try {
			FileOutputStream fos = new FileOutputStream(mSdFile);
			Log.e("Fos", "write succeeded......");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			Log.e("oos", "write succeeded......");
			for (Map.Entry<Integer, Intent> entry : mTable.entrySet()) {
				oos.writeInt(entry.getKey());
				String tmp = entry.getValue().toURI();
				Log.e("111111", tmp);
				oos.writeObject(tmp);
			}
			Log.e("Ob", "write succeeded......");
			oos.close();
			Log.e("Write", "write succeeded......");
		} catch (Exception ex) {
			Log.e("Write", "write failed......");
		}
	}

	public void checkCameraParameters() {
		if (mOpenCvCameraView.isAutoWhiteBalanceLockSupported()) {
			if (mOpenCvCameraView.getAutoWhiteBalanceLock()) {
				Log.d("AutoWhiteBalanceLock", "Locked");
			} else {
				Log.d("AutoWhiteBalanceLock", "Not Locked");
				mOpenCvCameraView.setAutoWhiteBalanceLock(true);

				if (mOpenCvCameraView.getAutoWhiteBalanceLock()) {
					Log.d("AutoWhiteBalanLockAfter", "Locked");
				}
			}
		} else {
			Log.d("AutoWhiteBalanceLock", "Not Supported");
		}
	}

	// svm native
	private native int doClassificationNative(float values[][], int indices[][],
											  int isProb, String modelFile, int labels[], double probs[]);

	/**Called when user clicks "Data Collection" in the menu
	 * Starts a file chooser which lets the user chooses whatever gesture data he want to add
	 * Or add a new one
	 * */
	public void callDataCollection() {
		appContainer.container.mSelectedLabel = -2;
		appContainer.container.mChooserMode = DATA_COLLECTION_MODE;
		showChooser();
	}

	public void showDialogBeforeAdd(String title, String message) {
		Log.i("Show Dialog", "Entered");
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						appContainer.gesture.doAddNewGesture();
						synchronized (sync) {
							sync.notify();
						}
						dialog.cancel();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						synchronized (sync) {
							sync.notify();
						}
						dialog.cancel();
					}
				});
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	public void showDialog(final Context v, String title, String message,
						   String posStr, String negStr, String neuStr) {
		appContainer.container.diagResult = null;

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(v);
		alertDialogBuilder.setTitle(title);
		alertDialogBuilder.setMessage(message).setCancelable(false)
				.setPositiveButton(posStr, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						appContainer.container.diagResult = "Positive";
						Toast.makeText(v, "Add more to Gesture "
								+ appContainer.container.mSelectedLabel, Toast.LENGTH_SHORT).show();
						appContainer.container.mCurLabel = appContainer.container.mSelectedLabel - 1;
						dialog.cancel();
					}
				})
				.setNegativeButton(negStr, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						appContainer.container.diagResult = "Negative";
						appContainer.gesture.doDeleteGesture(appContainer.container.mSelectedLabel);
						Toast.makeText(v, "Gesture "
								+ appContainer.container.mSelectedLabel + " is deleted", Toast.LENGTH_SHORT).show();
						appContainer.container.mCurLabel = appContainer.container.mSelectedLabel - 1;
						dialog.cancel();
					}
				});

		if (neuStr != null) {
			alertDialogBuilder.setNeutralButton(neuStr, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					appContainer.container.diagResult = "Neutral";
					Toast.makeText(v, "Canceled", Toast.LENGTH_SHORT).show();
					appContainer.container.mSelectedLabel = -2;
					dialog.cancel();
				}
			});
		}
		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
	}

	private void setButtonsVisible() {
		findViewById(R.id.AddBtn).setVisibility(View.VISIBLE);
		findViewById(R.id.TrainBtn).setVisibility(View.VISIBLE);
		findViewById(R.id.TestBtn).setVisibility(View.VISIBLE);
	}

	private void setButtonsInvisible() {
		findViewById(R.id.AddBtn).setVisibility(View.INVISIBLE);
		findViewById(R.id.TrainBtn).setVisibility(View.INVISIBLE);
		findViewById(R.id.TestBtn).setVisibility(View.INVISIBLE);
	}

	private void showChooser() {
		Intent target = FileUtils.createGetContentIntent();
		Intent intent = Intent.createChooser(target, getString(R.string.chooser_title));
		try {
			startActivityForResult(intent, REQUEST_CODE);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**SVM training which outputs a file named as "model" in MyDataSet.
	 * Called when user clicks "Train" button.
	 * */
	public void train(View view) {
		int kernelType = 2; // Radial basis function
		int cost = 4;
		int isProb = 0;
		float gamma = 0.001f;
		String trainingFileLoc = appContainer.container.mStoreFolderName + DATASET_NAME;
		String modelFileLoc = appContainer.container.mStoreFolderName + "/model";
		Log.i("Store Path", modelFileLoc);

		if (trainClassifierNative(trainingFileLoc, kernelType, cost, gamma, isProb,
				modelFileLoc) == -1) {
			Log.d(TAG, "training err");
			finish();
		}
		trainClassifierNative(trainingFileLoc, kernelType, cost, gamma, isProb, modelFileLoc);
		Toast.makeText(this, "Training is done", Toast.LENGTH_SHORT).show();
	}

	// svm native
	private native int trainClassifierNative(String trainingFile, int kernelType,
											 int cost, float gamma, int isProb, String modelFile);


	public void addNewGesture(View view) {
		appContainer.gesture.addNewGesture();
	}

	/**Called when user clicks "Test" button*/
	public void test(View view) {
		if (appContainer.container.mMode == TRAIN_REC_MODE) {
			appContainer.container.mMode = TEST_MODE;
		} else if (appContainer.container.mMode == TEST_MODE) {
			appContainer.container.mMode = TRAIN_REC_MODE;
		}
	}

	void callAppByLabel(int label) {
		Intent intent = mTable.get(label);
		if (intent != null) {
			appContainer.container.appTestFrameCount = 0;
			startActivity(intent);
		}
	}

	void makeContours() {
		appContainer.mHandGesture.contours.clear();
		Imgproc.findContours(appContainer.container.binMat, appContainer.mHandGesture.contours, appContainer.mHandGesture.hie, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

		//Find biggest contour and return the index of the contour, which is hg.cMaxId
		appContainer.mHandGesture.findBiggestContour();

		if (appContainer.mHandGesture.cMaxId > -1) {
			appContainer.mHandGesture.approxContour.fromList(appContainer.mHandGesture.contours.get(appContainer.mHandGesture.cMaxId).toList());
			Imgproc.approxPolyDP(appContainer.mHandGesture.approxContour, appContainer.mHandGesture.approxContour, 2, true);
			appContainer.mHandGesture.contours.get(appContainer.mHandGesture.cMaxId).fromList(appContainer.mHandGesture.approxContour.toList());

			//hg.contours.get(hg.cMaxId) represents the contour of the hand
			Imgproc.drawContours(appContainer.container.rgbaMat, appContainer.mHandGesture.contours, appContainer.mHandGesture.cMaxId, appContainer.container.mColorsRGB[0], 1);

			//Palm center is stored in hg.inCircle, radius of the inscribed circle is stored in hg.inCircleRadius
			appContainer.mHandGesture.findInscribedCircle(appContainer.container.rgbaMat);
			appContainer.mHandGesture.boundingRect = Imgproc.boundingRect(appContainer.mHandGesture.contours.get(appContainer.mHandGesture.cMaxId));

			Imgproc.convexHull(appContainer.mHandGesture.contours.get(appContainer.mHandGesture.cMaxId), appContainer.mHandGesture.hullI, false);

			appContainer.mHandGesture.hullP.clear();
			for (int i = 0; i < appContainer.mHandGesture.contours.size(); i++)
				appContainer.mHandGesture.hullP.add(new MatOfPoint());

			int[] cId = appContainer.mHandGesture.hullI.toArray();
			List<Point> lp = new ArrayList<>();
			Point[] contourPts = appContainer.mHandGesture.contours.get(appContainer.mHandGesture.cMaxId).toArray();

			for (int i = 0; i < cId.length; i++) {
				lp.add(contourPts[cId[i]]);
			}

			//hg.hullP.get(hg.cMaxId) returns the locations of the points in the convex hull of the hand
			appContainer.mHandGesture.hullP.get(appContainer.mHandGesture.cMaxId).fromList(lp);
			lp.clear();

			appContainer.mHandGesture.fingerTips.clear();
			appContainer.mHandGesture.defectPoints.clear();
			appContainer.mHandGesture.defectPointsOrdered.clear();

			appContainer.mHandGesture.fingerTipsOrdered.clear();
			appContainer.mHandGesture.defectIdAfter.clear();

			if ((contourPts.length >= 5)
					&& appContainer.mHandGesture.detectIsHand(appContainer.container.rgbaMat) && (cId.length >= 5)) {
				Imgproc.convexityDefects(appContainer.mHandGesture.contours.get(appContainer.mHandGesture.cMaxId),
						appContainer.mHandGesture.hullI, appContainer.mHandGesture.defects);
				List<Integer> dList = appContainer.mHandGesture.defects.toList();

				for (int i = 0; i < dList.size(); i++) {
					int id = i % 4;
					Point curPoint;

					if (id == 2) { //Defect point
						double depth = (double) dList.get(i + 1) / 256.0;
						curPoint = contourPts[dList.get(i)];

						Point curPoint0 = contourPts[dList.get(i - 2)];
						Point curPoint1 = contourPts[dList.get(i - 1)];
						Point vec0 = new Point(curPoint0.x - curPoint.x, curPoint0.y - curPoint.y);
						Point vec1 = new Point(curPoint1.x - curPoint.x, curPoint1.y - curPoint.y);
						double dot = vec0.x * vec1.x + vec0.y * vec1.y;
						double lenth0 = Math.sqrt(vec0.x * vec0.x + vec0.y * vec0.y);
						double lenth1 = Math.sqrt(vec1.x * vec1.x + vec1.y * vec1.y);
						double cosTheta = dot / (lenth0 * lenth1);

						if ((depth > appContainer.mHandGesture.inCircleRadius * 0.7) && (cosTheta >= -0.7)
								&& (!isClosedToBoundary(curPoint0, appContainer.container.rgbaMat))
								&& (!isClosedToBoundary(curPoint1, appContainer.container.rgbaMat))) {
							appContainer.mHandGesture.defectIdAfter.add((i));
							Point finVec0 = new Point(curPoint0.x - appContainer.mHandGesture.inCircle.x,
									curPoint0.y - appContainer.mHandGesture.inCircle.y);
							double finAngle0 = Math.atan2(finVec0.y, finVec0.x);
							Point finVec1 = new Point(curPoint1.x - appContainer.mHandGesture.inCircle.x,
									curPoint1.y - appContainer.mHandGesture.inCircle.y);
							double finAngle1 = Math.atan2(finVec1.y, finVec1.x);

							if (appContainer.mHandGesture.fingerTipsOrdered.size() == 0) {
								appContainer.mHandGesture.fingerTipsOrdered.put(finAngle0, curPoint0);
								appContainer.mHandGesture.fingerTipsOrdered.put(finAngle1, curPoint1);
							} else {
								appContainer.mHandGesture.fingerTipsOrdered.put(finAngle0, curPoint0);
								appContainer.mHandGesture.fingerTipsOrdered.put(finAngle1, curPoint1);
							}
						}
					}
				}
			}
		}

		if (appContainer.mHandGesture.detectIsHand(appContainer.container.rgbaMat)) {
			//mHandGesture.boundingRect represents four coordinates of the bounding box.
			Core.rectangle(appContainer.container.rgbaMat, appContainer.mHandGesture.boundingRect.tl(), appContainer.mHandGesture.boundingRect.br(), appContainer.container.mColorsRGB[1], 2);
			Imgproc.drawContours(appContainer.container.rgbaMat, appContainer.mHandGesture.hullP, appContainer.mHandGesture.cMaxId, appContainer.container.mColorsRGB[2]);
		}
	}

	boolean isClosedToBoundary(Point pt, Mat img) {
		int margin = 5;
		return ((pt.x > margin) && (pt.y > margin) && (pt.x < img.cols() - margin) &&
				(pt.y < img.rows() - margin));
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		Log.i(TAG, "On CameraView started!");
		appContainer.dataInit.initOnCameraViewStarted();
		if (appContainer.mHandGesture == null) appContainer.mHandGesture = new HandGesture();
	}

	@Override
	public void onCameraViewStopped() {
		Log.i(TAG, "On CameraView stopped!");
	}

	/**Called when each frame data gets received
	 * inputFrame contains the data for each frame
	 * Mode flow: BACKGROUND_MODE --> SAMPLE_MODE --> DETECTION_MODE <--> TRAIN_REC_MODE
	 * */
	@Override
	public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
		appContainer.container.rgbaMat = inputFrame.rgba();

		Core.flip(appContainer.container.rgbaMat, appContainer.container.rgbaMat, 1);

		Imgproc.GaussianBlur(appContainer.container.rgbaMat, appContainer.container.rgbaMat, new Size(5, 5), 5, 5);

		Imgproc.cvtColor(appContainer.container.rgbaMat, appContainer.container.rgbMat, Imgproc.COLOR_RGBA2RGB);

		//Convert original RGB colorSpace to the colorSpace indicated by COLOR_SPACE
		Imgproc.cvtColor(appContainer.container.rgbaMat, appContainer.container.interMat, COLOR_SPACE);

		if (appContainer.container.mMode == SAMPLE_MODE) {
			//Second mode which preSamples the colors of the hand
			appContainer.dataInit.preSampleHand(appContainer.container.rgbaMat);
		} else if (appContainer.container.mMode == DETECTION_MODE) {
			//Third mode which generates the binary image containing the
			//segmented hand represented by white color
			imageFacade.produceBinImage.produce(appContainer.container.interMat, appContainer.container.binMat);
			return appContainer.container.binMat;
		} else if ((appContainer.container.mMode == TRAIN_REC_MODE) || (appContainer.container.mMode == ADD_MODE)
				|| (appContainer.container.mMode == TEST_MODE) || (appContainer.container.mMode == APP_TEST_MODE)) {
			imageFacade.produceBinImage.produce(appContainer.container.interMat, appContainer.container.binMat);
			makeContours();

			String entry = appContainer.mHandGesture.featureExtraction(appContainer.container.rgbaMat, appContainer.container.mCurLabel);
			//Collecting the frame data of a certain gesture and storing it in the file train_data.txt.
			//This mode stops when the number of frames processed equals GES_FRAME_MAX
			if (appContainer.container.mMode == ADD_MODE) {
				appContainer.container.mGesFrameCount++;
				Core.putText(appContainer.container.rgbaMat, Integer.toString(appContainer.container.mGesFrameCount), new Point(10,
						10), Core.FONT_HERSHEY_SIMPLEX, 0.6, Scalar.all(0));

				appContainer.container.mFeaStrs.add(entry);
				if (appContainer.container.mGesFrameCount == GES_FRAME_MAX) {
					Runnable runnableShowBeforeAdd = new Runnable() {
						@Override
						public void run() {
							showDialogBeforeAdd("Add or not", "Add this new gesture labeled as "
									+ appContainer.container.mCurLabel + "?");
						}
					};

					mHandler.post(runnableShowBeforeAdd);
					try {
						synchronized (sync) {
							sync.wait();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					appContainer.container.mMode = TRAIN_REC_MODE;
				}
			} else if ((appContainer.container.mMode == TEST_MODE) || (appContainer.container.mMode == APP_TEST_MODE)) {
				Double[] doubleValue = appContainer.mHandGesture.features.toArray(new Double[appContainer.mHandGesture.features.size()]);
				values[0] = new float[doubleValue.length];
				indices[0] = new int[doubleValue.length];

				for (int i = 0; i < doubleValue.length; i++) {
					values[0][i] = (float) (doubleValue[i] * 1.0f);
					indices[0][i] = i + 1;
				}

				int isProb = 0;
				String modelFile = appContainer.container.mStoreFolderName + "/model";
				int[] returnedLabel = {0};
				double[] returnedProb = {0.0};

				//Predicted labels are stored in returnedLabel
				//Since currently prediction is made for each frame, only returnedLabel[0] is useful.
				int r = doClassificationNative(values, indices, isProb, modelFile, returnedLabel, returnedProb);

				if (r == 0) {
					if (appContainer.container.mMode == TEST_MODE)
						Core.putText(appContainer.container.rgbaMat, Integer.toString(returnedLabel[0]), new Point(15,
								15), Core.FONT_HERSHEY_SIMPLEX, 0.6, appContainer.container.mColorsRGB[0]);
					else if (appContainer.container.mMode == APP_TEST_MODE) {
						//Launching other apps
						Core.putText(appContainer.container.rgbaMat, Integer.toString(returnedLabel[0]), new Point(15,
								15), Core.FONT_HERSHEY_SIMPLEX, 0.6, appContainer.container.mColorsRGB[2]);

						if (returnedLabel[0] != 0) {
							if (appContainer.container.appTestFrameCount == APP_TEST_DELAY_NUM) {
								//Call other apps according to the predicted label
								//This is done every APP_TEST_DELAY_NUM frames
								callAppByLabel(returnedLabel[0]);
							} else {
								appContainer.container.appTestFrameCount++;
							}
						}
					}
				}
			}
		} else if (appContainer.container.mMode == BACKGROUND_MODE) {
			//First mode which preSamples background colors
			appContainer.dataInit.preSampleBack(appContainer.container.rgbaMat);
		}

		if (appContainer.container.isPictureSaved) {
			appContainer.storage.savePicture();
			appContainer.container.isPictureSaved = false;
		}
		return appContainer.container.rgbaMat;
	}
}