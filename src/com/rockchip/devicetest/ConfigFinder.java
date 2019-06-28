package com.rockchip.devicetest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.LogUtil;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.os.storage.DiskInfo;
import android.os.storage.StorageVolume;
import android.util.Log;

public class ConfigFinder {

	/**
	 * 查找配置文件
	 * 
	 * @return
	 */
	public static File findConfigFile(String file, Context context) {
		if (file == null)
			return null;
		File existedFile = null;
		String absolutePath = FileUtils.findFileByPartialName(file, context);
		if(absolutePath == null || absolutePath.equals("")) {
			return null;
		}	
		existedFile = new File(absolutePath);
		if (existedFile.exists()) {
			return existedFile;
		} else {
			return null;
		}
		
		/*There is a problem in some SDK as RK3128 with below search file method*/
//		// 0.Absolute
//		if (file.startsWith("/") || file.startsWith("\\")) {
//			return new File(file);
//		}
//
//		
//
//		// 1.External SDCard
//		File sdDir = Environment.getSecondVolumeStorageDirectory();
//		existedFile = new File(sdDir, file);
//		if (existedFile.exists()) {
//			return existedFile;
//		}
//
//		// 2.USB
//		List<String> usbList = getAliveUsbPath();
//		for (String usb : usbList) {
//			existedFile = new File(getSubUsbPath(usb), file);
//			if (existedFile.exists()) {
//				return existedFile;
//			}
//		}
//
//		// 3.Internal SDCard
//		sdDir = Environment.getExternalStorageDirectory();
//		existedFile = new File(sdDir, file);
//		if (existedFile.exists()) {
//			return existedFile;
//		}
//		// Not Found
//		return null;
	}

	public static String getSubUsbPath(String usbPath) {
		Process process;
		String temp;
		try {
			process = Runtime.getRuntime().exec("/system/bin/ls " + usbPath);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			while ((temp = reader.readLine()) != null) {
				if (temp.startsWith("udisk") && !temp.equals("udisk")) {
					usbPath += "/" + temp;
					reader.close();
					process.destroy();
					return usbPath;
				}
			}
			return usbPath;
		} catch (IOException e) {
			e.printStackTrace();
			return usbPath;
		}
	}

	/**
	 * 是否存在此配置文件
	 * 
	 * @param file
	 * @return
	 */
	public static boolean hasConfigFile(String file, Context context) {
		File searchFile = findConfigFile(file, context);
		boolean isExisted = searchFile != null && searchFile.exists();
		return isExisted;
	}

	/**
	 * 获取已经挂载的U盘
	 * 
	 * @return
	 */
	public static List<String> getAliveUsbPath(Context context) {
		List<String> usbList = new ArrayList<String>();
		StorageManager mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
		List<VolumeInfo> volumes = mStorageManager.getVolumes();
		Log.v("UsbTest","getAliveUsbPath volumes:"+volumes.size());
		for (VolumeInfo vol : volumes) {
			DiskInfo disk = vol.getDisk();
			if(disk != null) {
				Log.v("UsbTest","label:"+disk.label);
				if(disk.isUsb() && vol.getState() == VolumeInfo.STATE_MOUNTED){
					//usb dir
					StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(), false);
					usbList.add(sv.getPath());
				}
			}
		}
//		if (Environment.MEDIA_MOUNTED.equals(Environment
//				.getExternalStorageState())) {
//			String udisk = Environment.getExternalStorageDirectory()
//					.getAbsolutePath();
//			usbList.add(udisk);
//		}
//		if (Environment.MEDIA_MOUNTED.equals(Environment
//				.getHostStorage_Extern_0_State())) {
//			String udisk0 = Environment.getHostStorage_Extern_0_Directory()
//					.getAbsolutePath();
//			usbList.add(udisk0);
//		}
//		if (Environment.MEDIA_MOUNTED.equals(Environment
//				.getHostStorage_Extern_1_State())) {
//			String udisk1 = Environment.getHostStorage_Extern_1_Directory()
//					.getAbsolutePath();
//			usbList.add(udisk1);
//		}
//		if (Environment.MEDIA_MOUNTED.equals(Environment
//				.getHostStorage_Extern_2_State())) {
//			String udisk2 = Environment.getHostStorage_Extern_2_Directory()
//					.getAbsolutePath();
//			usbList.add(udisk2);
//		}
//		if (Environment.MEDIA_MOUNTED.equals(Environment
//				.getHostStorage_Extern_3_State())) {
//			String udisk3 = Environment.getHostStorage_Extern_3_Directory()
//					.getAbsolutePath();
//			usbList.add(udisk3);
//		}
//		if (Environment.MEDIA_MOUNTED.equals(Environment
//				.getHostStorage_Extern_4_State())) {
//			String udisk4 = Environment.getHostStorage_Extern_4_Directory()
//					.getAbsolutePath();
//			usbList.add(udisk4);
//		}
//		if (Environment.MEDIA_MOUNTED.equals(Environment
//				.getHostStorage_Extern_5_State())) {
//			String udisk5 = Environment.getHostStorage_Extern_5_Directory()
//					.getAbsolutePath();
//			usbList.add(udisk5);
//		}
		return usbList;
	}
	
	/**
	 * 获取已经挂载的PCIE
	 * 
	 * @return
	 */
	public static String getAlivePciePath(Context context,ArrayList<String> uuid_list) {
		StorageManager mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
		List<VolumeInfo> volumes = mStorageManager.getVolumes();
		Log.v("UsbTest","getAliveUsbPath volumes:"+volumes.size());
		for (VolumeInfo vol : volumes) {
			DiskInfo disk = vol.getDisk();
			if(disk != null) {
				//if(disk.isPcie() && vol.getState() == VolumeInfo.STATE_MOUNTED){
					//usb dir
				//	StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(), false);
				//	return sv.getPath();
				//}
			}
		}
		if(uuid_list != null && uuid_list.size() > 0)
		{
			for (VolumeInfo vol : volumes) {
				DiskInfo disk = vol.getDisk();
				if(disk != null) {
					StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(), false);
					if(disk.isUsb() && vol.getState() == VolumeInfo.STATE_MOUNTED && uuid_list.contains(sv.getUuid())){
						return sv.getPath();
					}
				}
			}
		}
		return null;
	}
	
	
	/**
	 * 获取已经挂载的Sdcard
	 * 
	 * @return
	 */
	public static String getAliveSdcardPath(Context context) {
		StorageManager mStorageManager = (StorageManager) context.getSystemService(Activity.STORAGE_SERVICE);
		List<VolumeInfo> volumes = mStorageManager.getVolumes();
		Log.v("UsbTest","getAliveUsbPath volumes:"+volumes.size());
		for (VolumeInfo vol : volumes) {
			DiskInfo disk = vol.getDisk();
			if(disk != null) {
				if(disk.isSd() && vol.getState() == VolumeInfo.STATE_MOUNTED){
					//usb dir
					StorageVolume sv = vol.buildStorageVolume(context, context.getUserId(), false);
					return sv.getPath();
				}
			}
		}
		return null;
	}
}
