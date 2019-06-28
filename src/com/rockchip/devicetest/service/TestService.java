/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月8日 下午9:45:08  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月8日      fxw         1.0         create
 *******************************************************************/

package com.rockchip.devicetest.service;

import java.io.File;

import com.rockchip.devicetest.AgingTestActivity;
import com.rockchip.devicetest.ConfigFinder;
import com.rockchip.devicetest.IndexActivity;
import com.rockchip.devicetest.TestApplication;
import com.rockchip.devicetest.constants.TypeConstants;
import com.rockchip.devicetest.enumerate.CommandType;
import com.rockchip.devicetest.enumerate.Commands;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.UsbSettings;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.SharedPreferencesEdit;
import com.rockchip.devicetest.utils.StorageList;
import com.rockchip.devicetest.utils.StringUtils;
import com.rockchip.devicetest.utils.SystemUtils;
import com.rockchip.devicetest.utils.TimerUtil;
import com.rockchip.devicetest.R;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.WindowManager;
import com.rockchip.devicetest.UnSafeShutDownActivity;
public class TestService extends Service {

	public static final String SP_CONFIG_FILE = "config";
	public static final String SP_KEY_FACTORY = "factory";
	public static final String EXTRA_KEY_TESTDATA = "TESTDATA";
	public static final String EXTRA_KEY_TESTFROM = "TESTFROM";
	public static final String FILE_FACTORY_TEST = "Factory_Test.bin";
	public static final String[] FILE_FACTORY_TEST_ARRAY = {"4G_Factory_Test.bin",
		"2G_Factory_Test.bin","Factory_Test.bin","Other_Factory_Test.bin"};
	public static final String KEY_FILE_FACTORY_TEST_PATH = "factory_test_file_path";
	public static final String KEY_FILE_AGING_TEST_PATH = "aging_test_file_path";

	public static final String FILE_AGING_TEST = "Aging_Test.bin";
	public static final String FILE_SN_TEST = "SN_Test.bin";
	private static final String APP_NAME = "DeviceTest.apk";
	private TestApplication mApp;
	private boolean isShowingApp;// 是否已显示当前应用
	private boolean isShowingUpdateDilog = false;
	private Handler mMainHandler;
	private StorageManager mStorageManager = null;

	private static final String TAG = "TestService";
	private static final boolean DEBUG = true;
	private String oldUsbMode = "";
	private void LOGV(String msg) {
		if (DEBUG)
			Log.d(TAG, msg);
	}

	// TODO FileObserve

    KeyguardManager mKeyguardManager; 
    private KeyguardLock mKeyguardLock; 
    private PowerManager mPowerManager; 
    private PowerManager.WakeLock mWakeLock; 
    
