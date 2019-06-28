package com.rockchip.devicetest.aging.backgroud;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.rockchip.devicetest.TestApplication;
import com.rockchip.devicetest.aging.AgingCallback;
import com.rockchip.devicetest.aging.AgingConfig;
import com.rockchip.devicetest.aging.AgingDelegate;
import com.rockchip.devicetest.aging.CpuTest;
import com.rockchip.devicetest.aging.MemoryTest;
import com.rockchip.devicetest.aging.VpuTest;
import com.rockchip.devicetest.constants.ResourceConstants;
import com.rockchip.devicetest.enumerate.AgingType;
import com.rockchip.devicetest.service.TestService;
import com.rockchip.devicetest.service.WatchdogService;
import com.rockchip.devicetest.utils.AppUtils;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.IniEditor;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.SharedPreferencesEdit;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.WindowManager;

public class AgingTestService extends Service implements AgingCallback{
	private static final String TAG= "3dtest";
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	private Handler handler = new Handler();
	private Timer timer;

	int mRamSize = 0;//check ram size ,为0时不check ram
	public static final String AGINGTEST_FOREGROUND_ACTION = "com.rockchip.devicetest.state.foreground";
	public static final String AGINGTEST_BACKGROUND_ACTION = "com.rockchip.devicetest.state.background";
	private static final String fileName = "Aging_Test_Video.mp4";
	private TestApplication mApp;
	private AgingDelegate mAgingDelegate;
	private IniEditor mIniConfig;
	private ActivityManager mAm;
	
//	AlarmManager mAlarmManager;   
//	PendingIntent mWatchSender;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		LogUtil.d(AgingTestService.this, "onCreate");
		Intent serviceIntent = new Intent();//启动后台服务
		serviceIntent.setClass(this, TestService.class);
		serviceIntent.putExtra(TestService.EXTRA_KEY_TESTFROM, "app");
		startService(serviceIntent);
		
		if(!MyWindowManager.isWindowShowing())
		MyWindowManager.createWindow(getApplicationContext());
		
//		mAlarmManager = (AlarmManager)this    
//				.getSystemService(Context.ALARM_SERVICE);    
//		Intent intent = new Intent(AgingTestService.this, AgingWatchReceiver.class);    
//		intent.setAction("devicetest.aging.watchdog");    
//		mWatchSender  = PendingIntent.getBroadcast(this, requestCode,    
//				intent, 0);    
		
		mAm = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE); 
		
		mApp = (TestApplication)getApplication();
		mApp.setShowingApp(true);
		mAgingDelegate = new AgingDelegate();
		initAgingDelegate();
		startTest();
		
		
		registerAntutuReceiver();

		start3DTest();
		
		Intent agingIntent = new Intent();//启动后台服务
		agingIntent.setAction("com.rockchip.devicetest.action.WATCH_DOG");
		agingIntent.putExtra(WatchdogService.COMMAND, WatchdogService.CMD_START_AGING);
		agingIntent.putExtra(WatchdogService.USE_3DTEST, true);
		agingIntent.setClass(this, WatchdogService.class);
		startService(agingIntent);
		
