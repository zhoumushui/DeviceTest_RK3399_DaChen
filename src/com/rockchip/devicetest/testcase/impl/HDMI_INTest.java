/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月12日 下午5:41:32  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月12日      fxw         1.0         create
 *******************************************************************/

package com.rockchip.devicetest.testcase.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.VideoView;

import com.rockchip.devicetest.ConfigFinder;
import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.ResourceConstants;
import com.rockchip.devicetest.enumerate.AgingType;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.IniEditor;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.SystemBinUtils;

public class HDMI_INTest extends BaseTestCase {

	private static final String TAG = "HDMI_SWITCH";
	private static final boolean DEBUG = true;
	private static final int SUCCESS = 1;
	private static final int FAIL = 0; // need reset Hdmi after delay
	// delay 2s to check hdmi state,and decide switch is SUCCESS or FAIL
	private static final int NEE_DELAY_CHECK = -1;

	public void LOGD(String msg) {
		if (DEBUG) {
			Log.d(TAG, msg);
		}
	}

	public HDMI_INTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		hdmiHandler = new Handler();
		HdmiState = new File("sys/devices/virtual/switch/hdmi/state");
	}

	/**
	 * HDMI_IN_FILE_STYLE: /sys/lt8641ex/channel： 可读可写 0-3288输出 1-HDMI IN输出
	 */
	private static final String HDMI_IN_FILE_STYLE = "/sys/lt8641ex/channel";
	private static final String HDMI_IN_ON = "1";
	private static final String HDMI_IN_OFF = "0";

	/***
	 * HDMI_IN_FILE_STYLE2: 写c切换通道,写完后检测hdmi连接状态
	 * 
	 */
	private static final String HDMI_IN_FILE_STYLE2 = "/sys/lt8641ex/switch";
	private static final String HDMI_IN_SWITCH = "c";

	private Runnable onRunnable;
	private Runnable offRunnable;
	private Runnable checkRunnable;
	private Runnable successRunnable;
	private static final int delayMillis = 15000;// 延时2s

	private Handler hdmiHandler;
	// 读取HDMI状态
	private File HdmiState = null;

	// TextView statusTV;

	@Override
	public boolean onTesting() {
		// TODO Auto-generated method stub
		LOGD("onTesting");
		//File style1 = new File(HDMI_IN_FILE_STYLE);
		//File style2 = new File(HDMI_IN_FILE_STYLE2);
		//if (!((style1.exists() && style1.isFile()) || (style2.exists() && style2.isFile()))) {
		//	onTestFail(R.string.pub_test_no_exist);
		//	return super.onTesting();
		//}
		// 关闭cvbs
		File cvbsFile = CVBSTest.getCvbsDisplay();
		CVBSTest.setEnabled(cvbsFile, false);
		String enablestr = FileUtils.readFromFile(cvbsFile);
		Log.v("HDMI_IN", "关闭cvbs--->" + enablestr);
		onRunnable = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub

				int need_reset = switchHdmi(true);
				LOGD("TRUN ON HDMI IN need_reset=" + need_reset);
				switch (need_reset) {
				case SUCCESS:
					LOGD("switch success");
					LOGD("onTestSuccess");
					// switchHdmi(false);
					// onTestSuccess();
					hdmiHandler.postDelayed(checkRunnable, delayMillis);
					// hdmiHandler.sendEmptyMessage(1);
					break;
				case NEE_DELAY_CHECK:
					LOGD("switch need delay to check");
					hdmiHandler.postDelayed(checkRunnable, delayMillis);
					break;
				case FAIL:
				default:
					LOGD("switch fail");
					LOGD("onTestFail");
					switchHdmi(false);
					onTestFail("HDMI IN is not connected");
					// hdmiHandler.sendEmptyMessage(0);

					break;
				}
			}
		};

		checkRunnable = new Runnable() {// just for onRunnable

			@Override
			public void run() {
				// TODO Auto-generated method stub
				boolean isconnect_afer_set = isHdmiConnected(HdmiState);
				LOGD(" delay 2s to check hdmi state,and need reset hdmi is "
						+ isconnect_afer_set);
				if (isconnect_afer_set) {
					LOGD("onTestFail");
					switchHdmi(false);
					onTestFail("HDMI IN is not connected");
				} else {
					LOGD("onTestSuccess");
					switchHdmi(false);
					hdmiHandler.postDelayed(successRunnable, 5000);
				}

			}
		};

		successRunnable = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.hdmi_in_title);
				builder.setMessage(R.string.hdmi_in_msg);
				builder.setPositiveButton(mContext.getString(R.string.pub_success),
						new OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								onTestSuccess();
							}
						});
				builder.setNegativeButton(mContext.getString(R.string.pub_fail),
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								onTestFail("HDMI IN is not connected");
							}
						});
				builder.setCancelable(false);
				AlertDialog dialog = builder.create();
				dialog.show();

			}
		};
		
		offRunnable = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				hdmiHandler.removeCallbacks(checkRunnable);
				hdmiHandler.removeCallbacks(onRunnable);
				switchHdmi(false);
			}
		};
		hdmiHandler.post(onRunnable);
		return true;
	}

	// @Override
	// public void onTestSuccess() {
	// // TODO Auto-generated method stub
	// hdmiHandler.post(offRunnable);
	// Log.i("HDMI_IN", "onTestSuccess");
	// super.onTestSuccess();
	// }
	//
	// @Override
	// public void onTestFail(int errResID) {
	// // TODO Auto-generated method stub
	// hdmiHandler.post(offRunnable);
	// Log.i("HDMI_IN", "onTestFail");
	// super.onTestFail(errResID);
	// }

	public void stop() {
		super.stop();

	}

	// @Override
	// protected void onResume() {
	// // TODO Auto-generated method stub
	// super.onResume();
	// LOGD("onResume");
	// hdmiHandler.post(onRunnable);
	// }

	/**
	 * 
	 * @param on
	 *            true is trun on hdmi_in ,false is trun off
	 * @return when return false,mean need reset Hdmi after delay
	 */
	private int switchHdmi(boolean on) {
		File style1 = new File(HDMI_IN_FILE_STYLE);
		File style2 = new File(HDMI_IN_FILE_STYLE2);
		if (style1.exists() && style1.isFile()) {
			boolean isconnect_before_set = isHdmiConnected(HdmiState);
			LOGD("channel isconnect_before_set = " + isconnect_before_set
					+ "   ,on=" + on);
			if (on) {
				if (isconnect_before_set)// turn on HDMI IN && HDMI CONNECTED==
				// true,after set HDMI_CONNECTED =
				// false
				{
					setConfigInt(HDMI_IN_FILE_STYLE, HDMI_IN_ON);
					boolean isconnect_afer_set = isHdmiConnected(HdmiState);
					LOGD("channel isconnect_afer_set = " + isconnect_afer_set);
					return (!isconnect_afer_set) ? SUCCESS : NEE_DELAY_CHECK;
				} else {
					return FAIL;
				}
			} else {
				// 当要关闭HDMI IN时，若此时hdmi已连接，则不进行操作
				if (isconnect_before_set) {
					return SUCCESS;
				} else {
					setConfigInt(HDMI_IN_FILE_STYLE, HDMI_IN_OFF);
					boolean isconnect_afer_set = isHdmiConnected(HdmiState);
					return isconnect_afer_set ? SUCCESS : NEE_DELAY_CHECK;
				}
			}
		} else if (style2.exists() && style2.isFile()) {
			boolean isconnect_before_set = isHdmiConnected(HdmiState);
			LOGD("hdmi-sw isconnect_before_set = " + isconnect_before_set
					+ "   ,on=" + on);
			if (on) {
				if (isconnect_before_set)// turn on HDMI IN && HDMI CONNECTED==
				// true,after set HDMI_CONNECTED =
				// false
				{
					setConfigInt(HDMI_IN_FILE_STYLE2, HDMI_IN_SWITCH);
					boolean isconnect_afer_set = isHdmiConnected(HdmiState);
					return !isconnect_afer_set ? SUCCESS : NEE_DELAY_CHECK;
				} else {
					return FAIL;
				}
			} else {
				// 当要关闭HDMI IN时，若此时hdmi已连接，则不进行操作
				if (isconnect_before_set) {
					return SUCCESS;
				} else {
					setConfigInt(HDMI_IN_FILE_STYLE2, HDMI_IN_SWITCH);
					boolean isconnect_afer_set = isHdmiConnected(HdmiState);
					return isconnect_afer_set ? SUCCESS : NEE_DELAY_CHECK;
				}
			}

		}
		return FAIL;
	}

	String getConfigInt(String filepath) {

		String config = "0";
		File _f = new File(filepath);
		if (_f.exists()) {
			try {
				byte[] buf = new byte[10];
				int len = 0;
				RandomAccessFile rdf = new RandomAccessFile(_f, "r");
				len = rdf.read(buf);
				String str = new String(buf, 0, 1);
				config = str;
			} catch (IOException re) {
				LOGD("IO Exception");
			} catch (NumberFormatException re) {
				LOGD("NumberFormatException");
			}
		}
		return config;
	}

	void setConfigInt(String filepath, String value) {
		LOGD("setConfigInt filepath=" + filepath + "   ,value=" + value);
		File _f = new File(filepath);
		if (!_f.exists()) {
			LOGD("setConfigInt  " + filepath + " is not exists");
		} else {
			try {
				FileWriter fr = new FileWriter(_f);
				fr.write(value);
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		_f = null;
	}

	public static boolean isHdmiConnected(File file) {
		boolean isConnected = false;
		if (file.exists()) {
			try {
				FileReader fread = new FileReader(file);
				BufferedReader buffer = new BufferedReader(fread);
				String strPlug = "plug=1";
				String str = null;

				while ((str = buffer.readLine()) != null) {
					int length = str.length();
					// if((length == 6) && (str.equals(strPlug))){
					if (str.equals("1")) {
						isConnected = true;
						break;
					} else {
						// isConnected = false;
					}
				}
			} catch (IOException e) {
				Log.e(TAG, "IO Exception");
			}
		}
		return isConnected;
	}
}
