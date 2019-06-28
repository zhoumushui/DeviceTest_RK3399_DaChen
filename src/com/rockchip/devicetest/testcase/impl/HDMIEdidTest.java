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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;

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
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;


public class HDMIEdidTest extends BaseTestCase {

	private static final String TAG = "HDMIEdidTest";
	private static final boolean DEBUG = true;
	private static final String HDMI_EDID_FILE = "/sys/class/display/HDMI/edidread";
	private File HdmiEdid = null;

	private void LOGV(String msg) {
		if (DEBUG)
			Log.i(TAG, msg);
	}

	public HDMIEdidTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
	}

	@Override
	public boolean onTesting() {
		LOGV("----------onTesting----------------");
        HdmiEdid = new File(HDMI_EDID_FILE);
        if(isHdmiEdid(HdmiEdid))
        {
           onTestSuccess("HDMI EDID success");
        }
        else
        {
            onTestFail("HDMI EDID fail");
        }

		return true;
	}

	
	public static boolean isHdmiEdid(File file) {
		boolean isEdid = false;
		if (file.exists()) {
			try {
				FileReader fread = new FileReader(file);
				BufferedReader buffer = new BufferedReader(fread);
				String strPlug = "plug=1";
				String str = null;

				while ((str = buffer.readLine()) != null) {
					int length = str.length();
					if (str.equals("1")) {
						isEdid = true;
						break;
					}					
				}
			} catch (IOException e) {
				Log.e(TAG, "IO Exception");
			}
		}
		return isEdid;
	}


	@Override
	public boolean onTestHandled(TestResult result) {
		return super.onTestHandled(result);
	}
	
}
