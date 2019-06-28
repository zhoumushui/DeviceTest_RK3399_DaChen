package com.rockchip.devicetest.testcase.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.util.Log;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.TimerUtil;

public class VGATest extends BaseTestCase {

	private static final String TAG = "VGATest";
	private static final boolean DEBUG = true;
	private static final String DISPLAY_FILE = "/sys/class/display";
	private static final String ENABLED = "1";
	private static final String DISABLED = "0";

	private AlertDialog dialog = null;

	private void LOGV(String msg) {
		if (DEBUG)
			Log.v(TAG, msg);
	}

	public VGATest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onTesting() {
		// TODO Auto-generated method stub
		final File displayVGA = new File(DISPLAY_FILE + "/VGA/enable");
		final File modes = new File(DISPLAY_FILE + "/VGA//modes");
		final File mode = new File(DISPLAY_FILE + "/VGA/mode");

		final File displayHDMI = new File(DISPLAY_FILE
				+ "/HDMI/enable");
		// setEnabled(displayHDMI, false);
		// TimerUtil.wait(2000);
		LOGV("has enable file--->" + displayVGA.exists());
		LOGV("has modes file--->" + modes.exists());
		LOGV("has mode file--->" + mode.exists());
		LOGV("has displayHDMI file--->" + displayHDMI.exists());
		if(!(displayVGA.exists() && modes.exists() &&  mode.exists())) {
			onTestFail(R.string.pub_test_no_exist);
			return super.onTesting();
		}
		String enablestr = FileUtils.readFromFile(displayVGA);
		LOGV("VGAenable--->" + enablestr);
		// if (enablestr != null && !ENABLED.equals(enablestr)) {
		// 	setEnabled(displayVGA, true);
		// 	LOGV("VGAenable--->" + FileUtils.readFromFile(displayVGA));
		// }
		List<String> data = readFile(modes);
		final String[] items = new String[data.size()];
		for (int i = 0; i < data.size(); i++) {
			items[i] = data.get(i);
			LOGV("items--->" + items[i]);
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(mContext.getString(R.string.vga_title));
		builder.setSingleChoiceItems(items, -1, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				LOGV("items" + "[" + which + "]--->" + items[which]);
				FileUtils.write2File(mode, items[which]);
				setEnabled(displayHDMI, false);
				TimerUtil.wait(1000);
				setEnabled(displayVGA, true);						
				TimerUtil.wait(2000);
				LOGV("VGA--->" + FileUtils.readFromFile(displayVGA));
				LOGV("hdmi--->" + FileUtils.readFromFile(displayHDMI));
				LOGV("mode--->" + FileUtils.readFromFile(mode));
			}
		});

		builder.setPositiveButton(mContext.getString(R.string.pub_success),
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
						
						setEnabled(displayHDMI, true);
						TimerUtil.wait(2000);
						LOGV("VGA--->" + FileUtils.readFromFile(displayVGA));
						LOGV("hdmi--->" + FileUtils.readFromFile(displayHDMI));
						onTestSuccess(mContext
								.getString(R.string.vga_resolution)
								+ FileUtils.readFromFile(mode));
					}
				});
		builder.setNegativeButton(mContext.getString(R.string.pub_fail),
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
						
						setEnabled(displayHDMI, true);
						TimerUtil.wait(2000);
						LOGV("VGA--->" + FileUtils.readFromFile(displayVGA));
						LOGV("hdmi--->" + FileUtils.readFromFile(displayHDMI));
						onTestFail(0);
					}
				});
		builder.setCancelable(false);
		dialog = builder.create();
		dialog.show();
		return super.onTesting();
	}

	// 读取modes 分辨率列表
	private List<String> readFile(File file) {
		List<String> data = new ArrayList<String>();
		String temp = "";
		if ((file != null) && file.exists()) {
			try {
				FileInputStream fin = new FileInputStream(file);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(fin));
				while ((temp = reader.readLine()) != null) {
					data.add(temp);
				}
				fin.close();
				return data;
			} catch (IOException e) {
				return null;
			}
		}
		return null;
	}

	private boolean setEnabled(File file, boolean enabled) {
		if (file == null)
			return false;
		return FileUtils.write2File(file, enabled ? ENABLED : DISABLED);
	}
}
