package com.rockchip.devicetest.testcase.impl;

import java.util.Map;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.networktest.BaseNetworkManager;
import com.rockchip.devicetest.networktest.BaseNetworkManager.onTestListener;
import com.rockchip.devicetest.networktest.LanNetworkManager;
import com.rockchip.devicetest.networktest.LanTestInfo;
import com.rockchip.devicetest.networktest.ResultInfo;
import com.rockchip.devicetest.networktest.WifiNetworkManager;
import com.rockchip.devicetest.networktest.WifiTestInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.StringUtils;

public class BaseNetWorkTest extends BaseTestCase implements onTestListener{

	/***
	 * 目前支持的网络
	 */
	public static final int NETWORK_TYPE_LAN = 0;
	public static final int NETWORK_TYPE_WIFI = 1;

	public int mNetWorkType = NETWORK_TYPE_LAN;//测试的网络类型

	private boolean mTestIperf;//是否测试Iperf
	private boolean mTestSmb;//是否测试smb
	private boolean mTestPing;//是否测试Ping

	//如果测试网络为Lan，还需要一些特殊的参数
	private LanTestInfo mLanTestInfo;

	//如果测试网络为WIFI，还需要一些特殊的参数
	private WifiTestInfo mWifiTestInfo;

	//如果需要测试smb
	private static final String DEFAULT_SMB_PATH = "smb://192.168.1.1/USB_Storage/test.mp4";
	private String mSmbPath = DEFAULT_SMB_PATH;

	private String mIperfServerIP;
	private String mIperfServerPort;
	
	private static final String DEFAULT_PACKAGE_SIZE ="65500";
	private String mPingPackageSize  ;


	BaseNetworkManager mNetworkManager ;

	public ResultInfo mConnectResultInfo = new ResultInfo();
	public ResultInfo mSmbResultInfo = new ResultInfo();
	public ResultInfo mIperfResultInfo = new ResultInfo();
	public ResultInfo mPingResultInfo = new ResultInfo();

	public BaseNetWorkTest(Context context, Handler handler,
			TestCaseInfo testcase) {
		super(context, handler, testcase);
	}

	@Override
	public boolean onTesting() {
		// TODO Auto-generated method stub
		Map<String, String> attachParams = mTestCaseInfo.getAttachParams();
		mNetWorkType = StringUtils.parseInt(
				attachParams.get(ParamConstants.NETWORK_TYPE), NETWORK_TYPE_LAN);//default is lan

		mTestIperf = StringUtils.parseBoolean(
				attachParams.get(ParamConstants.NETWORK_TEST_IPERF), true);//default test iperf
		mTestSmb = StringUtils.parseBoolean(
				attachParams.get(ParamConstants.NETWORK_TEST_SMB), true);//default test smb
		mTestPing = StringUtils.parseBoolean(
				attachParams.get(ParamConstants.NETWORK_TEST_PING), true);//default test ping
		
		mSmbPath = StringUtils.getStringValue( 
				attachParams.get(ParamConstants.SMB_PATH), DEFAULT_SMB_PATH);

		mIperfServerIP = attachParams.get(ParamConstants.SERVER_IP);
		mIperfServerPort =attachParams.get(ParamConstants.SERVER_PORT);
		
		mPingPackageSize = StringUtils.getStringValue( 
				attachParams.get(ParamConstants.PACKAGE_SIZE), DEFAULT_PACKAGE_SIZE);

		//mTestIperf = true;
		//如果测试的为wifi网络
		Log.d("sjf", "NetworkType:"+mNetWorkType+" ,mTestSmb:"+mTestSmb+",mTestIperf:"+mTestIperf+",mTestPing:"+mTestPing);
		if(mNetWorkType == NETWORK_TYPE_WIFI)
		{
			mWifiTestInfo = new WifiTestInfo();
			mWifiTestInfo.setSpecifiedAp(attachParams.get(ParamConstants.WIFI_AP));
			mWifiTestInfo.setPassword(attachParams.get(ParamConstants.WIFI_PSW));
			mWifiTestInfo.setStartSignalLevel(StringUtils.parseInt(
					attachParams.get(ParamConstants.WIFI_DB_START), 999));
			mWifiTestInfo.setEndSignalLevel(StringUtils.parseInt(
					attachParams.get(ParamConstants.WIFI_DB_END), 999));
			mWifiTestInfo.setNeedConnectAp(StringUtils.parseBoolean(
					attachParams.get(ParamConstants.WIFI_CONNECT), true));

			if (StringUtils.isEmptyObj(mWifiTestInfo.getSpecifiedAp())) {
				onTestFail(R.string.wifi_err_ap_not_specified);
				return false;
			}

			if (mWifiTestInfo.getStartSignalLevel() == 999 || mWifiTestInfo.getEndSignalLevel() == 999
					|| mWifiTestInfo.getStartSignalLevel() < mWifiTestInfo.getEndSignalLevel()) {// [-30]----[-70]
				onTestFail(R.string.wifi_err_cmd_params);
				return false;
			}
			mNetworkManager = new WifiNetworkManager(mContext,mWifiTestInfo);
		}else if(mNetWorkType ==  NETWORK_TYPE_LAN )
		{
			String mConnectMode = attachParams.get(ParamConstants.CONNECT_MODE);
			String mEthIpAddress = attachParams.get(ParamConstants.STATIC_IP);
			String mEthGateway = attachParams.get(ParamConstants.GATEWAY);
			String mEthNetmask = attachParams.get(ParamConstants.NETMASK);
			String mEthdns1 = attachParams.get(ParamConstants.DNS1);
			String mEthdns2 = attachParams.get(ParamConstants.DNS2);
			boolean mIsSpeed1000 = StringUtils.parseBoolean(
					attachParams.get(ParamConstants.IS_SPEED_1000), true);
			mLanTestInfo = new LanTestInfo(mConnectMode, mEthIpAddress, mEthNetmask, mEthGateway, mEthdns1, mEthdns2, mIsSpeed1000);
			mNetworkManager = new LanNetworkManager(mContext,mLanTestInfo);
		}

		mNetworkManager.setOnTestListener(this);
		mNetworkManager.startConnectTest();

		return super.onTesting();
	}

