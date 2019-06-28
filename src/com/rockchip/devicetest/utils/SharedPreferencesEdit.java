package com.rockchip.devicetest.utils;

import android.content.Context;
import android.content.SharedPreferences;




public class SharedPreferencesEdit
{
	/**
	 * @brief 本类内部调用的实例
	 */
	private static SharedPreferencesEdit sharedPreferencesEdit = new SharedPreferencesEdit();
	
	private SharedPreferences mSharedPreferences ;
	private Context mContext;
	private int systemAbnormalRestartTime = 0; 
	boolean isSystemStart = true; 
	private int mAgingTestTimes = 0 ;
	
	private String mIperfVersion = "";
	
	private boolean lastShutDownIsUnsafe = false;
	private boolean beforeLastShutDownAgingIsStart = false;

	/**
	 * @brief  得到对象的实例
	 * @return DlnaControlClient
	 */
	public static SharedPreferencesEdit getInstance() {
//		Log.d(TAG, "getInstance() start ");
		if (sharedPreferencesEdit == null) {
			sharedPreferencesEdit = new SharedPreferencesEdit();
		}
//		Log.d(TAG, "getInstance() end ");
		return sharedPreferencesEdit;
	}
	
	public void setContext(Context context) {
		mContext = context;
		mSharedPreferences = mContext.getApplicationContext().getSharedPreferences("TchipSharedPreferences", 0);  
		systemAbnormalRestartTime = mSharedPreferences.getInt("SystemAbnormalRestartTime", 0);
		isSystemStart = mSharedPreferences.getBoolean("isSystemStart", false);
		mAgingTestTimes = mSharedPreferences.getInt("mAgingTestTimes", 0); 
		
		mIperfVersion = mSharedPreferences.getString("iperf_version", "");
		
		lastShutDownIsUnsafe = mSharedPreferences.getBoolean("lastShutDownIsUnsafe", false);
		
		beforeLastShutDownAgingIsStart = mSharedPreferences.getBoolean("beforeLastShutDownAgingIsStart", false);
	}

	public boolean isAgingStartBeforeLastShutDown()
	{
		beforeLastShutDownAgingIsStart = mSharedPreferences.getBoolean("beforeLastShutDownAgingIsStart", false);
		return beforeLastShutDownAgingIsStart;
	}
	public void setBeforeLastShutDownAgingIsStart(boolean isstart)
	{
		SharedPreferences.Editor mEditor = mSharedPreferences.edit();  
		mEditor.putBoolean("beforeLastShutDownAgingIsStart", isstart);  
		mEditor.commit();  	
		this.beforeLastShutDownAgingIsStart = isstart;
	}
	
	public boolean isLastShutDownIsUnsafety()
	{
		lastShutDownIsUnsafe = mSharedPreferences.getBoolean("lastShutDownIsUnsafe", false);
		return  lastShutDownIsUnsafe;
	}
	public void setLastShutDownIsUnsafety(boolean unsafe)
	{
		SharedPreferences.Editor mEditor = mSharedPreferences.edit();  
		mEditor.putBoolean("lastShutDownIsUnsafe", unsafe);  
		mEditor.commit();  	
		this.lastShutDownIsUnsafe = unsafe;
	}
	
	public int getSystemAbnormalRestartTime() {
		return mSharedPreferences.getInt("SystemAbnormalRestartTime", 0);
	}
	
	public void setSystemAbnormalRestartTime(int systemAbnormalRestartTime) {
		SharedPreferences.Editor mEditor = mSharedPreferences.edit();  
		mEditor.putInt("SystemAbnormalRestartTime", systemAbnormalRestartTime);  
		mEditor.commit();  	
		this.systemAbnormalRestartTime = systemAbnormalRestartTime;
	}
	
	public boolean isSystemStart() {
		return mSharedPreferences.getBoolean("isSystemStart", false);
	}

	public void setSystemStart(boolean isSystemStart) {
		SharedPreferences.Editor mEditor = mSharedPreferences.edit();  
		mEditor.putBoolean("isSystemStart", isSystemStart);  
		mEditor.commit();  				 
		this.isSystemStart = isSystemStart;
	}


	public int getmAgingTestTimes() {
		return mSharedPreferences.getInt("mAgingTestTimes", 0);
	}

	public void setmAgingTestTimes(int agingTestTimes) {
		SharedPreferences.Editor mEditor = mSharedPreferences.edit();  
		mEditor.putInt("mAgingTestTimes", agingTestTimes);  
		mEditor.commit();  	
		this.mAgingTestTimes = agingTestTimes;
	}

	public String getIperfVersion() {
		return mSharedPreferences.getString("iperf_version", "");
	}

	public void setIperfVersion(String mIperfVersion) {
		this.mIperfVersion = mIperfVersion;
		SharedPreferences.Editor mEditor = mSharedPreferences.edit();  
		mEditor.putString("iperf_version", mIperfVersion);
		mEditor.commit();  				 
	}
	
	

	



}
