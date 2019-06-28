package com.rockchip.devicetest.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;

public class AppUtils {

	/***
	 * 检查制定包名的应用是否安装时，采取模糊查找，只要报名中包含packageName字段的都算
	 * @param context
	 * @param packageName
	 * @return
	 */ 
	public static boolean isAppInstalled(Context context, String packageName) {  
        final PackageManager packageManager = context.getPackageManager();  
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);  
       // List<String> pName = new ArrayList<String>();  
        boolean isInstall = false;
        if (pinfo != null) {  
            for (int i = 0; i < pinfo.size(); i++) {  
                String pn = pinfo.get(i).packageName;  
                if(pn != null && ( pn.equals(packageName)||pn.indexOf(packageName) !=-1) )
                {
                	isInstall = true;
                	break;
                }
            }  
        }   
        return isInstall;  
    }  
	public static PackageInfo getPackageInfo(Context context, String packageName) {  
        final PackageManager packageManager = context.getPackageManager();  
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);  
       // List<String> pName = new ArrayList<String>();  
        if (pinfo != null) {  
            for (int i = 0; i < pinfo.size(); i++) {  
                String pn = pinfo.get(i).packageName;  
                if(pn != null && ( pn.equals(packageName)||pn.indexOf(packageName) !=-1) )
                {
                	return pinfo.get(i);
                }
            }  
        }   
        return null;  
    }  
	
	public static boolean isActivityExist(Context context,String package_name,String class_name)
	{
		  Intent intent = new Intent();
		  intent.setClassName(package_name, class_name);
		  return context.getPackageManager().resolveActivity(intent, 0) != null;
	}
	
	public static void installApp(Context context,String  path) {
		// TODO Auto-generated method stub
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(path)),
				"application/vnd.android.package-archive");
		intent.putExtra("AutoInstall", true);
		
		context.startActivity(intent);
	}
	
	public static boolean isServiceRunning(Context mContext, String serviceName) {  
	    boolean isWork = false;  
	    ActivityManager myAM = (ActivityManager) mContext  
	            .getSystemService(Context.ACTIVITY_SERVICE);  
	    List<RunningServiceInfo> myList = myAM.getRunningServices(40);  
	    if (myList.size() <= 0) {  
	        return false;  
	    }  
	    for (int i = 0; i < myList.size(); i++) {  
	        String mName = myList.get(i).service.getClassName().toString();  
	        if (mName.equals(serviceName)) {  
	            isWork = true;  
	            break;  
	        }  
	    }  
	    return isWork;  
	}  
}
