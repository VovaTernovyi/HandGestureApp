package com.example.newdemo;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

import java.util.List;

public class MyCameraView extends JavaCameraView {
	private static final String TAG = "SAMPLE_MY_CAMERA_VIEW";

	public MyCameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Deprecated
	public List<Size> getResolutionList() {
		return mCamera.getParameters().getSupportedPreviewSizes();
	}

	@Deprecated
	public void setResolution(Size resolution) {
		disconnectCamera();
		mMaxHeight = resolution.height;
		mMaxWidth = resolution.width;
		connectCamera(getWidth(), getHeight());
	}

	public Size getResolution() {
		return mCamera.getParameters().getPreviewSize();
	}

	public boolean isAutoWhiteBalanceLockSupported() {
		return mCamera.getParameters().isAutoWhiteBalanceLockSupported();
	}

	public boolean getAutoWhiteBalanceLock() {
		return mCamera.getParameters().getAutoWhiteBalanceLock();
	}

	@Deprecated
	public void setAutoWhiteBalanceLock(boolean toggle) {
		Camera.Parameters params = mCamera.getParameters();
		params.setAutoWhiteBalanceLock(toggle);
		mCamera.setParameters(params);
	}
}