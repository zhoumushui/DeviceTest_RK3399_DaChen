/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月15日 下午5:01:01  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月15日      fxw         1.0         create
 *******************************************************************/

package com.rockchip.devicetest.aging.cpu;

import java.io.BufferedReader;
import android.os.SystemProperties;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Pattern;

import android.util.Log;

import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.StringUtils;

public class CpuInfoReader {

	public static final String SOC = "/sys/devices/system/cpu/soc";
	public static final String SCALING_CUR_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
	public static final String CPUINFO_MAX_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";
	public static final String CPUINFO_MIN_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq";
	public static final String SCALING_AVAILABLE_FREQUENCIES = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies";
	public static final String CPUINFO_TEMPERATURE = "/sys/devices/ff280000.tsadc/temp1_input";

	public static final String RK3399_CPUINFO_TEMPERATURE = "/sys/class/thermal/thermal_zone0/temp";
	public static final String RK3399_GPUINFO_TEMPERATURE = "/sys/class/thermal/thermal_zone1/temp";

	
	public static final String CPU0_SCALING_CUR_FREQ = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
	public static final String CPU4_SCALING_CUR_FREQ = "/sys/devices/system/cpu/cpu4/cpufreq/scaling_cur_freq";

	public static final String[] CPUINFO_TEMPERATURE_FIREFLY =
		{
            "/sys/devices/ff280000.tsadc/temp0_input",
            "/sys/devices/ff280000.tsadc/temp1_input",
            "/sys/devices/ff280000.tsadc/temp2_input",
            "/sys/devices/ff280000.tsadc/temp3_input",
		};

	public static float getProcessCpuRate() {
		float totalCpuTime1 = getCpuTime()[0];
		float processCpuTime1 = getAppCpuTime();
		try {
			Thread.sleep(360);
		} catch (Exception e) {
		}

		float totalCpuTime2 = getCpuTime()[0];
		float processCpuTime2 = getAppCpuTime();

		float cpuRate = 100 * (processCpuTime2 - processCpuTime1)
				/ (totalCpuTime2 - totalCpuTime1);

		return cpuRate;
	}

	// 获取系统总CPU使用时间和空闲时间
	public static long[] getCpuTime() {
		long[] cpuInfo = new long[2];
		String[] cpuInfos = null;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream("/proc/stat")), 1000);
			String load = reader.readLine();
			reader.close();
			cpuInfos = load.split(" ");
		} catch (IOException ex) {
			ex.printStackTrace();
			return cpuInfo;
		}

		long totalCpu = Long.parseLong(cpuInfos[2])
				+ Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
				+ Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5])
				+ Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
		cpuInfo[0] = totalCpu;
		cpuInfo[1] = Long.parseLong(cpuInfos[5]);
		return cpuInfo;
	}

	public static long getAppCpuTime() {
		String[] cpuInfos = null;
		try {
			int pid = android.os.Process.myPid();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream("/proc/" + pid + "/stat")), 1000);
			String load = reader.readLine();
			reader.close();
			cpuInfos = load.split(" ");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		long appCpuTime = Long.parseLong(cpuInfos[13])
				+ Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
				+ Long.parseLong(cpuInfos[16]);
		return appCpuTime;
	}


	/**
	 * 获取CPU核数
	 * @return
	 */
	public static int getCpuCores() {
		// Private Class to display only CPU devices in the directory listing
		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathname) {
				// Check if filename is "cpu", followed by a single digit number
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}

		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			// Return the number of cores (virtual CPU devices)
			return files.length;
		} catch (Exception e) {
			e.printStackTrace();
			// Default to return 1 core
			return -1;
		}
	}

	/**
	 * 获取CPU型号
	 * @return
	 */
	public static String getCpuModel(){
		String model = FileUtils.readFromFile(new File(SOC));
		if(model != null && model.length()>0)return model;
		return SystemProperties.get("ro.board.platform");
	}

	/**
	 * 获取CPU最低频率
	 * @return
	 */
	public static int getCpuMinFreq(){
		String minfreq = FileUtils.readFromFile(new File(CPUINFO_MIN_FREQ));
		return StringUtils.parseInt(minfreq, -1);
	}

	/**
	 * 获取CPU最高频率
	 * @return
	 */
	public static int getCpuMaxFreq(){
		String maxfreq = FileUtils.readFromFile(new File(CPUINFO_MAX_FREQ));
		return StringUtils.parseInt(maxfreq, -1);
	}

	/**
	 * 获取CPU当前频率
	 * @return
	 */
	public static int getCpuCurrentFreq(){
		String currfreq = FileUtils.readFromFile(new File(SCALING_CUR_FREQ));//cpuinfo_cur_freq
		return StringUtils.parseInt(currfreq, -1);
	}
	
	public static int getCpu0CurrentFreq(){
		String currfreq = FileUtils.readFromFile(new File(CPU0_SCALING_CUR_FREQ));//cpuinfo_cur_freq
		return StringUtils.parseInt(currfreq, -1);
	}
	
	public static int getCpu4CurrentFreq(){
		String currfreq = FileUtils.readFromFile(new File(CPU4_SCALING_CUR_FREQ));//cpuinfo_cur_freq
		return StringUtils.parseInt(currfreq, -1);
	}
	/*
	public static List<Integer> getAvailableFrequencies() {
		String freqs = FileUtils.readFromFile(new File(SCALING_AVAILABLE_FREQUENCIES));
	}
	 */

	/**
	 * 获取CPU温度
	 * @return
	 */
	
	
	public static String getCpuTemperature() {
		if(new File(RK3399_CPUINFO_TEMPERATURE).exists())
		{
			String cpu_temperature = FileUtils.readFromFile(new File(RK3399_CPUINFO_TEMPERATURE));
			String gpu_temperature = FileUtils.readFromFile(new File(RK3399_GPUINFO_TEMPERATURE));
			int cpu_tmp = StringUtils.parseInt(cpu_temperature, -1);
			int gpu_tmp = StringUtils.parseInt(gpu_temperature, -1);
			if(cpu_tmp == -1)
				cpu_temperature = "* null *";
			else{
				cpu_temperature =  " *"+cpu_tmp/1000+" °C* ";
			}
			if(gpu_tmp == -1)
				gpu_temperature = "* null *";
			else{
				gpu_temperature =  " *"+gpu_tmp/1000+" °C* ";
			}
			
			return cpu_temperature +"   / GPU温度:"+gpu_temperature;			
		}else if(new File(CPUINFO_TEMPERATURE).exists())
		{
			String temperature = FileUtils.readFromFile(new File(CPUINFO_TEMPERATURE));
			int tmp = StringUtils.parseInt(temperature, -1);
			if(tmp == -1)
				return null;
			else{
				return " *"+tmp+" °C* ";
			}
		}else{
			String temperature = "";
			for(int i=0;i<CPUINFO_TEMPERATURE_FIREFLY.length;i++)
			{
				int tmp = StringUtils.parseInt(FileUtils.readFromFile(new File(CPUINFO_TEMPERATURE_FIREFLY[i])), -1);
				if(tmp == -1)
				{
					temperature += " *null* ";
				}else{
					temperature += " *"+tmp+" °C* ";
				}
			}
			return temperature;			
		}
	}

}