//		startWatchAlarm();
		AgingWatchReceiver.scheduleAlarms(this);
	}

	private void registerAntutuReceiver()
	{
		IntentFilter antutu_filter = new IntentFilter();
		antutu_filter.addAction("com.antutu.benchmark.update.UI");
		antutu_filter.addAction("com.antutu.benchmark.inter.marooned.FINISHED");
		antutu_filter.addAction("com.antutu.benchmark.inter.3D.ERROR");
		antutu_filter.addAction("com.antutu.benchmark.BENCHMARK_START");
		antutu_filter.addAction("com.antutu.benchmark.BENCHMARK_CONTINUE");
		antutu_filter.addAction("com.antutu.benchmark.marooned.EXIT");
		antutu_filter.addAction("com.antutu.benchmark.marooned.FINISHED");
		antutu_filter.addAction("com.antutu.benchmark.marooned.ERROR");
		
		IntentFilter mount_filter = new IntentFilter();
		mount_filter.addAction(Intent.ACTION_MEDIA_MOUNTED); 
		mount_filter.addDataScheme("file");  
		registerReceiver(mBroadcastReceiver, antutu_filter);
		registerReceiver(mBroadcastReceiver, mount_filter);
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		boolean stop_test = intent.getBooleanExtra("STOP_TEST", false);
		LogUtil.d(AgingTestService.this, "onStartCommand stop_test:"+stop_test);
		if (timer == null && !stop_test) {
			timer = new Timer();
			timer.scheduleAtFixedRate(new RefreshTask(), 0, 1000);
		}
		if(stop_test)
		{
			if(MyWindowManager.isWindowShowing())MyWindowManager.removeSmallWindow(this);
			
			boolean isRun = isProessRunning(AgingTestService.this,"com.antutu.benchmark.full");
			LogUtil.d(AgingTestService.this, "onStartCommand antutu "+(isRun?"is running while be kill":"not running"));
			if(isRun)
			{
				mAm.forceStopPackage("com.antutu.benchmark.full");
			}
			stopSelf();
		}
		return START_NOT_STICKY;
	}


	
	/**
	 * 初始化测试项
	 */
	private void initAgingDelegate(){
		//read config
		InputStream in = null;
		try{
			in = getAssets().open(ResourceConstants.AGING_CONFIG_FILE);
			mIniConfig = new IniEditor();
			mIniConfig.load(in);
		}catch(Exception e){
			e.printStackTrace();
			LogUtil.show(this, "Read test config failed");
			return;
		}finally{
			if(in!=null){
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
		//cpu
		AgingConfig agingConfig = new AgingConfig(mIniConfig, AgingConfig.AGING_CPU);
		if(agingConfig.isActivated()){
			mAgingDelegate.addAgingTest(new CpuTest(agingConfig, this));
		}
		//memory
		agingConfig = new AgingConfig(mIniConfig, AgingConfig.AGING_MEM);
		agingConfig.add("ram_size",String.valueOf(mRamSize));
		if(agingConfig.isActivated()){
			mAgingDelegate.addAgingTest(new MemoryTest(agingConfig, this));
		}
		//gpu
//		agingConfig = new AgingConfig(mIniConfig, AgingConfig.AGING_GPU);
//		if(agingConfig.isActivated()){
//			mAgingDelegate.addAgingTest(new GpuTest(agingConfig, this));
//		}
//		//vpu
		agingConfig = new AgingConfig(mIniConfig, AgingConfig.AGING_VPU);
		if(agingConfig.isActivated()){
			mAgingDelegate.addAgingTest(new VpuTest(agingConfig, this));
		}
//		
		mAgingDelegate.onCreate(this,MyWindowManager.getFloatView());

		//在此+1 AgingTestTimes
		int times = SharedPreferencesEdit.getInstance().getmAgingTestTimes() + 1;
		SharedPreferencesEdit.getInstance().setmAgingTestTimes(times);
	}
	
	private void startTest()
	{
		LogUtil.d(this, "startTest");
		//Disable home power
		Intent keyIntent = new Intent(AGINGTEST_FOREGROUND_ACTION);
		sendBroadcast(keyIntent);

		Intent agingIntent = new Intent();//启动后台服务
		agingIntent.setAction("com.rockchip.devicetest.action.WATCH_DOG");
		agingIntent.putExtra(WatchdogService.COMMAND, WatchdogService.CMD_START_AGING);
		agingIntent.setClass(this, WatchdogService.class);
		startService(agingIntent);

		handler.postDelayed(new Runnable() {
			public void run() {
				mAgingDelegate.onStart();
			}
		}, 30);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		LogUtil.d(AgingTestService.this, "onDestroy");
		if (timer != null)
		{
			timer.cancel();
			timer = null;
		} 
		mAgingDelegate.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
		mApp.setShowingApp(false);

		AgingWatchReceiver.cancelAlarms(this);
		
		Intent agingIntent = new Intent();//agingTestActivity 进入后台时，关闭黄色led灯
		agingIntent.setAction("com.rockchip.devicetest.action.WATCH_DOG");
		agingIntent.putExtra(WatchdogService.COMMAND, WatchdogService.CMD_STOP_AGING);
		agingIntent.setClass(this, WatchdogService.class);
		startService(agingIntent);	
		

	}

	private boolean checkAntutu3DInstall()
	{
		return AppUtils.isAppInstalled(this, "com.antutu.benchmark.full")
				&& AppUtils.isActivityExist(this, "com.antutu.benchmark.full", "com.antutu.benchmark.full.UnityPlayerActivity") ;
	}
	private void findAntutu3DApk()
	{
		String path = FileUtils.findFileByPartialName("antutufull.apk", getApplicationContext());
		Log.v(TAG,"findApk:"+path);
		if(path != null && path.length() >0)
		{
			hideNoAppDialog();
			AppUtils.installApp(getApplicationContext(), path);
		}else{
			showNoAppDialog();
		}
	}
	AlertDialog mNoAppDialog ;
	private void showNoAppDialog()
	{
		if(mNoAppDialog == null)
		{
			AlertDialog.Builder builder=new AlertDialog.Builder(this);  //先得到构造器  
			builder.setTitle("提示"); //设置标题  
			builder.setMessage("未安装3DTest(antutufull.apk),请插入U盘安装!!!"); //设置内容  
			mNoAppDialog = builder.create();  
			mNoAppDialog.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
			mNoAppDialog.setCanceledOnTouchOutside(false);
		}

		if(!mNoAppDialog.isShowing())mNoAppDialog.show();
	}
	private void hideNoAppDialog()
	{
		if(mNoAppDialog != null && mNoAppDialog.isShowing())
		{
			mNoAppDialog.dismiss();
			mNoAppDialog = null;
		}
	}
	private void start3DTest()
	{
		if(checkAntutu3DInstall())//已经安装antutu 3dTest则直接启动
		{
			retry = 0;
			
			hideNoAppDialog();
			Intent intent = new Intent("com.antutu.benchmark.full.MAROONED_RUN");
			intent.setFlags(335544320);
			intent.setComponent(new ComponentName("com.antutu.benchmark.full", "com.antutu.benchmark.full.UnityPlayerActivity"));
			intent.putExtra("uid", "8939");
			startActivity(intent);
			//intent.getStringExtra("uid");
		}else{
			findAntutu3DApk();
		}
		//Log.v(TAG, "start3DTest:"+SystemUtils.checkApkExist(getApplicationContext(), "com.antutu.benchmark.full"));
	
	}

	private static final int MAX_RETRY_COUNT = 30;//循环检测，如果antutu未启动，则再次启动
	int retry = 30;
	class RefreshTask extends TimerTask {

		@Override
		public void run() {

			boolean isRun = isProessRunning(AgingTestService.this,"com.antutu.benchmark.full");
			LogUtil.d(AgingTestService.this,"checkTask isRun:"+isRun+",retry:"+retry);
			if(isRun)
			{
				retry = 0;
			}else{
				if(retry > MAX_RETRY_COUNT)
				{
					start3DTest();
				}else{
					retry ++;
				}
			}
//			Log.v(TAG, "isRun:"+isRun); 
//			if(!MyWindowManager.isWindowShowing())
//			{
//				handler.post(new Runnable() {
//					@Override
//					public void run() {
//						MyWindowManager.createWindow(getApplicationContext());
//						//MyWindowManager.updateUsedPercent(getApplicationContext());
//					}
//				});
//			}else{
////				handler.post(new Runnable() {
////					@Override
////					public void run() {
////						MyWindowManager.updateUsedPercent(getApplicationContext());
////					}
////				});
//			}
		}

	}
	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			Log.v(TAG, arg1.getAction());
			if("com.antutu.benchmark.marooned.FINISHED".equals(arg1.getAction())
					||"com.antutu.benchmark.marooned.EXIT".equals(arg1.getAction()))
			{
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						start3DTest();
					}
				}, 1000); 

			}else if("com.antutu.benchmark.inter.3D.ERROR".equals(arg1.getAction())
					||"com.antutu.benchmark.marooned.ERROR".equals(arg1.getAction()))
			{
				mAm.forceStopPackage("com.antutu.benchmark.full");
				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						start3DTest();
					}
				}, 3000); 
			}else if(Intent.ACTION_MEDIA_MOUNTED.equals(arg1.getAction()))
			{
				if(!checkAntutu3DInstall())
				{
					findAntutu3DApk();
				}
			}
		}
	};

	public static boolean isProessRunning(Context context, String proessName) {


		boolean isRunning = false;
		ActivityManager am = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);


		List<RunningAppProcessInfo> lists = am.getRunningAppProcesses();
		for (RunningAppProcessInfo info : lists) {
			if (info.processName.equals(proessName)) {
				isRunning = true;
			}
		}


		return isRunning;
	}


	public static final String AgingErrDir = "/data/data/com.rockchip.devicetest/aginglog";

	@Override
	public void onFailed(AgingType type, String errorMsg) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
 				AgingWatchReceiver.cancelAlarms(this);

				LogUtil.d(this, "Aging test failed. "+type);
				Intent agingIntent = new Intent();//启动后台服务
				agingIntent.setAction("com.rockchip.devicetest.action.WATCH_DOG");
				agingIntent.putExtra(WatchdogService.COMMAND, WatchdogService.CMD_STOP_AGING);
				agingIntent.setClass(this, WatchdogService.class);
				startService(agingIntent);	

				try {
					File dir = new File(AgingErrDir);
					if(!dir.exists())dir.mkdirs();
					
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");  
			        String filename=format.format(new Date());
					File logFile = new File(AgingErrDir+File.separator+filename+".log");
					logFile.createNewFile();
					FileUtils.write2File(logFile, " AgingTestActivity onFailed:"+type+"-"+errorMsg);
				} catch (IOException re) {
					
				}
				
				//Disable home powerstatic final int requestCode = 101;
//				public void startWatchAlarm()
//				{
//					long firstime = SystemClock.elapsedRealtime();    
//					
//					mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime,    
//							15 * 1000, mWatchSender);  
//				}
//				public void stopWatchAlarm()
//				{
//					mAlarmManager.cancel(mWatchSender);
//				}
				Intent keyIntent = new Intent(AGINGTEST_BACKGROUND_ACTION);
				sendBroadcast(keyIntent);
				
				mAgingDelegate.onFailed();
	}
	
//	static final int requestCode = 101;
//	public void startWatchAlarm()
//	{
//		long firstime = SystemClock.elapsedRealtime();    
//		
//		mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime,    
//				15 * 1000, mWatchSender);  
//	}
//	public void stopWatchAlarm()
//	{
//		mAlarmManager.cancel(mWatchSender);
//	}

}
