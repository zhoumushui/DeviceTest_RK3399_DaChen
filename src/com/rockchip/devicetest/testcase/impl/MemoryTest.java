/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月13日 上午9:27:11  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月13日      fxw         1.0         create
 *******************************************************************/   

package com.rockchip.devicetest.testcase.impl;

import java.util.HashMap;
import java.util.Map;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.ComponentName;
import android.content.Context;


import android.os.Handler;
import android.text.format.Formatter;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.StringUtils;
import com.rockchip.devicetest.utils.SystemInfoUtils;

public class MemoryTest extends BaseTestCase {

	public static final int INDEX_SDCARD = 1;
	private Context mContext;

	public MemoryTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		mContext = context;
	}

	public void onTestInit() {
		super.onTestInit();
	}
	private long ramSize;
	private long flashSize;
	private static final int DEFAULT_RAM_SIZE = 400;
	private static final int DEFAULT_FLASH_SIZE = 3000;
	private static final float DEFAULT_RATIO = 0.8f;

	public boolean onTesting() {
		if (mTestCaseInfo == null || mTestCaseInfo.getAttachParams() == null) {
			onTestFail(R.string.memory_err_attach_params);
			return false;
		}

		// Check specified wifi ap
		Map<String, String> attachParams = mTestCaseInfo.getAttachParams();
		ramSize = StringUtils.parseInt(attachParams.get(ParamConstants.RAM_SIZE), DEFAULT_RAM_SIZE);
		flashSize = StringUtils.parseInt(attachParams.get(ParamConstants.FLASH_SIZE), DEFAULT_FLASH_SIZE);
		long curRamSize = SystemInfoUtils.getRamSpace(mContext);
		long curFlashSize = SystemInfoUtils.getFlashSpace(mContext);
		LogUtil.d(MemoryTest.this,"配置内存="+Formatter.formatFileSize(mContext, ramSize*1024*1024)
					+";配置Flash="+Formatter.formatFileSize(mContext, flashSize*1024*1024));
		LogUtil.d(MemoryTest.this,"实际内存="+Formatter.formatFileSize(mContext, curRamSize*1024*1024)
				+";实际Flash="+Formatter.formatFileSize(mContext, curFlashSize*1024*1024));
		if(curRamSize > ramSize * DEFAULT_RATIO  && curFlashSize > flashSize * DEFAULT_RATIO )
		{
			
			onTestSuccess("配置内存="+Formatter.formatFileSize(mContext, ramSize*1024*1024)
					+";配置Flash="+Formatter.formatFileSize(mContext, flashSize*1024*1024));
		}else{
			onTestFail("配置内存="+Formatter.formatFileSize(mContext, ramSize*1024*1024)
					+";配置Flash="+Formatter.formatFileSize(mContext, flashSize*1024*1024));

		}
		return true;
	}

	@Override
	public boolean onTestHandled(TestResult result) {
		return super.onTestHandled(result);
	}



}
