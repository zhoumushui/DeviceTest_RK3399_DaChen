/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月7日 上午11:14:09  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月7日      fxw         1.0         create
 *******************************************************************/

package com.rockchip.devicetest.testcase.impl;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import android.net.ConnectivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.EthernetManager;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.IpConfiguration;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.NetworkUtil;
import com.rockchip.devicetest.utils.StringUtils;
import com.rockchip.devicetest.utils.SystemUtils;

public class WifiTest extends BaseTestCase {
	class TestWifiInfo {
		private String mSpecifiedAp;
		private String mPassword;
		public TestWifiInfo(String mSpecifiedAp, String mPassword) {
			super();
			this.mSpecifiedAp = mSpecifiedAp;
			this.mPassword = mPassword;
		}
		
	}
	class ResultInfo{
		boolean  success = false;
		String info;
		
		public ResultInfo(boolean success, String info) {
			super();
			this.success = success;
			this.info = info;
		}
		
		public ResultInfo() {
			super();
			// TODO Auto-generated constructor stub
		}

		public boolean isSuccess() {
			return success;
		}
		public void setSuccess(boolean success) {
			this.success = success;
		}
		public String getInfo() {
			return info;
		}
		public void setInfo(String info) {
			this.info = info;
		}

		
		
		
		
	}
	// Message
	public static final int MSG_START_SCAN_WIFI = 1;
	public static final int MSG_CHECK_NEXT_WIFI = 2;

	// Combo scans can take 5-6s to complete - set to 10s.
	private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;
	private static final int DEFAULT_WIFI_TIMEOUT = 20 * 1000;
	private static final int WIFI_LEVEL_TIMEOUT = 3 * 1000;

	private WifiManager mWifiManager;
	private WifiHandler mWifiHandler;
	private boolean hasRegisterReceiver;
	private boolean isConnecting;
	private boolean needConnectAp;
	private boolean needCheck5G;
	
	private TestWifiInfo mWifiinfo;
	private TestWifiInfo mWifiinfo_5G;
	
	private int mStartSignalLevel;
	private int mEndSignalLevel;
	
	private ArrayList<TestWifiInfo> mTestWifiInfoList ;
	private int mCurTestIndex;
	private ArrayList<ResultInfo> mResultInfoList ;
	

