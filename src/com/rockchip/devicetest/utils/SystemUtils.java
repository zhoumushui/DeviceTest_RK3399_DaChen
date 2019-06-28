package com.rockchip.devicetest.utils;


import java.util.ArrayList;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.os.SystemProperties;
import android.util.Log;
import android.app.ActivityManagerNative;

import java.io.DataOutputStream;
import java.io.File;
import android.os.SystemProperties;
import android.net.EthernetManager;

public class SystemUtils {

	public static final String TAG = "SystemUtils";
	public static final String	HOST_MODE = new String("1");
	public static final String	SLAVE_MODE = new String("2");
	private static final String SYS_FILE = "/sys/bus/platform/drivers/usb20_otg/force_usb_mode";

	
	//ro.board.platform;
	public static boolean isSystemReady()
	{
		return ActivityManagerNative.isSystemReady();
	}

	public static boolean isBootComplete(){
		int sys_boot_completed =SystemProperties.getInt("sys.boot_completed",0);
		int dev_bootcomplete =SystemProperties.getInt("dev.bootcomplete",0);
		return sys_boot_completed == 1 && dev_bootcomplete == 1;
	}

	public static boolean needSetHome(Context context) {
		PackageManager mPm = context.getPackageManager();
		ArrayList<ResolveInfo> homeActivities = new ArrayList<ResolveInfo>();
		ComponentName currentDefaultHome  = mPm.getHomeActivities(homeActivities);
		if(currentDefaultHome == null)return true;
		return false;
	}

	public static void buildHomeActivitiesList(Context context) {
		PackageManager mPm = context.getPackageManager();
		ArrayList<ResolveInfo> homeActivities = new ArrayList<ResolveInfo>();
		ComponentName currentDefaultHome  = mPm.getHomeActivities(homeActivities);


		if(currentDefaultHome == null)
		{
			if(homeActivities.size()>1)
			{
				ComponentName[] mHomeComponentSet = new ComponentName[homeActivities.size()];
				ComponentName newHome = null;
				for (int i = 0; i < homeActivities.size(); i++) {
					final ResolveInfo candidate = homeActivities.get(i);
					final ActivityInfo info = candidate.activityInfo;
					ComponentName activityName = new ComponentName(info.packageName, info.name);
					mHomeComponentSet[i] = activityName;
					if(activityName != null &&
							activityName.getClassName() != null && 
							activityName.getClassName().indexOf("com.android.launcher")!=-1)
					{
						newHome = activityName;
					}

				}
				if(newHome == null) newHome =  mHomeComponentSet[0];
				LogUtil.d(context,"buildHomeActivitiesList newHome.getClassName() ="+newHome.getClassName());
				IntentFilter mHomeFilter = new IntentFilter(Intent.ACTION_MAIN);
				mHomeFilter.addCategory(Intent.CATEGORY_HOME);
				mHomeFilter.addCategory(Intent.CATEGORY_DEFAULT);
				mPm.replacePreferredActivity(mHomeFilter, IntentFilter.MATCH_CATEGORY_EMPTY,
						mHomeComponentSet, newHome);
			}
		}else{
			LogUtil.d(context,"buildHomeActivitiesList currentDefaultHome.getClassName() ="+currentDefaultHome.getClassName());
		}

	}


	//firefly开发板专有双系统设备重置增加的操作！
	public static void fireflyRecovery(){
		try{
			File f=new File("/dev/block/mtd/by-name/linuxroot");
			if(f.exists()){
				String cmd[] = {
						"(echo -n \"boot-recovery\\0firefly-recovery\" | busybox dd bs=16k seek=1 conv=sync; busybox dd if=/dev/zero bs=16k count=1) > /dev/block/mtd/by-name/misc \n"
				};
				Process process = null;

				process = Runtime.getRuntime().exec("/system/bin/sh");
				for(int i = 0; i < cmd.length; i++){
					DataOutputStream dos = new DataOutputStream(process.getOutputStream());
					dos.writeBytes(cmd[i]);	
					dos.flush();
				}
			}  
		} catch(Exception e) {

		}
	}
	
	public static void doMasterClear(Context context,boolean eraseSdCard) {
        Intent intent = new Intent(Intent.ACTION_MASTER_CLEAR);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
        intent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE, eraseSdCard);
        context.sendBroadcast(intent);
        // Intent handling is asynchronous -- assume it will happen soon.
    }

	public static void startLogSave()
	{
		int status = SystemProperties.getInt("app.logsave.start", 0);
		Log.d("TestService", "startLogSave: status is"+(status==1?"already run":"not run"));
		if(status != 1)
		{
			SystemProperties.set("app.logsave.start","1");
			Log.d("TestService", "startLogSave: now startLogsave");
		}
	}

	public static void setVolume(Context context, String volumeString)
	{
		if(volumeString == null || volumeString.length() == 0)return;
		int volume = Integer.parseInt(volumeString);
		if(volume <= 0 || volume > 100){
			Log.d("TestService", "volume is out of range!");
			return;
		}
		AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int currentVolume = maxVolume * volume / 100;

		LogUtil.d(context,"currentVolume="+currentVolume+"   ,maxVolume="+maxVolume);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
	}

	public static String getUSBMode() {
		// File file = new File(SYS_FILE);
		// if(file != null && file.exists()){
		// 	return FileUtils.readFromFile(file);
		// } else {
		// 	Log.e(TAG, SYS_FILE+"get File err!");
		// 	return "";
		// }
		return "";
	}
	
	public static void setUSBMode(String mode) {
		// File file = new File(SYS_FILE);
		// if(file != null && file.exists()){
		// 	FileUtils.write2File(file, mode);
		// } else {
		// 	Log.e(TAG, SYS_FILE+"get File err!");
		// }
	}
	public static void setEthEnable(Context context,boolean enable)
	{
//		Log.v("sjf","setEthEnable enable="+enable);
//		SystemProperties.set("app.tchip.config",enable?"1":"0");
		
		
		EthernetManager mEthManager = (EthernetManager) context.getSystemService(Context.ETHERNET_SERVICE);
		if(enable){
			//mEthManager.setInterfaceEnable("eth0",true);
   //zouxf
			//mEthManager.setEthernetEnabled(true);
		}else{
			//mEthManager.setEthernetEnabled(false);
			//mEthManager.setInterfaceEnable("eth0",false);
  //zouxf
		}
	}
	
	
	public static boolean checkApkExist(Context context, String packageName) {
		if (packageName == null || packageName.length() ==0)
		return false;
		try {
			ApplicationInfo info = context.getPackageManager()
					.getApplicationInfo(packageName,
							PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

}
