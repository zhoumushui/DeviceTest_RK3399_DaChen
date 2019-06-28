/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月14日 下午2:25:05  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月14日      fxw         1.0         create
 *******************************************************************/   

package com.rockchip.devicetest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.rockchip.devicetest.aging.AgingCallback;
import com.rockchip.devicetest.aging.AgingConfig;
import com.rockchip.devicetest.aging.AgingDelegate;
import com.rockchip.devicetest.aging.CpuTest;
import com.rockchip.devicetest.aging.GpuTest;
import com.rockchip.devicetest.aging.MemoryTest;
import com.rockchip.devicetest.aging.VpuTest;
import com.rockchip.devicetest.aging.backgroud.AgingTestService;
import com.rockchip.devicetest.constants.ResourceConstants;
import com.rockchip.devicetest.enumerate.AgingType;
import com.rockchip.devicetest.service.TestService;
import com.rockchip.devicetest.service.WatchdogService;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.IniEditor;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.SharedPreferencesEdit;
import com.rockchip.devicetest.utils.SystemInfoUtils;
import com.rockchip.devicetest.utils.SystemUtils;
import com.rockchip.devicetest.utils.TestConfigReader;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class AgingTestActivity extends BaseActivity implements AgingCallback {

	public static final String AGINGTEST_FOREGROUND_ACTION = "com.rockchip.devicetest.state.foreground";
	public static final String AGINGTEST_BACKGROUND_ACTION = "com.rockchip.devicetest.state.background";
	private static final String fileName = "Aging_Test_Video.mp4";
	private TestApplication mApp;
	private AgingDelegate mAgingDelegate;
	private IniEditor mIniConfig;
	private Handler mMainHandler;
	private int mKeyBackCount;
	private boolean hasPassedFactory;
	private boolean useAntutu3DTest= true;
	private Toast mBackToast;
	private Button resetDevice;
	private TextView tv;

	private View aging_activity_layout ;
	private boolean initDone = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogUtil.d(this, "onCreate");
		SharedPreferencesEdit.getInstance().setContext(AgingTestActivity.this);
		setContentView(R.layout.test_aging);
		mMainHandler = new Handler();
		Intent serviceIntent = new Intent();//启动后台服务
		serviceIntent.setClass(this, TestService.class);
		serviceIntent.putExtra(TestService.EXTRA_KEY_TESTFROM, "app");
		startService(serviceIntent);


		aging_activity_layout = (View)findViewById(R.id.aging_activity_layout);
		//version
		TextView softVersionText = (TextView)findViewById(R.id.tv_soft_ver2);
		softVersionText.setText(SystemInfoUtils.getAppVersionName(this));

		// 点击退出老化测试
		tv = (TextView) findViewById(R.id.exit);
		tv.setOnClickListener(clickListener);

		resetDevice = (Button) findViewById(R.id.reset_device);
		resetDevice.setOnClickListener(clickListener);

		mApp = (TestApplication)getApplication();
		mAgingDelegate = new AgingDelegate();

		hasPassedFactory = hadPassFactoryTest();
		if(!hasPassedFactory){
			return;
		}
		/*initAgingDelegate();
		mAgingDelegate.onCreate(this);*/


		init();
	}

	AlertDialog mCoyingDialog;

	public void init()
	{
		String videoPath = "/mnt/sdcard/"+fileName;//this.getFilesDir()+"/"+fileName;
		File videoFile = new File(videoPath);
		if(videoFile.exists() && videoFile.length()>0)
		{	
			initDone();
		}else{
			String findFilePath = FileUtils.findFileByPartialName(fileName, AgingTestActivity.this);
			if (findFilePath != null && !findFilePath.equals("")) {
				showDialog();
				copyDataInBackground(findFilePath,videoPath);
			} else {
				exitDialog();
			}
		}
	}

	private void initDone() {

		initAgingDelegate();
		mAgingDelegate.onCreate(this,aging_activity_layout);

		//在此+1 AgingTestTimes
		int times = SharedPreferencesEdit.getInstance().getmAgingTestTimes() + 1;
		SharedPreferencesEdit.getInstance().setmAgingTestTimes(times);

		initDone = true;
	}

	private View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v == tv) {
				System.exit(0);
			} else if (v == resetDevice){
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(
						AgingTestActivity.this);
				builder.setTitle(R.string.pub_reset_device);
				builder.setMessage(R.string.pub_reset_device_msg);
				builder.setPositiveButton(
						getString(R.string.pub_cancel),
						new OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {								
								dialog.dismiss();								
							}
						});
				builder.setNegativeButton(
						getString(R.string.pub_reset_device),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								SystemUtils.fireflyRecovery();
								LogUtil.d(this,"恢复出厂设置");
								SystemUtils.doMasterClear(getApplicationContext(), false);					
							}
						});
				builder.create().show();
			}

		}
	};


	private void copyDataInBackground(String fromPath,String toPath) {
		final String _fromPath = fromPath;
		final String _toPath = toPath;
		Thread thread = new Thread() {
			public void run() {
				LogUtil.d(AgingTestActivity.this, "fromPath="+_fromPath+",toPath="+_toPath);
				boolean isCopySuccess = FileUtils.copyFile(_fromPath, _toPath);
				LogUtil.d(AgingTestActivity.this, "isCopySuccess="+isCopySuccess);

				if (isCopySuccess) {
					mMainHandler.post(new Runnable() {

						@Override
						public void run() {
							dismissDialog();
							initDone();
							startTest();
						}
					});
				} else {
					mMainHandler.post(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							exitDialog();
						}
					});
				}
			}
		};
		thread.start();
	}


	private void showDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.pub_prompt);
		View view =  LayoutInflater.from(this).inflate(R.layout.copy_file_progress,
				null);
		builder.setView(view);
		mCoyingDialog = builder.create();
		mCoyingDialog.show();
	}

	private void exitDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.pub_fail);
		builder.setMessage(R.string.no_file);
		builder.setNegativeButton("退出", new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				System.exit(0);
			}
		});
		mCoyingDialog = builder.create();
		mCoyingDialog.show();
	}

	private void dismissDialog() {
		if (mCoyingDialog != null)
			mCoyingDialog.dismiss();
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
		agingConfig = new AgingConfig(mIniConfig, AgingConfig.AGING_GPU);
		if(agingConfig.isActivated()){
			mAgingDelegate.addAgingTest(new GpuTest(agingConfig, this));
		}
		//vpu
		agingConfig = new AgingConfig(mIniConfig, AgingConfig.AGING_VPU);
		if(agingConfig.isActivated()){
			mAgingDelegate.addAgingTest(new VpuTest(agingConfig, this));
		}
	}

	private void startTest()
	{
		LogUtil.d(this, "startTest");
		if(useAntutu3DTest)
		{
			startService(new Intent(this,AgingTestService.class));
			finish();
		}
		//Disable home power
		Intent keyIntent = new Intent(AGINGTEST_FOREGROUND_ACTION);
		sendBroadcast(keyIntent);

		Intent agingIntent = new Intent();//启动后台服务
		agingIntent.setAction("com.rockchip.devicetest.action.WATCH_DOG");
		agingIntent.putExtra(WatchdogService.COMMAND, WatchdogService.CMD_START_AGING);
		agingIntent.setClass(this, WatchdogService.class);
		startService(agingIntent);

		mMainHandler.postDelayed(new Runnable() {
			public void run() {
				mAgingDelegate.onStart();
			}
		}, 30);
	}

	@Override
	protected void onStart() {
		super.onStart();
		LogUtil.d(this, "onStart initDone="+initDone);
		mApp.setShowingApp(true);
		if(!hasPassedFactory){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.pub_prompt);
			builder.setMessage(R.string.aging_test_check);
			builder.setPositiveButton(getString(R.string.pub_success), new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			});
			builder.setCancelable(false);
			builder.create().show();
			return;
		}
		if(initDone)
		{
			startTest();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		LogUtil.d(this, "onResume");
	}

	@Override
	protected void onPause() {
		super.onPause();
		LogUtil.d(this, "onPause");
	}

	protected void onStop() {
		super.onStop();
		LogUtil.d(this, "onStop");
		if(!useAntutu3DTest)
		       mApp.setShowingApp(false);
		if(!hasPassedFactory){
			return;
		}
		mAgingDelegate.onStop();
		
		if(!useAntutu3DTest)
		{
			Intent agingIntent = new Intent();//agingTestActivity 进入后台时，关闭黄色led灯
			agingIntent.setAction("com.rockchip.devicetest.action.WATCH_DOG");
			agingIntent.putExtra(WatchdogService.COMMAND, WatchdogService.CMD_STOP_AGING);
			agingIntent.setClass(this, WatchdogService.class);
			startService(agingIntent);		
		}
		//Disable home power
		Intent keyIntent = new Intent(AGINGTEST_BACKGROUND_ACTION);
		sendBroadcast(keyIntent);
	}

	protected void onDestroy() {
		super.onDestroy();
		LogUtil.d(this, "onDestroy");
		if(!hasPassedFactory){
			return;
		}
		mAgingDelegate.onDestroy();
	}

	int mRamSize = 0;//check ram size ,为0时不check ram
	/**
	 * 获取是否已通过功能测试
	 */
	private boolean hadPassFactoryTest(){
		
		File factoryFile = null;
		Bundle testBundle = getIntent().getExtras();
		if(testBundle != null && testBundle.getString(TestService.KEY_FILE_AGING_TEST_PATH)!=null)
		{
			factoryFile = new File(testBundle.getString(TestService.KEY_FILE_AGING_TEST_PATH));
		}else{
			String filePath = FileUtils.findFileByPartialName(TestService.FILE_AGING_TEST, AgingTestActivity.this);
//			factoryFile = ConfigFinder.findConfigFile(TestService.FILE_AGING_TEST);
			factoryFile = new File(filePath);
		} 
		
		IniEditor mUserConfig = new IniEditor();
		if(factoryFile!=null&&factoryFile.exists()){
			TestConfigReader configReader = new TestConfigReader();
			mUserConfig = configReader.loadConfig(factoryFile);
			String required = mUserConfig.get("FactoryTest", "required");
			String ramsize = mUserConfig.get("FactoryTest", "ramsize");
			if(!TextUtils.isEmpty(ramsize))
			{
					try {
						mRamSize = Integer.valueOf(ramsize);
					} catch (Exception e) {
						// TODO: handle exception
					}
			}
			if("0".equals(required)){//不需要通过工厂测试
				return true;
			}
		}

		File passFile = new File(Environment.getExternalStorageDirectory(), "ftest_pass.bin");
		if(passFile.exists()){
			return true;
		}

		int mode = Context.MODE_PRIVATE|Context.MODE_MULTI_PROCESS;
		SharedPreferences sp = getSharedPreferences(TestService.SP_CONFIG_FILE, mode);
		return sp.getBoolean(TestService.SP_KEY_FACTORY, false);
	}

	@Override
	public void onBackPressed() {
		/*
		mKeyBackCount++;
		mMainHandler.removeCallbacks(mKeyBackAction);
		mMainHandler.postDelayed(mKeyBackAction, 3000);
		final int totalCnt = 5;
		if(mKeyBackCount>=totalCnt)
			super.onBackPressed();
		else{
			if(mBackToast==null){
				mBackToast = Toast.makeText(this, getString(R.string.aging_test_back, totalCnt-mKeyBackCount), Toast.LENGTH_SHORT);
			}
			mBackToast.setText(getString(R.string.aging_test_back, totalCnt-mKeyBackCount));
			mBackToast.show();
		}
		 */
	}
	Runnable mKeyBackAction = new Runnable() {
		public void run() {
			mKeyBackCount = 0;
		}
	};
	public static final String AgingErrDir = "/data/data/com.rockchip.devicetest/aginglog";
	/***
	 * 测试失败
	 */
	@Override
	public void onFailed(AgingType type, String errorMsg) {
		// TODO Auto-generated method stub
		LogUtil.d(this, "Aging test failed. "+type);
		if(!useAntutu3DTest)
		{
			Intent agingIntent = new Intent();//启动后台服务
			agingIntent.setAction("com.rockchip.devicetest.action.WATCH_DOG");
			agingIntent.putExtra(WatchdogService.COMMAND, WatchdogService.CMD_STOP_AGING);
			agingIntent.setClass(this, WatchdogService.class);
			startService(agingIntent);	
		}
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
		
		//Disable home power
		Intent keyIntent = new Intent(AGINGTEST_BACKGROUND_ACTION);
		sendBroadcast(keyIntent);
		
		mAgingDelegate.onFailed();
		
		

		
	}
	
	

}