	public WifiTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		mWifiHandler = new WifiHandler(handler.getLooper());
	}

	@Override
	public void onTestInit() {
		super.onTestInit();
	}

	@Override
	public boolean onTesting() {
		isConnecting = false;
		// Check params
		if (mTestCaseInfo == null || mTestCaseInfo.getAttachParams() == null) {
			onTestFail(R.string.wifi_err_attach_params);
			return false;
		}

		// Check specified wifi ap
		Map<String, String> attachParams = mTestCaseInfo.getAttachParams();
		String mSpecifiedAp = attachParams.get(ParamConstants.WIFI_AP);
		String mPassword = attachParams.get(ParamConstants.WIFI_PSW);		
		needCheck5G = "1".equals(attachParams
				.get(ParamConstants.WIFI_NEED_CHECK_5G));
		// mSpecifiedAp = "tvbox";
		if (StringUtils.isEmptyObj(mSpecifiedAp)) {
			onTestFail(R.string.wifi_err_ap_not_specified);
			return false;
		}

		// Check connect wifi ap
		needConnectAp = "1".equals(attachParams
				.get(ParamConstants.WIFI_CONNECT));

		// Check command parameter
		 mStartSignalLevel = StringUtils.parseInt(
				attachParams.get(ParamConstants.WIFI_DB_START), 999);
		 mEndSignalLevel = StringUtils.parseInt(
				attachParams.get(ParamConstants.WIFI_DB_END), 999);
		if (mStartSignalLevel == 999 || mEndSignalLevel == 999
				| mStartSignalLevel < mEndSignalLevel) {// [-30]----[-70]
			onTestFail(R.string.wifi_err_cmd_params);
			return false;
		}
		
		mWifiinfo = new TestWifiInfo(mSpecifiedAp, mPassword);
		mWifiinfo_5G = new TestWifiInfo(mSpecifiedAp+"_5G", mPassword);
//		mWifiinfo = new TestWifiInfo("tchip-B", "tchip2101");
//		mWifiinfo_5G =  new TestWifiInfo("tchip-D", "tchip2102");
		
		if(mTestWifiInfoList == null)mTestWifiInfoList=new ArrayList<WifiTest.TestWifiInfo>();
		else mTestWifiInfoList.removeAll(mTestWifiInfoList);
		
		if(mResultInfoList == null)mResultInfoList=new ArrayList<WifiTest.ResultInfo>();
		else mResultInfoList.removeAll(mResultInfoList);
		
		
		
		mTestWifiInfoList.add(mWifiinfo);
		if(needCheck5G)mTestWifiInfoList.add(mWifiinfo_5G);
		mCurTestIndex = 0;
		
		if (!hasRegisterReceiver) {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
			// intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
			// intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
			intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
			intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			// intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
			mContext.registerReceiver(mReceiver, intentFilter);
			hasRegisterReceiver = true;
		}

		EthernetManager ethManager = (EthernetManager) mContext
				.getSystemService(Context.ETHERNET_SERVICE);
		if(ethManager != null) {
			// 以太网已开启,需要先关闭以太网测试
			if(NetworkUtil.isEthAvailable(ethManager)){
				//ethManager.setEthernetEnabled(false);
				//ethManager.setInterfaceEnable("eth0",false);  //zouxf
			}
		}

		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
		setTestTimeout(DEFAULT_WIFI_TIMEOUT);
		return true;
	}

	@Override
	public boolean onTestHandled(TestResult result) {
		if (hasRegisterReceiver) {
			mContext.unregisterReceiver(mReceiver);
			hasRegisterReceiver = false;
		}
		SystemUtils.setEthEnable(mContext,true);
		super.onTestHandled(result);
		// boolean sendResult =
		// mViewHolder.setSendResult(sendResult?mContext.getString(R.string.));
		return true;
	}

	/**
	 * 停止测试
	 */
	public void stop() {

		if (hasRegisterReceiver) {
			mContext.unregisterReceiver(mReceiver);
			hasRegisterReceiver = false;
		}
		mWifiHandler.removeMessages(MSG_START_SCAN_WIFI);
		super.stop();
	}

	public class WifiHandler extends Handler {

		public WifiHandler() {
			super();
		}

		public WifiHandler(Looper looper) {
			super(looper);
		}

		private int mRetry = 0;

		void startScan() {
			if (!hasMessages(MSG_START_SCAN_WIFI)) {
				sendEmptyMessage(MSG_START_SCAN_WIFI);
			}
		}

		void forceScan() {
			removeMessages(MSG_START_SCAN_WIFI);
			sendEmptyMessage(MSG_START_SCAN_WIFI);
		}

		void stopScan() {
			mRetry = 0;
			removeMessages(MSG_START_SCAN_WIFI);
		}
		
		void checkNextWifi()
		{
			if (!hasMessages(MSG_CHECK_NEXT_WIFI)) {
				sendEmptyMessage(MSG_CHECK_NEXT_WIFI);
			}
		}

		void sendMessage(int what, int arg1) {
			Message message = obtainMessage();
			message.what = what;
			message.arg1 = arg1;
			message.sendToTarget();
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_START_SCAN_WIFI:
				if (mWifiManager.startScan()) {
					mRetry = 0;
				} else if (++mRetry >= 3) {
					mRetry = 0;
					onTestFail(R.string.wifi_err_scan_fail);
					return;
				}
				sendEmptyMessageDelayed(MSG_START_SCAN_WIFI,
						WIFI_RESCAN_INTERVAL_MS);
				break;
			case MSG_CHECK_NEXT_WIFI:
					if(mCurTestIndex < mTestWifiInfoList.size() -1)
					{
						mCurTestIndex++;
					}else{
						
					}
				break;
			}
		}
	}

	
	private ResultInfo checkAp(TestWifiInfo info)
	{
		ScanResult scanResult = getScanResultWithSpecifiedAp(info.mSpecifiedAp);
		ResultInfo result = new ResultInfo();
		StringBuilder sb = new StringBuilder();
		if(scanResult != null)
		{
			if (scanResult.level <= mStartSignalLevel
					&& scanResult.level >= mEndSignalLevel) {
				result.setSuccess(true);
				result.setInfo("Wifi: " + info.mSpecifiedAp
						+ ", dBm: " + scanResult.level+" ,frequency:"+scanResult.frequency);
			} else {						
				String errMsg = mContext.getString(
						R.string.wifi_err_signal_outrange,
						info.mSpecifiedAp,scanResult.level);
				result.setSuccess(false);
				result.setInfo(errMsg+" ,frequency:"+scanResult.frequency);
			}
		}else{
			String errMsg = mContext.getString(
					R.string.wifi_err_scan_outrange,
					info.mSpecifiedAp);
			result.setSuccess(false);
			result.setInfo(errMsg);
		}
		return result;
		
	}

	
	private void checkTestEnd()
	{
		if(mCurTestIndex >= mTestWifiInfoList.size() -1)
		{
			boolean isSuccess = true;
			StringBuilder sb = new StringBuilder();
			for (ResultInfo info : mResultInfoList) {
				isSuccess = isSuccess && info.isSuccess();
				sb.append(info.getInfo()+"\r\n");
			}
			if(isSuccess)onTestSuccess(sb.toString());
			else onTestFail(sb.toString());
		}else{
			TestWifiInfo curTestWifiInfo = mTestWifiInfoList.get(++mCurTestIndex);
			WifiConfiguration wifiConfig = getConfig(getScanResultWithSpecifiedAp(curTestWifiInfo.mSpecifiedAp),curTestWifiInfo);
			if (wifiConfig == null) {
				return;
			}
			setTestTimeout(DEFAULT_WIFI_TIMEOUT);
			WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
			if (mWifiManager.isWifiEnabled() && wifiInfo != null
					&& wifiInfo.getNetworkId() >= 0) {// 假如当前已连接，需要先断开
				if(checkApName(curTestWifiInfo.mSpecifiedAp,mWifiManager.getConnectionInfo().getSSID()))
					mWifiManager.forget(wifiInfo.getNetworkId(), null);
				else
					mWifiManager.disconnect();
			}
			updateDetail(mContext.getString(
					R.string.wifi_connecting_ap, curTestWifiInfo.mSpecifiedAp));
			mWifiManager.connect(wifiConfig, mConnectListener);
		}
	}
