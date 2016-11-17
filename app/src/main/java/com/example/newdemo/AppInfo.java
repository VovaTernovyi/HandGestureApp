package com.example.newdemo;

import android.content.Intent;
import android.graphics.drawable.Drawable;

import java.io.IOException;
import java.io.Serializable;

public class AppInfo implements Serializable {
		private String mAppLabel;
	    private Drawable mAppIcon ;
	    private Intent mIntent ;
	    private String mPkgName ;
	      
	    public AppInfo(){}  
	      
	    public String getAppLabel() {
			return mAppLabel;
	    }

	    public void setAppLabel(String appName) {
	        this.mAppLabel = appName;
	    }  
	    public Drawable getAppIcon() {
			return mAppIcon;
	    }

	    public void setAppIcon(Drawable appIcon) {
			this.mAppIcon = appIcon;
	    }

	    public Intent getIntent() {
			return mIntent;
	    }

	    public void setIntent(Intent intent) {
			this.mIntent = intent;
	    }

	    public String getPkgName(){
			return mPkgName ;
	    }

	    public void setPkgName(String pkgName){
			this.mPkgName=pkgName ;
	    }  	
	    
	    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
	        out.defaultWriteObject();
	        out.writeObject(mAppLabel);
	        out.writeObject(mAppIcon);
	        out.writeObject(mIntent);
	        out.writeObject(mPkgName);
	    }

	    private void readObject(java.io.ObjectInputStream in) throws IOException,
				ClassNotFoundException {
			in.defaultReadObject();
	        mAppLabel=(String) in.readObject();
	        mAppIcon=(Drawable) in.readObject();
	        mIntent=(Intent) in.readObject();
	        mPkgName=(String) in.readObject();
	    }
}
