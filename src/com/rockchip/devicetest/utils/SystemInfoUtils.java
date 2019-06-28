/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月8日 下午5:06:15  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月8日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

public class SystemInfoUtils {

    private static final String FILENAME_PROC_VERSION = "/proc/version";
    private static final String FILENAME_MEMINFO = "/proc/meminfo";
	
    
    /**
     * Reads a line from the specified file.
     * @param filename the file to read from
     * @return the first line, if any.
     * @throws IOException if the file couldn't be read
     */
    private static String readLine(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        try {
            return reader.readLine();
        } finally {
            reader.close();
        }
    }
    
    
    private static String readLine(String filename,String key) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename), 256);
        
        String line = null;
        try {
			while((line=reader.readLine())!=null){
				if(line.contains(key)) return line;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
            reader.close();
        }
        return line;
    }
    /**
     * 获取flash容量，单位为MB
     * @param context
     * @return
     */
    public static long getFlashSpace(Context context){
    	long rknand_space = getFlashSpaceFromRknand(context);
    	if(rknand_space != 0)return rknand_space;
    	
    	long mmc_space_1 = getFlashSpaceFromMMC(context,FILENAME_MMC_1);
    	if(mmc_space_1 != 0)return mmc_space_1;
    	
     	long mmc_space_0 = getFlashSpaceFromMMC(context,FILENAME_MMC_0);
    	if(mmc_space_0 != 0)return mmc_space_0;
    	
    	
    	String[] partitions = {"/dev", "/system", "/cache", "/metadata", "/data", Environment.getExternalStorageDirectory().getPath()};
    	long flashSize = 0;
    	for(String part : partitions){
    		StatFs stat = new StatFs(part);
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			flashSize += blockSize*totalBlocks;
    	}
    	flashSize = (long)(Math.ceil(flashSize/1024.00/1024.00/1024.00)*1024);
    	return flashSize;
    }
    /**
     * 获取flash容量
     * @param context
     * @return
     */
    public static String getFormattedFlashSpace(Context context){
    	
    	long flashSize = getFlashSpace(context);
    	return Formatter.formatFileSize(context, flashSize*1024*1024);
//    	long rknand_space = getFlashSpaceFromRknand(context);
//    	if(rknand_space != 0)return Formatter.formatFileSize(context, rknand_space*1024*1024);
//    	
//     	long mmc_space_0 = getFlashSpaceFromMMC(context,FILENAME_MMC_0);
//    	if(mmc_space_0 != 0)return Formatter.formatFileSize(context, mmc_space_0*1024*1024);
//    	
//     	long mmc_space_1 = getFlashSpaceFromMMC(context,FILENAME_MMC_1);
//    	if(mmc_space_1 != 0)return Formatter.formatFileSize(context, mmc_space_1*1024*1024);
//    	
//    	String[] partitions = {"/dev", "/system", "/cache", "/metadata", "/data", Environment.getExternalStorageDirectory().getPath()};
//    	long flashSize = 0;
//    	for(String part : partitions){
//    		StatFs stat = new StatFs(part);
//			long blockSize = stat.getBlockSize();
//			long totalBlocks = stat.getBlockCount();
//			flashSize += blockSize*totalBlocks;
//    	}
//    	flashSize = (long)(Math.ceil(flashSize/1024.00/1024.00/1024.00)*1024*1024*1024);
//    	return Formatter.formatFileSize(context, flashSize);
    }
    
    private static final String FILENAME_RKNAND = "/proc/rknand";
    private static final String KEY_RKNAND_DEVICE_CAPACITY = "Device Capacity";
    public static long getFlashSpaceFromRknand(Context context){
    	try {
			String line = readLine(FILENAME_RKNAND,KEY_RKNAND_DEVICE_CAPACITY);
			if(line == null )return 0;
			int begin = line.indexOf(':');
	        int end = line.indexOf("MB");
	        if(begin == -1 || end == -1)return 0;
	        line = line.substring(begin + 1, end).trim();
	        
	        long space = Integer.parseInt(line);
	    	       
	        return space;
		} catch (Exception e) {
		}
    	return 0;
    }
    
    private static final String FILENAME_MMC_0 = "/sys/block/mmcblk0/size";
    private static final String FILENAME_MMC_1 = "/sys/block/mmcblk1/size";
    
    public static long getFlashSpaceFromMMC(Context context,String mmc_path){
    	try {
			String line = readLine(mmc_path);
			if(line == null )return 0;
			long size = Long.valueOf(line.trim());    	       
	        return (int) (size/(512*4));
		} catch (Exception e) {
		}
    	return 0;
    }
    
    /**
     * 获取内存容量,单位为MB
     */
    public static long getRamSpace(Context context){
    	try {
			String line = readLine(FILENAME_MEMINFO);
	        int begin = line.indexOf(':');
	        int end = line.indexOf('k');
	        line = line.substring(begin + 1, end).trim();
            /*改成long，内存达4G使用int将越界！ add by lynn*/
	        long total = Integer.parseInt(line);
	        return total/1024;
		} catch (Exception e) {
		}
    	return 0;
    }
    /**
     * 获取内存容量
     */
    public static String getFormattedRamSpace(Context context){
    	try {
			String line = readLine(FILENAME_MEMINFO);
	        int begin = line.indexOf(':');
	        int end = line.indexOf('k');
	        line = line.substring(begin + 1, end).trim();
            /*改成long，内存达4G使用int将越界！ add by lynn*/
	        long total = Integer.parseInt(line);
	        String space = Formatter.formatFileSize(context, total*1024);
	        return space;
		} catch (Exception e) {
		}
    	return null;
    }
    
    /**
     * 获取App版本信息
     */
    public static String getAppVersionName(Context context){
    	try {
			PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			return pInfo.versionName;
    	} catch (NameNotFoundException e) {
    		return null;
		}  
    }
    
    /**
     * 获取内核版本
     * @return
     */
	public static String getFormattedKernelVersion() {
        try {
            return formatKernelVersion(readLine(FILENAME_PROC_VERSION));

        } catch (IOException e) {
            LogUtil.e(SystemInfoUtils.class,  "IO Exception when getting kernel version for Device Info screen");
            return "Unavailable";
        }
    }

    public static String formatKernelVersion(String rawKernelVersion) {
        // Example (see tests for more):
        // Linux version 3.0.31-g6fb96c9 (android-build@xxx.xxx.xxx.xxx.com) \
        //     (gcc version 4.6.x-xxx 20120106 (prerelease) (GCC) ) #1 SMP PREEMPT \
        //     Thu Jun 28 11:02:39 PDT 2012

        final String PROC_VERSION_REGEX =
            "Linux version (\\S+) " + /* group 1: "3.0.31-g6fb96c9" */
            "\\((\\S+?)\\) " +        /* group 2: "x@y.com" (kernel builder) */
            "(?:\\(gcc.+? \\)) " +    /* ignore: GCC version information */
            "(#\\d+) " +              /* group 3: "#1" */
            "(?:.*?)?" +              /* ignore: optional SMP, PREEMPT, and any CONFIG_FLAGS */
            "((Sun|Mon|Tue|Wed|Thu|Fri|Sat).+)"; /* group 4: "Thu Jun 28 11:02:39 PDT 2012" */

        Matcher m = Pattern.compile(PROC_VERSION_REGEX).matcher(rawKernelVersion);
        if (!m.matches()) {
        	LogUtil.e(SystemInfoUtils.class, "Regex did not match on /proc/version: " + rawKernelVersion);
            return "Unavailable";
        } else if (m.groupCount() < 4) {
        	LogUtil.e(SystemInfoUtils.class, "Regex match on /proc/version only returned " + m.groupCount()
                    + " groups");
            return "Unavailable";
        }
        return m.group(1) + "  " +                 // 3.0.31-g6fb96c9
            m.group(2) + " " + m.group(3) + "  " + // x@y.com #1
            m.group(4);                            // Thu Jun 28 11:02:39 PDT 2012
    }
}