//	//判断5g是否存在
//	private void check5g()
//	{
//		ScanResult scanResult = getScanResultWithSpecifiedAp(mWifiinfo_5G.mSpecifiedAp);
//		if(scanResult == null)
//		{
//			
//		}
//		
//	}
	/**
	 * Wifi广播消息接收处理
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			
			String action = intent.getAction();
			LogUtil.d(
					this,action);
			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
						WifiManager.WIFI_STATE_UNKNOWN);
				if (state == WifiManager.WIFI_STATE_ENABLED) {
					mWifiHandler.startScan();
				}
			} else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
				//if(needCheck5G)check5g();
				ScanResult scanResult = getScanResultWithSpecifiedAp(mWifiinfo.mSpecifiedAp);
				
				if (scanResult == null) {
					return;
				}
				
				if (!needConnectAp) {
					ResultInfo info = checkAp(mWifiinfo) ;
					if(needCheck5G)
					{
						ResultInfo info_5G = checkAp(mWifiinfo_5G);
						if(info.isSuccess() && info_5G.isSuccess()){
							onTestSuccess(info.getInfo()+"\r\n"+info_5G.getInfo());
						} else {// 信号不符合，延迟三秒再次检测
							setTestTimeout(WIFI_LEVEL_TIMEOUT + 1000);
							Runnable checkLevelAction = new Runnable() {
								public void run() {
									LogUtil.d(WifiTest.this,
											"Delay check wifi signal. ");
									ResultInfo info = checkAp(mWifiinfo) ;
									ResultInfo info_5G = checkAp(mWifiinfo_5G);
									if(info.isSuccess() && info_5G.isSuccess())
									{
										onTestSuccess(info.getInfo()+"\r\n"+info_5G.getInfo());
									}else{
										onTestFail(info.getInfo()+"\r\n"+info_5G.getInfo());
									}
								}
							};
							mWifiHandler.postDelayed(checkLevelAction,
									WIFI_LEVEL_TIMEOUT);
						}
					}else{
						if(info.isSuccess() ){
							onTestSuccess(info.getInfo());
						} else {// 信号不符合，延迟三秒再次检测
							setTestTimeout(WIFI_LEVEL_TIMEOUT + 1000);
							Runnable checkLevelAction = new Runnable() {
								public void run() {
									LogUtil.d(WifiTest.this,
											"Delay check wifi signal. ");
									ResultInfo info = checkAp(mWifiinfo) ;
									if(info.isSuccess())
									{
										onTestSuccess(info.getInfo());
									}else{
										onTestFail(info.getInfo());
									}
								}
							};
							mWifiHandler.postDelayed(checkLevelAction,
									WIFI_LEVEL_TIMEOUT);
						}
					}
					return;
				}
				
				if(mCurTestIndex >= mTestWifiInfoList.size()) return;
				TestWifiInfo curTestWifiInfo = mTestWifiInfoList.get(mCurTestIndex);
				

				WifiConfiguration wifiConfig = getConfig(getScanResultWithSpecifiedAp(curTestWifiInfo.mSpecifiedAp),curTestWifiInfo);
				if (wifiConfig == null) {
					return;
				}
				if (!isConnecting) {
					isConnecting = true;
					mWifiHandler.stopScan();
					setTestTimeout(DEFAULT_WIFI_TIMEOUT);
					WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
					if (mWifiManager.isWifiEnabled() && wifiInfo != null
							&& wifiInfo.getNetworkId() >= 0) {// 假如当前已连接，需要先断开
						if(checkApName(curTestWifiInfo.mSpecifiedAp,mWifiManager.getConnectionInfo().getSSID()))
							mWifiManager.forget(wifiInfo.getNetworkId(), null);
						else
							mWifiManager.disconnect();
					}
					updateDetail(mContext.getString(
							R.string.wifi_connecting_ap, curTestWifiInfo.mSpecifiedAp));
					mWifiManager.connect(wifiConfig, mConnectListener);
				}
			} else if ((needConnectAp
					&& WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action) )) {
				NetworkInfo info = (NetworkInfo) intent
						.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

				if (isConnecting && info.isConnected()) {
					WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
					if (mWifiManager.isWifiEnabled() && wifiInfo != null
							&& wifiInfo.getIpAddress() != 0) {
						int rssi = wifiInfo.getRssi();
						int ipAddr = wifiInfo.getIpAddress();
						final StringBuffer ipBuf = new StringBuffer();
						ipBuf.append(ipAddr & 0xff).append('.')
								.append((ipAddr >>>= 8) & 0xff).append('.')
								.append((ipAddr >>>= 8) & 0xff).append('.')
								.append((ipAddr >>>= 8) & 0xff);

						int level = rssi;// ;scanResult.level;
						
						if(mCurTestIndex >= mTestWifiInfoList.size()) return;
						final TestWifiInfo curTestWifiInfo = mTestWifiInfoList.get(mCurTestIndex);
						
						if (level > mStartSignalLevel
								|| level < mEndSignalLevel) {
							ScanResult scanResult = getScanResultWithSpecifiedAp(curTestWifiInfo.mSpecifiedAp);
							level = scanResult.level;
						}

						if (level <= mStartSignalLevel
								&& level >= mEndSignalLevel) {
							ScanResult scanResult = getScanResultWithSpecifiedAp(curTestWifiInfo.mSpecifiedAp);
							mResultInfoList.add(new ResultInfo(true,"Wifi: " +curTestWifiInfo. mSpecifiedAp
									+ "," + ipBuf + ", dBm: "
									+ level+" ,frequency:"+scanResult.frequency));
							 checkTestEnd();
						} else {// 信号不符合，延迟三秒再次检测
							setTestTimeout(WIFI_LEVEL_TIMEOUT + 1000);
							Runnable checkLevelAction = new Runnable() {
								public void run() {
									LogUtil.d(WifiTest.this,
											"Delay check wifi signal after connect. ");
									ScanResult scanResult = getScanResultWithSpecifiedAp(curTestWifiInfo.mSpecifiedAp);
									int slevel = scanResult.level;
									if (slevel <= mStartSignalLevel
											&& slevel >= mEndSignalLevel) {
										mResultInfoList.add(new ResultInfo(true,"Wifi: " +curTestWifiInfo. mSpecifiedAp
												+ "," + ipBuf + ", dBm: "
												+ slevel+" ,frequency:"+scanResult.frequency));
										 checkTestEnd();
									} else {
										String errMsg = mContext
												.getString(
														R.string.wifi_err_signal_outrange,
														curTestWifiInfo.mSpecifiedAp,slevel)+" ,frequency:"+scanResult.frequency;
										mResultInfoList.add(new ResultInfo(false,errMsg));
										 checkTestEnd();
									}
								}
							};
							mWifiHandler.postDelayed(checkLevelAction,
									WIFI_LEVEL_TIMEOUT);
						}
						//mWifiManager.forget(wifiInfo.getNetworkId(), null);
						LogUtil.d(
								this,
								"Current wifi ap NetID: "
										+ wifiInfo.getNetworkId() + ", level: "
										+ level + ", startLevel: "
										+ mStartSignalLevel + ", endLevel: "
										+ mEndSignalLevel);
					}
					isConnecting = false;
				}
			} else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION
					.equals(action)) {

			}else if( WifiManager.RSSI_CHANGED_ACTION.equals(action))
			{
				WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

				String mWifiSsid = null;
				if (wifiInfo != null && wifiInfo.getWifiSsid() != null) {
					String unicode = wifiInfo.getWifiSsid().toString();
					if (!isEmpty(unicode)) {
						mWifiSsid = unicode;
					} else {
						mWifiSsid = wifiInfo.getWifiSsid().getHexString();
					}
				}
				if(mCurTestIndex >= mTestWifiInfoList.size()) return;
				final TestWifiInfo curTestWifiInfo = mTestWifiInfoList.get(mCurTestIndex);
				LogUtil.d(WifiTest.this,"mSpecifiedAp="+curTestWifiInfo.mSpecifiedAp+" ,wifiInfo.getSSID()="+mWifiSsid);
				if(mWifiManager.isWifiEnabled() && wifiInfo != null
						&& checkApName(curTestWifiInfo.mSpecifiedAp, mWifiSsid)
						&& wifiInfo.getIpAddress() != 0)
				{
					LogUtil.d(WifiTest.this,"mWifiManager.isWifiEnabled()="+mWifiManager.isWifiEnabled()+"  ,wifiInfo != null ="+(wifiInfo != null)
							+"   ,wifiInfo.getIpAddress()="+wifiInfo.getIpAddress());
					int rssi = wifiInfo.getRssi();
					int ipAddr = wifiInfo.getIpAddress();
					final StringBuffer ipBuf = new StringBuffer();
					ipBuf.append(ipAddr & 0xff).append('.')
							.append((ipAddr >>>= 8) & 0xff).append('.')
							.append((ipAddr >>>= 8) & 0xff).append('.')
							.append((ipAddr >>>= 8) & 0xff);

					int level = rssi;// ;scanResult.level;
					ScanResult scanResult = getScanResultWithSpecifiedAp(curTestWifiInfo.mSpecifiedAp);
					
					if (level > mStartSignalLevel
							|| level < mEndSignalLevel) {
						
						level = scanResult.level;
					}

					if (level <= mStartSignalLevel
							&& level >= mEndSignalLevel) {
						mResultInfoList.add(new ResultInfo(true,"Wifi: " +curTestWifiInfo. mSpecifiedAp
								+ "," + ipBuf + ", dBm: "
								+ level+" ,frequency:"+scanResult.frequency));
						 checkTestEnd();
					} else {// 信号不符合，延迟三秒再次检测
						setTestTimeout(WIFI_LEVEL_TIMEOUT + 1000);
						Runnable checkLevelAction = new Runnable() {
							public void run() {
								LogUtil.d(WifiTest.this,
										"Delay check wifi signal after connect. ");
								ScanResult scanResult = getScanResultWithSpecifiedAp(curTestWifiInfo.mSpecifiedAp);
								int slevel = scanResult.level;
								if (slevel <= mStartSignalLevel
										&& slevel >= mEndSignalLevel) {
									mResultInfoList.add(new ResultInfo(true,"Wifi: " +curTestWifiInfo. mSpecifiedAp
											+ "," + ipBuf + ", dBm: "
											+ slevel+" ,frequency:"+scanResult.frequency));
									 checkTestEnd();
								} else {
									String errMsg = mContext
											.getString(
													R.string.wifi_err_signal_outrange,
													curTestWifiInfo.mSpecifiedAp,slevel)+" ,frequency:"+scanResult.frequency;
									mResultInfoList.add(new ResultInfo(false,errMsg));
									 checkTestEnd();
								}
							}
						};
						mWifiHandler.postDelayed(checkLevelAction,
								WIFI_LEVEL_TIMEOUT);
					}
					//mWifiManager.forget(wifiInfo.getNetworkId(), null);
					LogUtil.d(
							this,
							"Current wifi ap NetID: "
									+ wifiInfo.getNetworkId() + ", level: "
									+ level + ", startLevel: "
									+ mStartSignalLevel + ", endLevel: "
									+ mEndSignalLevel);
				}
				
			}
		}
	};
	  /**
     * Returns true if the string is null or 0-length.
     * @param str the string to be examined
     * @return true if str is null or zero length
     */
	public static boolean isEmpty(CharSequence str) {
		if (str == null || str.length() == 0)
			return true;
		else
			return false;
	}
	boolean  checkApName(String specifiedAp ,String curSsid)
	{
		if(specifiedAp == null || curSsid == null ) return false;
		boolean isAp5G = specifiedAp.contains("5G");
		if(isAp5G)
		{
			String baseSsid = specifiedAp;
			int index = specifiedAp.indexOf("5G");
	    	if(index > 1)
	    	baseSsid = specifiedAp.substring(0,index-1);
	    	return  specifiedAp.contains("5G")
					&& specifiedAp.contains(baseSsid);
			
		}else{
			return specifiedAp.equals(curSsid);
		}
	}
	//扫描双wifi的规则是，例如2.4g的wifi ssid为xiaomi 则5g的为xiaomi_5G;
	//为了处理路由器配置名称错误，现在5g除了会扫描xiaomi_5G外，还会吧名称中包含xiaomi且
	//是5G的wifi扫描出来
	private ScanResult getScanResultWithSpecifiedAp(String specifiedAp) {
		if(specifiedAp == null) return null;
		boolean isAp5G = specifiedAp.contains("5G");
	    String baseSsid = specifiedAp;
	    if(isAp5G)
	    {
	    	int index = specifiedAp.indexOf("5G");
	    	if(index > 1)
	    	baseSsid = specifiedAp.substring(0,index-1);
	    }
	    
		ScanResult likeApScanResult = null;
		List<ScanResult> resultList = mWifiManager.getScanResults();
		if ((resultList != null) && (!resultList.isEmpty())) {
			for (ScanResult scanResult : resultList) {
				if (specifiedAp != null && specifiedAp.equals(scanResult.SSID)) {
					return scanResult;
				}

				if(isAp5G && specifiedAp != null 
						&& !specifiedAp.equals(scanResult.SSID)
						&& scanResult.SSID.contains("5G")
						&& scanResult.SSID.contains(baseSsid)
						&& scanResult.frequency > 5000)
				{
					likeApScanResult = scanResult;
				}
			}
		}
		return likeApScanResult;
	}
	
	
	


	// 连接WIFI处理
	/**
	 * These values are matched in string arrays -- changes must be kept in sync
	 */
	static final int SECURITY_NONE = 0;
	static final int SECURITY_WEP = 1;
	static final int SECURITY_PSK = 2;
	static final int SECURITY_EAP = 3;

	WifiManager.ActionListener mConnectListener = new WifiManager.ActionListener() {
		public void onSuccess() {
		}

		public void onFailure(int reason) {
			if(mCurTestIndex >= mTestWifiInfoList.size() ) return;
			String msg = mContext.getString(R.string.wifi_err_connect,
					mTestWifiInfoList.get(mCurTestIndex).mSpecifiedAp, mTestWifiInfoList.get(mCurTestIndex).mPassword);
			
			mResultInfoList.add(new ResultInfo(false, msg));
			checkTestEnd();			
		}
	};

	private WifiConfiguration getConfig(ScanResult sresult,TestWifiInfo info) {
		if(sresult == null)return null;
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = convertToQuotedString(sresult.SSID);
		int mAccessPointSecurity = getSecurity(sresult);
		if (mAccessPointSecurity == SECURITY_NONE) {
			config.allowedKeyManagement.set(KeyMgmt.NONE);
		} else {
			int length =info.mPassword.length();
			if (StringUtils.isEmptyObj(info.mPassword)) {
				onTestFail(R.string.wifi_err_pwd_unspecified);
				return null;
			}
			switch (mAccessPointSecurity) {
			case SECURITY_WEP:
				config.allowedKeyManagement.set(KeyMgmt.NONE);
				config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
				config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
				// WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
				if ((length == 10 || length == 26 || length == 58)
						&& info.mPassword.matches("[0-9A-Fa-f]*")) {
					config.wepKeys[0] = info.mPassword;
				} else {
					config.wepKeys[0] = '"' + info.mPassword + '"';
				}
				break;

			case SECURITY_PSK:
				config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
				if (info.mPassword.matches("[0-9A-Fa-f]{64}")) {
					config.preSharedKey = info.mPassword;
				} else {
					config.preSharedKey = '"' + info.mPassword + '"';
				}
				break;

			case SECURITY_EAP:
				onTestFail(R.string.wifi_err_pwd_unsupport);
				return null;
			default:
				return null;
			}
		}
		
		/*config.setIpConfiguration(
                new IpConfiguration(IpAssignment.UNASSIGNED, ProxySettings.UNASSIGNED,
                                    null, null));*/
		 
		/* config.setIpConfiguration(
	                new IpConfiguration(IpAssignment.UNASSIGNED, ProxySettings.UNASSIGNED,
	                		new StaticIpConfiguration(), null));*/
		config.setProxySettings(ProxySettings.UNASSIGNED);
		config.setIpAssignment(IpAssignment.UNASSIGNED);
		//config
//			config.proxySettings = ProxySettings.UNASSIGNED;
//			config.ipAssignment = IpAssignment.UNASSIGNED;
//			config.linkProperties = new LinkProperties();
		return config;
	}

	static String convertToQuotedString(String string) {
		return "\"" + string + "\"";
	}

	static int getSecurity(ScanResult result) {
		if (result.capabilities.contains("WEP")) {
			return SECURITY_WEP;
		} else if (result.capabilities.contains("PSK")) {
			return SECURITY_PSK;
		} else if (result.capabilities.contains("EAP")) {
			return SECURITY_EAP;
		}
		return SECURITY_NONE;
	}
	
	
	

}
