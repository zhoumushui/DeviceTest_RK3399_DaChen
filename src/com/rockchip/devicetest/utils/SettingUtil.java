package com.rockchip.devicetest.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class SettingUtil {

	/**
	 * 0- 关 1- 开 3- 进入数码管测试模式 全亮 4- 退出数码管测试模式
	 */
	public static final File fileLedScreen = new File("/proc/hph_led_switch");

	public static void setLedScreenMode(int mode) {
		switch (mode) {
		case 0:
			SaveFileToNode(fileLedScreen, "0");
			break;
		case 3:
			SaveFileToNode(fileLedScreen, "3");
			break;
		case 4:
			SaveFileToNode(fileLedScreen, "4");
			break;

		case 1:
		default:
			SaveFileToNode(fileLedScreen, "1");
			break;
		}
	}
	
	/**
	 * 0-关
	 * 1-开
	 * 3-进入风扇测试模式， 风扇全速转
	 * 4-退出风扇测试模式， 风扇根据风扇开关状态运行
	 */
	public static final File fileFan = new File("/proc/fan_status");
	
	public static void setFanMode(int mode) {
		switch (mode) {
		case 0:
			SaveFileToNode(fileFan, "0");
			break;
		case 3:
			SaveFileToNode(fileFan, "3");
			break;
		case 4:
			SaveFileToNode(fileFan, "4");
			break;

		case 1:
		default:
			SaveFileToNode(fileFan, "1");
			break;
		}
	}
	

	public static void SaveFileToNode(File file, String value) {
		if (file.exists()) {
			try {
				StringBuffer strbuf = new StringBuffer("");
				strbuf.append(value);
				OutputStream output = null;
				OutputStreamWriter outputWrite = null;
				PrintWriter print = null;
				try {
					output = new FileOutputStream(file);
					outputWrite = new OutputStreamWriter(output);
					print = new PrintWriter(outputWrite);
					print.print(strbuf.toString());
					print.flush();
					output.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
			}
		} else {
		}
	}

	public static int getFileInt(File file) {
		if (file.exists()) {
			try {
				InputStream inputStream = new FileInputStream(file);
				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);
				int ch = 0;
				if ((ch = inputStreamReader.read()) != -1) {
					inputStreamReader.close();
					return Integer.parseInt(String.valueOf((char) ch));
				} else {
					inputStreamReader.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

}
