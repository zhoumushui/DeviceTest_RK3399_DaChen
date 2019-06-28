/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月12日 下午6:14:57  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月12日      fxw         1.0         create
 *******************************************************************/

package com.rockchip.devicetest.testcase.impl;

import java.io.File;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.SystemProperties;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.TimerUtil;

public class CVBSTest extends BaseTestCase {

	private static final String DISPLAY_FILE = "/sys/class/display";
	private static final String ENABLED = "1";
	private static final String DISABLED = "0";

	private static final String TAG = "CVBSTest";

	public CVBSTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
	}

	@Override
	public boolean onTesting() {
		File cvbsFile = getCvbsDisplay();
		if(!cvbsFile.exists()) {
			onTestFail(R.string.pub_test_no_exist);
			return super.onTesting();
		}
		
		SystemProperties.set("tchip.devicetest.flag", ENABLED);
                
		setEnabled(getHdmiDisplay(), false);
		TimerUtil.wait(500);// Delay

		String enablestr = FileUtils.readFromFile(cvbsFile);
		LogUtil.v(CVBSTest.this, "file getCvbsDisplay before--->" + enablestr);
		if (enablestr != null && !ENABLED.equals(enablestr)) {
			setEnabled(cvbsFile, true);
		}
		LogUtil.v(CVBSTest.this,
				"file getCvbsDisplay after--->" + FileUtils.readFromFile(cvbsFile));
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.cvbs_title);
		builder.setMessage(R.string.cvbs_msg);
		builder.setPositiveButton(mContext.getString(R.string.pub_success),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						SystemProperties.set("tchip.devicetest.flag", DISABLED);
						setEnabled(getCvbsDisplay(), false);
						TimerUtil.wait(500);
						setEnabled(getHdmiDisplay(), true);
						onTestSuccess();
						// 先关再开
					}
				});
		builder.setNegativeButton(mContext.getString(R.string.pub_fail),
				new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						SystemProperties.set("tchip.devicetest.flag", DISABLED);
						setEnabled(getCvbsDisplay(), false);
						setEnabled(getHdmiDisplay(), true);
						onTestFail(0);
					}
				});
		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();
		return true;
	}

	/**
	 * 获取CVBS enable节点
	 * 
	 * @return
	 */
	public static File getCvbsDisplay() {
		File tvFile = new File(DISPLAY_FILE + "/display0.TV", "enable");
		//兼容3066机型
		File tvFile1 = new File(DISPLAY_FILE + "/display1.TV", "enable");
		//兼容3128
		File tvFile2 = new File(DISPLAY_FILE + "/TV", "enable");
		if(tvFile.exists()) {
			return tvFile;
		} else if(tvFile1.exists()){
		// SystemBinUtils.chmod("666", tvFile.getAbsolutePath());
			return tvFile1;
		} else {
			return tvFile2;
		}
		/*
		 * File displayFile = new File(DISPLAY_FILE); File[] files =
		 * displayFile.listFiles(); if(files==null) return null;
		 * 
		 * for(File item : files){ if(item.getName().endsWith("TV")){ return new
		 * File(item, "enable"); } } return null;
		 */
	}

	public static File getHdmiDisplay() {
		File hdmiFile = new File(DISPLAY_FILE + "/display0.HDMI", "enable");
		// SystemBinUtils.chmod("666", hdmiFile.getAbsolutePath());
		//兼容3128
		File hdmiFile1 = new File(DISPLAY_FILE + "/HDMI" , "enable");
		if (hdmiFile.exists()){
			return hdmiFile;
		} else{
			return hdmiFile1;
		}
		/*
		 * File displayFile = new File(DISPLAY_FILE); File[] files =
		 * displayFile.listFiles(); if(files==null) return null;
		 * 
		 * for(File item : files){ if(item.getName().endsWith("HDMI")){ return
		 * new File(item, "enable"); } } return null;
		 */
	}

	public static boolean setEnabled(File file, boolean enabled) {
		if (file == null)
			return false;
		return FileUtils.write2File(file, enabled ? ENABLED : DISABLED);
	}
}