	public void onCreate() {
		super.onCreate();
		LOGV("onCreate");
		mMainHandler = new Handler();
		mApp = (TestApplication) getApplication();
		mApp.mTestService = this;
		IntentFilter ifilter = new IntentFilter();
		ifilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		ifilter.addDataScheme("file");
		oldUsbMode = SystemUtils.getUSBMode();
		
		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE); 
        mKeyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE); 
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String startFrom = null;
		if(intent != null && intent.getExtras() !=null)
		{
			startFrom = intent.getExtras().getString(EXTRA_KEY_TESTFROM,"null");
		}
		LOGV("onStartCommand,from:"+startFrom);
		if(!isShowingUpdateDilog) {
			checkUpdate();
		}
		if ("app".equals(startFrom)) {
			Log.v(TAG,"isSystemReady()="+SystemUtils.isSystemReady()+
					"isBootComplete()="+SystemUtils.isBootComplete());
			if(!SystemUtils.isBootComplete())checkHome();
			return super.onStartCommand(intent, flags, startId);
		} else if ("mount".equals(startFrom)) {
			startTest();
			LOGV("Rock Recv Mount action. " + SystemClock.uptimeMillis());
		} else if ("boot".equals(startFrom) || "system".equals(startFrom)) {
			LOGV("Rock Recv " + startFrom + " action. "
					+ SystemClock.uptimeMillis());
			if(checkMount()) {
				startTest(); 
			} else {
				stopSelf();
			}

		}
		return super.onStartCommand(intent, flags, startId);
	}
	/**
	 * 判断当前是否有测试文件，如果有，设置defaultHome，并启动Home
	 */
	private void checkHome()
	{
		LOGV("check home ,if need will set default home");
		int whichTest = inWhichTest();
		if (whichTest == IN_FACTORY_TEST || whichTest == IN_AGING_TEST) {
			LOGV("has test bin, will set default home and start");
			boolean needCheck = SystemUtils.needSetHome(this);
			LOGV("needCheck="+needCheck);
			if(needCheck)
			{
				SystemUtils.buildHomeActivitiesList(this);
				Intent homeIntent = new Intent(Intent.ACTION_MAIN, null);
				homeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				homeIntent.addCategory(Intent.CATEGORY_HOME);
				this.startActivity(homeIntent);
			}
		} else {
			stopSelf();
		}
	}

	// 检查U盘/SDCard挂载情况
	private boolean checkMount() {
		
		StorageList mStorageList = new StorageList(TestService.this);
		String[] volumnPaths = mStorageList.getVolumnPaths();
		for (String path : volumnPaths) {
			if (isMounted(path) ) {
				LogUtil.d(TestService.this, "Rock check storage, mounted. ");
				LOGV("Rock check storage, mounted. ");
				return true;
			} 
		}
		
		return false;
	}

	// 判断是否已经Mount
	private boolean isMounted(String path) {
		if (mStorageManager == null) {
			mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
		}
		String externalVolumeState = mStorageManager.getVolumeState(path);
		return externalVolumeState.equals(Environment.MEDIA_MOUNTED);
	}

	public void setShowingApp(boolean isShowing) {
		isShowingApp = isShowing;
	}

	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LOGV("onDestroy");
		releaseWakeLock();
		if(!oldUsbMode.equals(""))
			SystemUtils.setUSBMode(oldUsbMode);
	}

	// 启动测试
	public void startTest() {	
		if(isShowingApp)
			return;
		mMainHandler.removeCallbacks(mDelayRunnable);
		mMainHandler.postDelayed(mDelayRunnable, 2000);
	}

	private Runnable mDelayRunnable = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			handleTest();
		}
	};

	private static final int IN_FACTORY_TEST = 0;
	private static final int IN_AGING_TEST = 1;
	private static final int IN_SN_TEST = 2;

	Bundle testBundle = null;
	// 执行测试
	private void handleTest() {
		// 检测功能测试
		testBundle = new Bundle();

		int whichTest = inWhichTest();
		switch (whichTest) {
		case IN_FACTORY_TEST:
			LogUtil.d(this, "Rock do factory test. ");
			SystemUtils.setUSBMode(SystemUtils.HOST_MODE);
			disableKeyguard();
			startActivity(IndexActivity.class,testBundle);
			break;
		case IN_AGING_TEST:
			disableKeyguard();
			if(SharedPreferencesEdit.getInstance().isLastShutDownIsUnsafety())
			{
				LogUtil.d(this, "last shutdown is unsafe ,start checkactivity before do aging test.");
				startActivity(UnSafeShutDownActivity.class,testBundle);
			}else{
				LogUtil.d(this, "Rock do aging test.");
				startActivity(AgingTestActivity.class,testBundle);
			}
			SharedPreferencesEdit.getInstance().setBeforeLastShutDownAgingIsStart(true);
			break;
		case IN_SN_TEST:
			UsbSettings.enableADB(this);
			UsbSettings.setUsbSlaveMode();
			break;

		default:
			stopSelf();
			LogUtil.d(this, "It is not in factory/aging test mode.");
			break;
		}

	}

	private boolean checkUpdate() {

		String filePath = FileUtils.findFileByPartialName(APP_NAME, TestService.this);
		if ("".equals(filePath)) {
			LOGV("no "+APP_NAME);
			return false;					
		}
		LOGV("apk path--->" + filePath);
		int newVerCode = getApkVersionCode(filePath);
		String newVerName = getApkVersionName(filePath);

		int verCode0 = getVersionCode();
		String verName0 = getVersionName();

		LOGV("new apk version--->" + newVerCode + ":" + newVerName);
		LOGV("old apk version--->" + verCode0 + ":" + verName0);
		if ((verCode0 != -1) && (newVerCode > verCode0)) {
			doNewVersionUpdate(newVerCode, newVerName, verCode0, verName0); // 更新新版本
			return true;
		} 
		return false;
	}

	// 获得当前对象的版本
	public int getVersionCode() {
		int verCode = -1;
		PackageInfo pi;

		try {
			pi = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			if (pi != null) {
				verCode = pi.versionCode;
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			// init();
		}
		return verCode;
	}

	public String getVersionName() {
		String verName = "";
		PackageInfo pi;

		try {
			pi = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			if (pi != null) {
				verName = pi.versionName;
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			// init();
		}
		return verName;
	}

	// 获得apk版本
	private int getApkVersionCode(String filePath) {
		int code = -1;
		File file = new File(filePath);
		if (file.exists()) {
			PackageManager pm = getPackageManager();
			PackageInfo packageInfo = pm.getPackageArchiveInfo(filePath,
					PackageManager.GET_ACTIVITIES);
			code = packageInfo.versionCode;
			return code;
		} else {
			LOGV("file not exist");
			return code;
		}
	}

	private String getApkVersionName(String filePath) {
		String versionName = "";
		File file = new File(filePath);
		if (file.exists()) {
			PackageManager pm = getPackageManager();
			PackageInfo packageInfo = pm.getPackageArchiveInfo(filePath,
					PackageManager.GET_ACTIVITIES);
			versionName = packageInfo.versionName;
			return versionName;
		} else {
			LogUtil.v(TAG, "file not exist");
			return versionName;
		}
	}

	private void doNewVersionUpdate(int newVerCode, String newVerName, int verCode0, String verName0) {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer();
		sb.append(getString(R.string.current_version));
		sb.append(verName0 + " ");
		sb.append(getString(R.string.code));
		sb.append(verCode0);
		sb.append(getString(R.string.find_new_version));
		sb.append(newVerName + " ");
		sb.append(getString(R.string.code));
		sb.append(newVerCode);
		sb.append(getString(R.string.yes_no_update));
		AlertDialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.soft_update).setMessage(sb.toString());
		builder.setPositiveButton(R.string.update, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d(TAG, "PositiveButton onClick~");
				update();
				isShowingUpdateDilog = false;
			}
		});
		builder.setNegativeButton(R.string.not_update, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Log.d(TAG, "NegativeButton onClick~");	
				handleTest();
				isShowingUpdateDilog = false;
			}
		});
		dialog = builder.create();
		dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
		dialog.show();
		dialog.getWindow().getDecorView().findViewById(android.R.id.button1)
		.setFocusableInTouchMode(true);
		dialog.getWindow().getDecorView().findViewById(android.R.id.button1)
		.requestFocus();
		isShowingUpdateDilog = true;
	}

	private void update() {

		// TODO Auto-generated method stub
		try {
			String filePath = FileUtils.findFileByPartialName(APP_NAME, TestService.this);

			Intent intent = new Intent(Intent.ACTION_VIEW);
			File file = new File(filePath);
			// String type = getMIMEType(file);
			intent.setDataAndType(Uri.fromFile(file),
					"application/vnd.android.package-archive");
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			stopSelf();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void startActivity(Class activity)
	{
		startActivity(activity, null);
	}
	private void startActivity(Class activity,Bundle bundle) {// 可以改用广播来通知activity已创建
		SystemUtils.startLogSave();
		Intent agingIntent = new Intent();
		agingIntent.setClass(this, activity);
		if(bundle != null)
			agingIntent.putExtras(bundle);
		agingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(agingIntent);
	}


	/**
	 * 检测是否有Factory_Test.bin文件
	 */
	private int inWhichTest() {
		int whichTest = -1;
		String factoryFilePath = FileUtils.findFileByPartialName(TestService.FILE_FACTORY_TEST, TestService.this);
		String agingFilePath = FileUtils.findFileByPartialName(TestService.FILE_AGING_TEST, TestService.this);
		String externalStorageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		Log.d(TAG, "factoryFilePath:"+factoryFilePath+" agingFilePath:"+agingFilePath+" externalStorageFilePath:"+externalStorageFilePath);
		if(factoryFilePath != "" && factoryFilePath != null 
				&& (factoryFilePath.indexOf(externalStorageFilePath) == -1)) {
			whichTest =	IN_FACTORY_TEST;
		} else if (agingFilePath != "" && agingFilePath != null 
				&& (agingFilePath.indexOf(externalStorageFilePath) == -1)) {
			whichTest =	IN_AGING_TEST;
		} else if (factoryFilePath != "" && factoryFilePath != null){
			whichTest =	IN_FACTORY_TEST;
		} else if (agingFilePath != "" && agingFilePath != null ) {
			whichTest =	IN_AGING_TEST;
		} else if (isInSNTest()) {
			whichTest =	IN_SN_TEST;
		}

		if(whichTest == IN_FACTORY_TEST) {
			if(testBundle !=null)
				testBundle.putString(KEY_FILE_FACTORY_TEST_PATH, factoryFilePath);
			if(factoryFilePath.indexOf("uncopy") == -1) {
				backupInexternalStorageDirectory(factoryFilePath);
			}
		} else if (whichTest == IN_AGING_TEST) {
			if(testBundle !=null)
				testBundle.putString(KEY_FILE_AGING_TEST_PATH, agingFilePath);
			if(agingFilePath.indexOf("notips") != -1) {
				SharedPreferencesEdit.getInstance().setLastShutDownIsUnsafety(false);
			}
			if(agingFilePath.indexOf("uncopy") == -1 && agingFilePath.indexOf("notips") == -1) {
				backupInexternalStorageDirectory(agingFilePath);
			}
		}

		return whichTest;
	}

	private void backupInexternalStorageDirectory(final String oldFilePath) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				File externalStorageDirectory = Environment.getExternalStorageDirectory();
				String externalStorageDirectoryPath = externalStorageDirectory.getAbsolutePath();
				if(oldFilePath.indexOf(externalStorageDirectoryPath) == -1) {
					FileUtils.copyFile(oldFilePath, 
							externalStorageDirectoryPath+"/"+StringUtils.getFileNameFromPath(oldFilePath));
				}
			}
		}).start();

	}

	/**
	 * 检测是否有Aging_Test.bin文件
	 */
	private boolean isInAgingTest() {
		String filePath = FileUtils.findFileByPartialName(FILE_AGING_TEST, TestService.this);
		if(filePath != null && !filePath.equals("")) {
			if(testBundle !=null)
				testBundle.putString(KEY_FILE_AGING_TEST_PATH, filePath);
			if(filePath.indexOf("uncopy") == -1) {
				backupInexternalStorageDirectory(filePath);
			}
			return true;
		}
		return false;
	}

	/**
	 * 检测是否有SN_Test.bin文件
	 */
	private boolean isInSNTest() {
		return ConfigFinder.hasConfigFile(FILE_SN_TEST, TestService.this);
	}
	
	private void disableKeyguard()
	{
		mWakeLock = mPowerManager.newWakeLock 
				(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "Tag"); 
		mWakeLock.acquire(); 
		mKeyguardLock = mKeyguardManager.newKeyguardLock(""); 
		mKeyguardLock.disableKeyguard(); 
	}
	private void releaseWakeLock(){
		if (mWakeLock != null) { 
            mWakeLock.release(); 
            mWakeLock = null; 
        } 
	}

}
