package com.rockchip.devicetest.aging.backgroud;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.rockchip.devicetest.R;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

public class MyWindowManager {


	private static FloatWindowView floatWindow;


	/**
	 * 小悬浮窗View的参数
	 */
	private static LayoutParams windowParams;

	/**
	 * 用于控制在屏幕上添加或移除悬浮窗
	 */
	private static WindowManager mWindowManager;

	/**
	 * 用于获取手机可用内存
	 */
	private static ActivityManager mActivityManager;

	/**
	 * 创建一个小悬浮窗。
	 * 
	 * @param context
	 *            必须为应用程序的Context.
	 */
	public static void createWindow(Context context) {
		WindowManager windowManager = getWindowManager(context);
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		if (floatWindow == null) {
			floatWindow = new FloatWindowView(context);
			if (windowParams == null) {
				windowParams = new LayoutParams();
				windowParams.type = LayoutParams.TYPE_PHONE;
				windowParams.format = PixelFormat.RGBA_8888;
				windowParams.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
						| LayoutParams.FLAG_NOT_FOCUSABLE;
				windowParams.gravity = Gravity.RIGHT | Gravity.TOP;
				windowParams.width = FloatWindowView.viewWidth;
				windowParams.height = FloatWindowView.viewHeight;
				windowParams.x = screenWidth;//screenWidth;
				windowParams.y = screenHeight / 2;
			}
			floatWindow.setParams(windowParams);
			windowManager.addView(floatWindow, windowParams);
		}
	}

	/**
	 * 将小悬浮窗从屏幕上移除。
	 * 
	 * @param context
	 *            必须为应用程序的Context.
	 */
	public static void removeSmallWindow(Context context) {
		if (floatWindow != null) {
			WindowManager windowManager = getWindowManager(context);
			windowManager.removeView(floatWindow);
			floatWindow = null;
		}
	}

	

//	/**
//	 * 更新悬浮窗的TextView上的数据，显示内存使用的百分比。
//	 * 
//	 * @param context
//	 *            可传入应用程序上下文。
//	 */
//	public static void updateUsedPercent(Context context) {
//		if (floatWindow != null) {
//			TextView percentView = (TextView) floatWindow.findViewById(R.id.percent);
//			percentView.setText(getUsedPercentValue(context));
//		}
//	}

	/**
	 * 是否有悬浮窗(包括小悬浮窗和大悬浮窗)显示在屏幕上。
	 * 
	 * @return 有悬浮窗显示在桌面上返回true，没有的话返回false。
	 */
	public static boolean isWindowShowing() {
		return floatWindow != null ;
	}
	
	public static View getFloatView() {
		return floatWindow.getFloatLayout(); 
	}

	/**
	 * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
	 * 
	 * @param context
	 *            必须为应用程序的Context.
	 * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
	 */
	private static WindowManager getWindowManager(Context context) {
		if (mWindowManager == null) {
			mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		}
		return mWindowManager;
	}

	/**
	 * 如果ActivityManager还未创建，则创建一个新的ActivityManager返回。否则返回当前已创建的ActivityManager。
	 * 
	 * @param context
	 *            可传入应用程序上下文。
	 * @return ActivityManager的实例，用于获取手机可用内存。
	 */
	private static ActivityManager getActivityManager(Context context) {
		if (mActivityManager == null) {
			mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		}
		return mActivityManager;
	}

	/**
	 * 计算已使用内存的百分比，并返回。
	 * 
	 * @param context
	 *            可传入应用程序上下文。
	 * @return 已使用内存的百分比，以字符串形式返回。
	 */
	public static String getUsedPercentValue(Context context) {
		String dir = "/proc/meminfo";
		try {
			FileReader fr = new FileReader(dir);
			BufferedReader br = new BufferedReader(fr, 2048);
			String memoryLine = br.readLine();
			String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
			br.close();
			long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));
			long availableSize = getAvailableMemory(context) / 1024;
			int percent = (int) ((totalMemorySize - availableSize) / (float) totalMemorySize * 100);
			return percent + "%";
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "悬浮窗";
	}

	/**
	 * 获取当前可用内存，返回数据以字节为单位。
	 * 
	 * @param context
	 *            可传入应用程序上下文。
	 * @return 当前可用内存。
	 */
	private static long getAvailableMemory(Context context) {
		ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
		getActivityManager(context).getMemoryInfo(mi);
		return mi.availMem;
	}

}
