/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月21日 下午3:45:52  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月21日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.service;

import com.rockchip.devicetest.TestApplication;
import com.rockchip.devicetest.aging.backgroud.AgingTestService;
import com.rockchip.devicetest.testcase.LEDSettings;
import com.rockchip.devicetest.testcase.LEDSettings.LEDMode;
import com.rockchip.devicetest.utils.AppUtils;
import com.rockchip.devicetest.utils.LogUtil;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 *	看门狗监听服务,独立进程
 */
public class WatchdogService extends Service {
	
	public static final String COMMAND = "command";
	public static final String USE_3DTEST = "use_3dtest";
	public static final int CMD_START_AGING = 1;
	public static final int CMD_STOP_AGING = 2;
	public static final int CMD_RESTART_WATCHDOG = 3;
	private Handler mMainHandler = null;
	private boolean isRunningAgingTest;
	private LEDMode mLEDMode;
	private ActivityManager mActivityManager;
	private TestApplication mApp;
	@Override
	public void onCreate() {
		super.onCreate();
		mApp = (TestApplication)getApplication();
		mMainHandler = new Handler();
		mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
	}
	boolean m3DTestRun = false;
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		int command = 0;
		m3DTestRun = false;
		if(intent != null && intent.getExtras() != null)
		{
		   command = intent.getIntExtra(COMMAND, 0);
		   m3DTestRun = intent.getBooleanExtra(USE_3DTEST, false);
 		}
		LogUtil.d(WatchdogService.this, "command:"+command+",m3DTestRun:"+m3DTestRun);
		if(command == CMD_START_AGING){
			startAgingTest();
		}else if(command == CMD_STOP_AGING){
			stopAgingTest();
		}else if(command == CMD_RESTART_WATCHDOG)
		{
			startAgingTest();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	/**
	 * 启动老化测试
	 */
	public void startAgingTest(){
		if(isRunningAgingTest){
			return;
		}
		LogUtil.d(WatchdogService.this, "startAgingTest");
		isRunningAgingTest = true;
		mMainHandler.removeCallbacks(mLedCtrlAction);
		LEDSettings.offLed();
		mMainHandler.postDelayed(mLedCtrlAction, 3000);
	}

	private static final int MAX_CHECK_COUNT = 10;
	int delayCheckAging = 0;
	Runnable mLedCtrlAction = new Runnable() {
		public void run() {
			LogUtil.d(WatchdogService.this, "isRunningAgingTest:"+isRunningAgingTest);
			if(isRunningAgingTest){
				ComponentName cn = mActivityManager.getRunningTasks(1).get(0).topActivity;
				boolean isHDMINotificiation = cn.getClassName().endsWith("HDMINotificiationActivity");
			//	boolean is3DTest = mApp.is3DTestRun();
				LogUtil.d(WatchdogService.this, "m3DTestRun:"+m3DTestRun);
				if(m3DTestRun)
				{
					LogUtil.d(WatchdogService.this, "delayCheckAging:"+delayCheckAging);
					if(delayCheckAging >= MAX_CHECK_COUNT)
					{
						boolean running = AppUtils.isServiceRunning(getApplicationContext(), "com.rockchip.devicetest.aging.backgroud.AgingTestService");
						LogUtil.d(WatchdogService.this, "running:"+running);
						
						if(!running)
						{
						WatchdogService.this.sendBroadcast(new Intent("action.start.agingtest"));
						}						
						delayCheckAging = 0;
					}else{
						delayCheckAging++;
					}
					mMainHandler.removeCallbacks(mDelayStopAction);
				}else if(!cn.getPackageName().equals(getPackageName())&&!isHDMINotificiation ){
					mMainHandler.postDelayed(mDelayStopAction, 6000);
				}else{
					mMainHandler.removeCallbacks(mDelayStopAction);
				}
				if(mLEDMode==LEDMode.OFF){
					LEDSettings.onLed();
					mLEDMode = LEDMode.ON;
				}else{
					LEDSettings.offLed();
					mLEDMode = LEDMode.OFF;
				}
				LogUtil.d(WatchdogService.this, "mLEDMode:"+mLEDMode.value);
				//mMainHandler.removeCallbacks(mLedCtrlAction);
				mMainHandler.postDelayed(mLedCtrlAction, 1500);
			}
		}
	};
	
	private Runnable mDelayStopAction = new Runnable(){
		public void run() {
			ComponentName cn = mActivityManager.getRunningTasks(1).get(0).topActivity;
			boolean isHDMINotificiation = cn.getClassName().endsWith("HDMINotificiationActivity");
			if(cn.getPackageName().equals(getPackageName())||isHDMINotificiation){
				//
			}else{
				isRunningAgingTest = false;
				mMainHandler.removeCallbacks(mLedCtrlAction);
				LEDSettings.offLed();
			}
		};
	};
	
	/**
	 * 停止老化测试
	 */
	public void stopAgingTest(){
		LogUtil.d(WatchdogService.this, "stopAgingTest");
		isRunningAgingTest = false;
		mMainHandler.removeCallbacks(mLedCtrlAction);
		LEDSettings.offLed();
		stopSelf();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
}
