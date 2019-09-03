package com.rockchip.devicetest.testcase.impl;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.pm.PackageManager;
import android.hardware.Camera.CameraInfo;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.SettingUtil;

public class DualHdmiInTest extends BaseTestCase {
	private AlertDialog mDialog;

	boolean isRunning = false;

	public DualHdmiInTest(Context context, Handler handler,
			TestCaseInfo testcase) {
		super(context, handler, testcase);
	}

	@Override
	public void onTestInit() {
		super.onTestInit();
		isRunning = true;
		new Thread(new SwitchHdmiInThread()).start();
	}

	@Override
	public boolean onTestHandled(TestResult result) {
		return super.onTestHandled(result);
	}

	@Override
	public boolean onTesting() {
		if (mDialog != null) {
			mDialog.show();
			return true;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.dual_hdmi_in_title);
		builder.setMessage(R.string.dual_hdmi_in_msg);
		builder.setPositiveButton(mContext.getString(R.string.pub_success),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						isRunning = false;
						onTestSuccess();
					}
				});
		builder.setNegativeButton(mContext.getString(R.string.pub_fail),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						isRunning = false;
						onTestFail(0);
					}
				});
		builder.setCancelable(false);
		mDialog = builder.create();
		// 在dialog show后再启动Camera，以确保Camera在后open！
		mDialog.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface arg0) {
				LogUtil.d(DualHdmiInTest.this, "start CameraTestActivity");

				startAppbyPackage(mContext, "teaonly.rk.droidipcam");
			}
		});
		mDialog.show();

		return true;
	}

	private static void startAppbyPackage(Context context, String packageName) {
		PackageManager packageManager = context.getPackageManager();
		Intent intent = packageManager.getLaunchIntentForPackage(packageName);
		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}
	}

	File fileHdmiInChannel = new File("/proc/setchannel");

	final Handler autoHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				SettingUtil.SaveFileToNode(fileHdmiInChannel, "1");
				Log.i("AZ", "HDMI-IN > 1");
				break;

			case 2:
				SettingUtil.SaveFileToNode(fileHdmiInChannel, "2");
				Log.i("AZ", "HDMI-IN > 2");
				break;

			default:
				break;
			}
		}
	};

	boolean isOne = true;

	class SwitchHdmiInThread implements Runnable {
		@Override
		public void run() {
			while (isRunning) {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				Message message = new Message();
				message.what = isOne ? 1 : 2;
				autoHandler.sendMessage(message);
				isOne = !isOne;
			}
		}
	}

}
