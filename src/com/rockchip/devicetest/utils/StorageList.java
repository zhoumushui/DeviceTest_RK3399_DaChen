package com.rockchip.devicetest.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

public class StorageList {
	private static final String TAG="StorageList";
	
	private StorageManager mStorageManager;
	private Method mMethodGetPaths;
	private Method mMethodGetPathsState;


	public StorageList(Context context) {
	
		if(context != null){
			mStorageManager = (StorageManager)context.getSystemService(Context.STORAGE_SERVICE);
			try{
				mMethodGetPaths = mStorageManager.getClass().
						getMethod("getVolumePaths");
				//通过调用类的实例mStorageManager的getClass()获取StorageManager类对应的Class对象
				//getMethod("getVolumePaths")返回StorageManager类对应的Class对象的getVolumePaths方法，这里不带参数
				//getDeclaredMethod()----可以不顾原方法的调用权限
				mMethodGetPathsState = mStorageManager.getClass().
						getMethod("getVolumeState",String.class);//String.class形参列表
			}catch(NoSuchMethodException ex){
				ex.printStackTrace();
			}
		}	
	}

	public String[] getVolumnPaths(){
		String[] paths=null;
		try{
			paths=(String[])mMethodGetPaths.invoke(mStorageManager);//调用该方法
			if(paths == null) {
				Log.d(TAG, "mMethodGetPaths Failed!!!");
				return null;
			}
	
			for(String path : paths) {
				Log.d(TAG,"Storage'paths:"+path+" state:"+getVolumeState(path));				
			}
	
		}catch(IllegalArgumentException ex){
			ex.printStackTrace();
		}catch(IllegalAccessException ex){
			ex.printStackTrace();	
		}catch(InvocationTargetException ex){
			ex.printStackTrace();
		}
		return paths;
	}
	
	public String getVolumeState(String mountPoint){
		//mountPoint是挂载点名Storage'paths[1]:/mnt/extSdCard不是/mnt/extSdCard/
		//不同手机外接存储卡名字不一样。/mnt/sdcard
		String status=null;
		try{
			status=(String)mMethodGetPathsState.invoke(mStorageManager, mountPoint);
			//调用该方法，mStorageManager是主调，mountPoint是实参数
		}catch(IllegalArgumentException ex){
			ex.printStackTrace();
		}catch(IllegalAccessException ex){
			ex.printStackTrace();	
		}catch(InvocationTargetException ex){
			ex.printStackTrace();
		}
		Log.d(TAG, "VolumnState:"+status);
		return status;
	}
	
	public boolean isVolumeMounted(String mountPoint) {
		if(Environment.MEDIA_MOUNTED.equals(getVolumeState(mountPoint))) {
			return true;
		}
		return false;
	}
}