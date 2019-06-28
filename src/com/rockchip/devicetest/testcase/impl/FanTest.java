package com.rockchip.devicetest.testcase.impl;

import java.io.File;
import java.util.Map;

import twinone.lib.androidtools.shell.Command;
import twinone.lib.androidtools.shell.Shell;

import android.content.Context;
import android.os.Handler;

import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.StringUtils;
import com.rockchip.devicetest.R;

public class FanTest extends BaseTestCase{

//	测试运行时反馈的AD值：
//	cat /sys/bus/iio/devices/iio\:device0/in_voltage3_raw
//	停止的AD参考值：761
//	运行的AD参考值: 119
	
	private static final int DEFAULT_RUN_AD = 119;
	private static final int DEFAULT_STOP_AD = 761;
	
	private int mRunAD = DEFAULT_RUN_AD;
	private int mStopAD = DEFAULT_STOP_AD;
	
	private static final  int DEVIATION = 60;//范围判断的偏差值,即run时>=mRunAD-60 && <= mRunAD+60 则为正常
	private static final String AD_PATH = "/sys/bus/iio/devices/iio\\:device0/in_voltage3_raw";
	
	public FanTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		// TODO Auto-generated constructor stub
		
	}

	@Override
	public boolean onTesting() {
		// TODO Auto-generated method stub
		Map<String, String> attachParams = mTestCaseInfo.getAttachParams();
		mRunAD = StringUtils.parseInt(
				attachParams.get(ParamConstants.FAN_RUN_AD),
				DEFAULT_RUN_AD);
		mStopAD = StringUtils.parseInt(
				attachParams.get(ParamConstants.FAN_STOP_AD),
				DEFAULT_STOP_AD);
		
		int mCurAd = getFanAd();
		LogUtil.d(FanTest.this,"mRunAD:"+mRunAD+" ,mStopAD:"+mStopAD+",mCurAd:"+mCurAd);
		if(mCurAd >=mRunAD-DEVIATION && mCurAd <= mRunAD+DEVIATION)
		{
			onTestSuccess(mContext.getResources().getString(R.string.fan_success_run, mCurAd));
		}/*else if(mCurAd >=mStopAD-DEVIATION && mCurAd <= mStopAD+DEVIATION)
		{
			onTestSuccess(mContext.getResources().getString(R.string.fan_success_stop, mCurAd));
		}*/else{
			onTestFail(mContext.getResources().getString(R.string.fan_error, mCurAd));
		}
		return true;
	}
	private int getFanAd()
	{
		int ad = 0;
		Shell mShell = new Shell();
		Command cat = mShell.execute("cat "+AD_PATH);
		LogUtil.d(FanTest.this, "cat.exitStatus:"+cat.exitStatus);
		if(cat.exitStatus == 0)
		{
			try {
				LogUtil.d(FanTest.this,"str:"+cat.output[0]);
				ad = Integer.parseInt(cat.output[0].trim());
				LogUtil.d(FanTest.this,"ad:"+ad);
			} catch (Exception e) {
				// TODO: handle exception
				LogUtil.d(FanTest.this,"ad parse failed");
			}
			
			
		}
		return ad;
	}
//	private int getFanAd()
//	{
//		File mAdFile = new File(AD_PATH);
//		LogUtil.d(FanTest.this,mAdFile.getAbsolutePath());
//		int ad = 0;
//		LogUtil.d(FanTest.this,"mAdFile.exists():"+mAdFile.exists()+",mAdFile.isFile():"+mAdFile.isFile());
//		if(mAdFile.exists() && mAdFile.isFile())
//		{
//			String str = FileUtils.readFromFile(mAdFile);
//			LogUtil.d(FanTest.this,"str:"+str);
//			if(str != null)
//			{
//				try {
//					ad = Integer.parseInt(str.trim());
//					LogUtil.d(FanTest.this,"ad:"+ad);
//				} catch (Exception e) {
//					// TODO: handle exception
//					LogUtil.d(FanTest.this,"ad parse failed");
//				}
//			}
//		}
//		return ad;
//	}

	
}
