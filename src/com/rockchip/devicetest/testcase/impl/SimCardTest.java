package com.rockchip.devicetest.testcase.impl;

import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.telephony.CellInfo;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.StringUtils;
import com.rockchip.devicetest.R;

public class SimCardTest extends BaseTestCase{

	MyPhoneStateListener    mPhoneStateListener;
	TelephonyManager mTelephonyManager;
	public SimCardTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		// TODO Auto-generated constructor stub
		mPhoneStateListener = new MyPhoneStateListener();
		mTelephonyManager = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);

	}
	String mImsi = null;
	private static final int DEFAULT_SIGNAL_LEVEL = 3;
	int signalLevel = DEFAULT_SIGNAL_LEVEL;
	@Override
	public boolean onTesting() {
		// TODO Auto-generated method stub
		Map<String, String> attachParams = mTestCaseInfo.getAttachParams();
		signalLevel = StringUtils.parseInt(
				attachParams.get(ParamConstants.SIGNAL_LEVEL),
				DEFAULT_SIGNAL_LEVEL);
		
		 LogUtil.d(SimCardTest.this, "checkSignalLevel:"+signalLevel);
		mImsi = mTelephonyManager.getSubscriberId();
		 if (mImsi != null)
		{
			 registerListener();
			 LogUtil.d(SimCardTest.this, "imsi:"+mImsi);
		 }else{
			 LogUtil.d(SimCardTest.this, "imsi:"+mImsi);
			 onTestFail(R.string.simcard_err_null_imsi);
		 }
		    
		return super.onTesting();
	}
	
	

    @Override
	public boolean onTestHandled(TestResult result) {
		// TODO Auto-generated method stub
    	unregisterListener();
		return super.onTestHandled(result);
	}

    private String getOperators(String imsi)
    {
    	if(imsi == null)
    	{
    		return "未知";
    	}else if(imsi.startsWith("46000") || imsi.startsWith("46002")|| imsi.startsWith("46007"))
    	{
    		return "中国移动";
    	}else if(imsi.startsWith("46001")||imsi.startsWith("46006")){
    		return "中国联通";
    	}else if(imsi.startsWith("46003")||imsi.startsWith("46005")||imsi.startsWith("46011")){
    		return "中国电信";
    	}else{
    		return "未知";
    	}
    }
    
	public void registerListener() {
    	mTelephonyManager.listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    public void unregisterListener() {
    	mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }
	private class MyPhoneStateListener extends PhoneStateListener {

		@SuppressLint("NewApi")
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			// TODO Auto-generated method stub
			super.onSignalStrengthsChanged(signalStrength);
			
			  LogUtil.d(SimCardTest.this, "onSignalStrengthsChanged signalStrength=" + signalStrength +
                      ((signalStrength == null) ? "" : (" level=" + signalStrength.getLevel())));
			  
			 if(signalStrength == null)
			 {
				 onTestFail(mContext.getResources().getString(R.string.simcard_err_null_signalstrength,mImsi,getOperators(mImsi)));
			 }else if( signalStrength.getLevel() < signalLevel)
			 {
				 onTestFail(mContext.getResources().getString(R.string.simcard_err_insufficient_signalstrength,
						 mImsi,
						 getOperators(mImsi),
						 signalStrength.isGsm()?"gsm|lte" : "cdma",
						signalStrength.getLevel(),
						signalLevel));

			 }else{
				 onTestSuccess(mContext.getResources().getString(R.string.simcard_test_result,
						 mImsi,
						 getOperators(mImsi),
						 signalStrength.isGsm()?"gsm|lte" : "cdma",
						signalStrength.getLevel()));
			 }
		} 
		
	}
}