	@Override
	public boolean onTestHandled(TestResult result) {
		// TODO Auto-generated method stub
		mNetworkManager.unRegisterReceiver();
		return super.onTestHandled(result);
	}

	@Override
	public void onConnectTestEnd(ResultInfo info) {
		// TODO Auto-generated method stub
		mConnectResultInfo = info;
		
		if(info.isSuccess())
		{
			if(mTestSmb)
			{
				mNetworkManager.startTestSmb(mSmbPath);
			}else if(mTestIperf)
			{
				mNetworkManager.startTestIperf(mIperfServerIP,mIperfServerPort);
			}else if(mTestPing)
			{
				mNetworkManager.startTestPing(mIperfServerIP, mPingPackageSize);
			}else{
				onTestSuccess(info.getResult());
			}
		}else{
			onTestFail(info.getResult());
		}

	}

	@Override
	public void onSmbTestEnd(ResultInfo info) {
		// TODO Auto-generated method stub
		mSmbResultInfo = info;
		mNetworkManager.unRegisterSmbReceiver();
		if(mTestIperf)
		{
			mNetworkManager.startTestIperf(mIperfServerIP,mIperfServerPort);
		}else if(mTestPing)
		{
			mNetworkManager.startTestPing(mIperfServerIP, mPingPackageSize);
		}else{
			TestDone();
		}
	}

	@Override
	public void onIperfTestEnd(ResultInfo info) {
		// TODO Auto-generated method stub
		mIperfResultInfo = info;
		if(mTestPing)
		{
			mNetworkManager.startTestPing(mIperfServerIP, mPingPackageSize);
		}else{
			TestDone();
		}
	}
	
	@Override
	public void onPingTestEnd(ResultInfo info) {
		// TODO Auto-generated method stub
		mPingResultInfo = info;
		TestDone();
	}
	
	@Override
	public void onUpdateDetail(String detail) {
		// TODO Auto-generated method stub
		updateDetail(detail);
	}


	private void TestDone()
	{
		boolean success = mConnectResultInfo.isSuccess();
		StringBuilder sb = new StringBuilder(mConnectResultInfo.getResult());
		if(mTestSmb || mTestIperf || mTestPing)sb.append("|");

		if(mTestSmb)
		{
			success = success && mSmbResultInfo.isSuccess();
			sb.append("Smb:"+(mSmbResultInfo.isSuccess()?"Success":"Fail"));
			if(!TextUtils.isEmpty(mSmbResultInfo.getResult()))sb.append(","+mSmbResultInfo.getResult());
			if(mTestIperf || mTestPing)sb.append("|");
		}

		if(mTestIperf)
		{
			success = success && mIperfResultInfo.isSuccess();
			sb.append("Iperf:"+(mIperfResultInfo.isSuccess()?"Success":"Fail"));
			if(!TextUtils.isEmpty(mIperfResultInfo.getResult()))sb.append(","+mIperfResultInfo.getResult());
			if(mTestPing)sb.append("|");
		}
		
		if(mTestPing)
		{
			success = success && mPingResultInfo.isSuccess();
			sb.append("Ping:"+(mPingResultInfo.isSuccess()?"Success":"Fail"));//+","+mPingResultInfo.getResult()).append("|");
			if(!TextUtils.isEmpty(mPingResultInfo.getResult()))sb.append(","+mPingResultInfo.getResult());
		}

		if(success) onTestSuccess(sb.toString());
		else onTestFail(sb.toString());

	}




}
