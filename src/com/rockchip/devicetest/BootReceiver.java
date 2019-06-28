/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Filename:    BootReceiver.java  
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2012-4-23 上午09:13:38  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2012-4-6      fxw         1.0         create
 *******************************************************************/
package com.rockchip.devicetest;

import com.rockchip.devicetest.service.TestService;
import com.rockchip.devicetest.utils.SharedPreferencesEdit;
import com.rockchip.devicetest.utils.SystemUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;


public class BootReceiver extends BroadcastReceiver {

	private Context mContext;

	private static final String TAG = "TestService";
	private static final boolean DEBUG = true;
	private static final String ACTION_START_AGINGTEST = "action.start.agingtest";

	private void LOGV(String msg) {
		if (DEBUG)
			Log.v(TAG, msg);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		SharedPreferencesEdit.getInstance().setContext(context);
		String action = intent.getAction();
		LOGV("action:" + action);
		if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
			Intent newIntent = new Intent(mContext, TestService.class);
			String path = intent.getDataString();
			boolean bootcomplete = SystemUtils.isBootComplete();
			LOGV("path" + path+",bootcomplete="+bootcomplete);

			if(bootcomplete)
				newIntent.putExtra(TestService.EXTRA_KEY_TESTFROM, "mount");	
			else
				newIntent.putExtra(TestService.EXTRA_KEY_TESTFROM, "app");
			LOGV("path1:" + path);

			mContext.startService(newIntent);
		} else if (ACTION_START_AGINGTEST.equals(action)) {
			SharedPreferencesEdit.getInstance().setLastShutDownIsUnsafety(false);
			Intent newIntent = new Intent(mContext, TestService.class);
			newIntent.putExtra(TestService.EXTRA_KEY_TESTFROM, "mount");
			mContext.startService(newIntent);
		}else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			LOGV("getSystemAbnormalRestartTime0:" + SharedPreferencesEdit.getInstance().getSystemAbnormalRestartTime());
			LOGV("setSystemStart0:" + SharedPreferencesEdit.getInstance().isSystemStart());
			LOGV("SystemUtils.isBootComplete()="+SystemUtils.isBootComplete());
			if(SharedPreferencesEdit.getInstance().isSystemStart()) {
				int times = SharedPreferencesEdit.getInstance().getSystemAbnormalRestartTime();
				times++;
				SharedPreferencesEdit.getInstance().setSystemAbnormalRestartTime(times);
				if(SharedPreferencesEdit.getInstance().isAgingStartBeforeLastShutDown())
					SharedPreferencesEdit.getInstance().setLastShutDownIsUnsafety(true);
				else
					SharedPreferencesEdit.getInstance().setLastShutDownIsUnsafety(false);
			}else{
				SharedPreferencesEdit.getInstance().setLastShutDownIsUnsafety(false);
			}
			SharedPreferencesEdit.getInstance().setBeforeLastShutDownAgingIsStart(false);
			Log.v(TAG, "setLastShutDownIsUnsafety ="+SharedPreferencesEdit.getInstance().isLastShutDownIsUnsafety());
			SharedPreferencesEdit.getInstance().setSystemStart(true);
			LOGV("setSystemStart1:" + SharedPreferencesEdit.getInstance().isSystemStart());

			Intent newIntent = new Intent(mContext, TestService.class);
			newIntent.putExtra(TestService.EXTRA_KEY_TESTFROM, "boot");
			mContext.startService(newIntent);
			LOGV("getSystemAbnormalRestartTime1:" + SharedPreferencesEdit.getInstance().getSystemAbnormalRestartTime());

		} else if (Intent.ACTION_SHUTDOWN.equals(action)) {		
			SharedPreferencesEdit.getInstance().setSystemStart(false);
		}
	}
	



}
