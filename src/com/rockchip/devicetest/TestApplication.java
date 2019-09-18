/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月9日 上午10:28:39  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月9日      fxw         1.0         create
 *******************************************************************/   

package com.rockchip.devicetest;

import java.util.List;

import com.rockchip.devicetest.service.TestService;
import android.app.ActivityManagerNative;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Application;
import android.content.ComponentName;
import android.util.Log;

//proxy
public class TestApplication extends Application {

	public IndexActivity mIndexActivity;
	public TestService mTestService;
	ActivityManager mActivityManager; 
	private boolean mActivityReady;

	public void onCreate() {
//		System.setProperty("jcifs.resolveOrder", "DNS");
		System.setProperty("jcifs.smb.client.dfs.disabled", "true");
//		System.setProperty("jcifs.netbios.cachePolicy", "0");
//		System.setProperty("jcifs.smb.client.attrExpirationPeriod", "0");
		
		 
		System.setProperty("jcifs.smb.client.soTimeout", "10000");
		System.setProperty("jcifs.smb.client.responseTimeout", "10000");
		mActivityManager= (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		
		android.os.SystemProperties.set("tchip.init.nvme", "0");
		super.onCreate();
	}

	public boolean isDispatcherReady(){
		return isOnTopApplication();
	}

	public void setActivityReady(boolean ready){
		this.mActivityReady = ready;
	}

	public boolean isOnTopApplication(){
		boolean isOnTop = false;
		List<RunningTaskInfo> RunningTaskInfo = mActivityManager.getRunningTasks(1);
		if(RunningTaskInfo != null && RunningTaskInfo.size() >0)
		{
			ComponentName cn = mActivityManager.getRunningTasks(1).get(0).topActivity;
			isOnTop = mActivityReady&&mIndexActivity!=null&&cn.getPackageName().equals(getPackageName());
		}
		Log.v("TestService", "isOnTop="+isOnTop);
		return isOnTop;
		
	}

	public void setShowingApp(boolean isShowing){
		if(mTestService!=null){
			mTestService.setShowingApp(isShowing);
		}
	}
	
	public boolean isSystemReady()
	{
		Log.v("TestService", "ActivityManagerNative.isSystemReady()="+ActivityManagerNative.isSystemReady());
		return ActivityManagerNative.isSystemReady();
	}

}
