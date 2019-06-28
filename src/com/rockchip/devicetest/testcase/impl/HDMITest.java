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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.VideoView;

import com.rockchip.devicetest.ConfigFinder;
import com.rockchip.devicetest.R;
import com.rockchip.devicetest.VideoActivity;
import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.constants.ResourceConstants;
import com.rockchip.devicetest.enumerate.AgingType;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.IniEditor;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.SystemBinUtils;

public class HDMITest extends BaseTestCase {

	private static final String DISPLAY_FILE_OLD = "/sys/class/display";
	private static final String ENABLED_OLD = "1";
	private static final String DISABLED_OLD = "0";

	private static final String DISPLAY_FILE = "/sys/devices/platform/display-subsystem/drm/card0/card0-HDMI-A-1/enabled";
	private static final String ENABLED = "enabled";
	private static final String DISABLED = "disabled";

	private VideoView mVideoView;

	private File testVideo;
	public static String videoPath;

	private static final String TAG = "HDMITest";
	private static final boolean DEBUG = true;

	private void LOGV(String msg) {
		if (DEBUG)
			Log.i(TAG, msg);
	}

	public HDMITest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
	}

	@Override
	public boolean onTesting() {
		LOGV("----------onTesting----------------");
		File hdmiFile = getHdmiDisplay();
		if (!hdmiFile.exists()) {
			onTestFail(R.string.pub_test_no_exist);
			return super.onTesting();
		}

		if (mTestCaseInfo == null || mTestCaseInfo.getAttachParams() == null) {
			onTestFail(R.string.vpu_err_attach_params);
			return false;
		}
		testVideo = getVideoFile();
		// File testVideo = getTestVideoFile();
		if (testVideo == null || !testVideo.exists()) {
			onTestFail(R.string.vpu_err_video);
			return false;
		}

		String enablestr = FileUtils.readFromFile(hdmiFile);
		Log.i("AZ", "[HDMI]enablestr=" + enablestr);
		if (enablestr != null && !ENABLED.equals(enablestr)) {
			setEnabled(hdmiFile, true);
		}
		if (isHasPlayer()) {
			useSystemPlayer();
		} else {
			// useVideoView(); //弹对话框选择是否全屏测试
			useVideoView1(); // 直接进行全屏测试
		}
		return true;
	}

	/**
	 * 获取HDMI enable节点
	 * 
	 * @return
	 */
	private File getHdmiDisplay() {
		// File hdmiFile = new File(DISPLAY_FILE + "/HDMI", "enable");
		File hdmiFile = new File(DISPLAY_FILE);
		// SystemBinUtils.chmod("666", hdmiFile.getAbsolutePath());
		return hdmiFile;
		/*
		 * File displayFile = new File(DISPLAY_FILE); File[] files =
		 * displayFile.listFiles(); if(files==null) return null;
		 * 
		 * for(File item : files){ if(item.getName().endsWith("HDMI")){ return
		 * new File(item, "enable"); } } return null;
		 */
	}

	public File getVideoFile() {
		Map<String, String> attachParams = mTestCaseInfo.getAttachParams();
		String fileName = attachParams.get(ParamConstants.VIDEO_TEST);
		// isHasVideoFile(fileName);
		// File file = new File(findFilePath);
		File file = new File(
				FileUtils.findFileByPartialName(fileName, mContext));
		LOGV("filename-->" + file.getPath());
		return file;
	}

	/**
	 * 从agingconfig中获取测试片源文件路径
	 * 
	 * @param file
	 * @param enabled
	 * @return
	 */
	public File getTestVideoFile() {
		InputStream in = null;
		try {
			in = mContext.getAssets().open(ResourceConstants.AGING_CONFIG_FILE);
			IniEditor agingConfig = new IniEditor();
			agingConfig.load(in);
			String mediaFile = agingConfig.get(AgingType.VPU.getType(),
					"testvideo");
			return ConfigFinder.findConfigFile(mediaFile, mContext);
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.e(mContext, "Read aging test config failed");
			return null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public boolean setEnabled(File file, boolean enabled) {
		return FileUtils.write2File(file, enabled ? ENABLED : DISABLED);
	}

	// 使用系统播放器进行视频测试
	private void useSystemPlayer() {
		LOGV("testVidep path--->" + testVideo.getPath());
		AlertDialog dialogTest = null;
		AlertDialog.Builder mBuilder = new AlertDialog.Builder(mContext);
		mBuilder.setTitle(R.string.hdmi_title).setMessage(R.string.hdmi_msg);
		mBuilder.setPositiveButton(R.string.pub_success, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				onTestSuccess();
			}
		});
		mBuilder.setNegativeButton(R.string.pub_fail, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				onTestFail(0);
			}
		});
		mBuilder.setCancelable(false);
		dialogTest = mBuilder.create();
		// 在dialog show后再启动视频播放，以确保视频播放在后！
		dialogTest.setOnShowListener(new OnShowListener() {

			@Override
			public void onShow(DialogInterface arg0) {
				// TODO Auto-generated method stub
				startVideo();
			}
		});
		dialogTest.show();
		dialogTest.getWindow().getDecorView()
				.findViewById(android.R.id.button1).requestFocus();
	}

	private void startVideo() {
		// File file1 = new File("/system/app/RkVideoPlayer.apk");
		// File file2 = new File("/system/app/Gallery2.apk");

		Intent intent1 = mContext.getPackageManager()
				.getLaunchIntentForPackage("android.rk.RockVideoPlayer");
		// Intent intent2 = mContext.getPackageManager()
		// .getLaunchIntentForPackage("com.android.gallery3d");
		intent1.setClassName("android.rk.RockVideoPlayer",
				"android.rk.RockVideoPlayer.VideoPlayActivity");
		if (intent1 != null && testVideo != null) {
			intent1.setDataAndType(Uri.fromFile(testVideo), "video/mp4");
			mContext.startActivity(intent1);
		} /*
		 * else if (intent2 != null && testVideo != null) {
		 * intent2.setDataAndType(Uri.fromFile(testVideo), "video/mp4");
		 * mContext.startActivity(intent2); }
		 */else {
			useVideoView1();
		}
	}

	private void useVideoView() {
		mVideoView = new VideoView(mContext);
		int winHeight = mContext.getResources().getDisplayMetrics().heightPixels;
		int winWidth = mContext.getResources().getDisplayMetrics().widthPixels;
		// mVideoView.setMinimumHeight((int) (winHeight * 0.7));
		// mVideoView.setMinimumWidth((int) (winHeight * 0.7 * 1.5));
		mVideoView.setMinimumHeight(winHeight);
		mVideoView.setMinimumWidth(winWidth);
		mVideoView.setFocusable(false);

		AlertDialog dialog = null;
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.hdmi_title);
		builder.setMessage(R.string.hdmi_msg);
		builder.setView(mVideoView);
		builder.setPositiveButton(mContext.getString(R.string.pub_success),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mVideoView.stopPlayback();
						onTestSuccess();
					}
				});
		builder.setNegativeButton(mContext.getString(R.string.pub_fail),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mVideoView.stopPlayback();
						onTestFail(0);
					}
				});
		builder.setNeutralButton(R.string.screen_test, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, VideoActivity.class);
				Activity act = ((Activity) mContext);
				act.startActivityForResult(intent, 1024);
			}
		});
		builder.setCancelable(false);
		dialog = builder.create();
		dialog.show();
		// 获得默认焦点
		dialog.getWindow().getDecorView().findViewById(android.R.id.button3)
				.requestFocus();

		videoPath = testVideo.getAbsolutePath();
		mVideoView.setVideoPath(videoPath);
		mVideoView.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer mp) {
				mp.start();
			}
		});
		mVideoView.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				if (isTesting()) {
					mVideoView.setVideoPath(videoPath);
				}
			}
		});
		mVideoView.setOnErrorListener(new OnErrorListener() {
			public boolean onError(MediaPlayer mp, int what, int extra) {
				onTestFail(R.string.vpu_err_play);
				return true;
			}
		});
	}

	private void useVideoView1() {
		// HDMI测试直接全屏
		videoPath = testVideo.getAbsolutePath();
		Intent intent = new Intent(mContext, VideoActivity.class);
		Activity act = ((Activity) mContext);
		act.startActivityForResult(intent, 1024);
	}

	private boolean isHasPlayer() {
		boolean hasPlayer = false;
		String packageName1 = "";
		String packageName2 = "";
		String packageName3 = "";
		String className1 = "";
		String className2 = "";
		String className3 = "";
		try {
			File file1 = new File("/system/app/RkVideoPlayer.apk");
			File file2 = new File("/data/app/xbmc-rk3288-265.apk");
			File file3 = new File("/system/app/Gallery2/Gallery2.apk");
			LOGV("system player--->" + file1.exists());
			LOGV("system player--->" + file2.exists());
			LOGV("system player--->" + file3.exists());
			PackageManager pm = mContext.getPackageManager();
			PackageInfo info1 = pm.getPackageArchiveInfo(file1.getPath(),
					PackageManager.GET_ACTIVITIES);
			if (info1 != null) {
				ApplicationInfo appInfo1 = info1.applicationInfo;
				packageName1 = appInfo1.packageName; // 得到安装包名称
				className1 = appInfo1.className;
				LOGV("--->" + packageName1 + "--" + className1);
			}
			PackageInfo info2 = pm.getPackageArchiveInfo(file2.getPath(),
					PackageManager.GET_ACTIVITIES);
			if (info2 != null) {
				ApplicationInfo appInfo2 = info2.applicationInfo;
				packageName2 = appInfo2.packageName; // 得到安装包名称
				className2 = appInfo2.className;
				LOGV("className2--->" + packageName2 + "--" + className2);
			}
			PackageInfo info3 = pm.getPackageArchiveInfo(file3.getPath(),
					PackageManager.GET_ACTIVITIES);
			if (info3 != null) {
				ApplicationInfo appInfo3 = info3.applicationInfo;
				packageName3 = appInfo3.packageName; // 得到安装包名称
				className3 = appInfo3.className;
				LOGV("className2--->" + packageName3 + "--" + className3);
			}
			if (file1.exists() || file2.exists() || file3.exists()) {
				hasPlayer = true;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return hasPlayer;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (1024 == requestCode) {
			if (resultCode == 111) {
				onTestSuccess();
			} else {
				onTestFail(0);
			}
		}
	}
}
