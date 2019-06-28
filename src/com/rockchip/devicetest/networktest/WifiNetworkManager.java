package com.rockchip.devicetest.networktest;

import java.util.List;

import com.rockchip.devicetest.R;

import com.rockchip.devicetest.testcase.impl.WifiTest.WifiHandler;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.NetworkUtil;
import com.rockchip.devicetest.utils.StringUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.IpConfiguration;
import android.net.ProxyInfo;
import android.net.RouteInfo;
import android.net.StaticIpConfiguration;
import android.net.EthernetManager;

import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;

public class WifiNetworkManager extends BaseNetworkManager{

	// Message
	public static final int MSG_START_SCAN_WIFI = 1;
	public static final int MSG_CHECK_NEXT_WIFI = 2;

	// Combo scans can take 5-6s to complete - set to 10s.
	private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;
	private static final int DEFAULT_WIFI_TIMEOUT = 20 * 1000;
	private static final int WIFI_LEVEL_TIMEOUT = 3 * 1000;
	
	private boolean isConnecting;

	WifiTestInfo mWifiTestInfo;
	private WifiHandler mWifiHandler;
	public WifiNetworkManager(Context context,WifiTestInfo info) {
		super(context);
		// TODO Auto-generated constructor stub
		mWifiTestInfo = info;
		mWifiHandler = new WifiHandler();

		if(mEthManager != null) {
			// 以太网已开启,需要先关闭以太网测试
			if(NetworkUtil.isEthAvailable(mEthManager)){
				//mEthManager.setEthernetEnabled(false);
			}
		}

		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	@Override
	public void endConnectTest() {
		// TODO Auto-generated method stub
		super.endConnectTest();
		mWifiHandler.removeMessages(MSG_START_SCAN_WIFI);
	}

	@Override
	public void startConnectTest() {
		// TODO Auto-generated method stub
		super.startConnectTest();
		isConnecting = false;
	}

	@Override
	public void registerReceiver() {
		// TODO Auto-generated method stub
		super.registerReceiver();
		if(hasRegistReceiver)return;	
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		// intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
		// intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		// intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		mContext.registerReceiver(mWifiReceiver, intentFilter);
		hasRegistReceiver = true;

	}

	@Override
	public void unRegisterReceiver() {
		// TODO Auto-generated method stub
		super.unRegisterReceiver();
		if(!hasRegistReceiver)return;
		mContext.unregisterReceiver(mWifiReceiver);
		hasRegistReceiver = false;
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
					mConnectResultInfo.setResultInfo(false, mContext.getString(R.string.wifi_err_scan_fail), true);
					endConnectTest();
					return;
				}
				sendEmptyMessageDelayed(MSG_START_SCAN_WIFI,
						WIFI_RESCAN_INTERVAL_MS);
				break;
			}
		}
	}

	/**
	 * Wifi广播消息接收处理
	 */
	BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
						WifiManager.WIFI_STATE_UNKNOWN);
				if (state == WifiManager.WIFI_STATE_ENABLED) {
					mWifiHandler.startScan();
				}
			} else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
				ScanResult scanResult = getScanResultWithSpecifiedAp(mWifiTestInfo.getSpecifiedAp());
				if (scanResult == null) {
					return;
				}
				if (!mWifiTestInfo.isNeedConnectAp()) {// 不需要连接AP测试
					int level = scanResult.level;
					if (level <= mWifiTestInfo.getStartSignalLevel() && level >= mWifiTestInfo.getEndSignalLevel()) {
						mConnectResultInfo.setResultInfo(true, "Wifi: " + mWifiTestInfo.getSpecifiedAp() + ", dBm: "
								+ level, true);
						endConnectTest();
					} else {// 信号不符合，延迟三秒再次检测
						setConnectTestTimeOut(WIFI_LEVEL_TIMEOUT + 1000);
						Runnable checkLevelAction = new Runnable() {
							public void run() {
								LogUtil.d(this,
										"Delay check wifi signal. ");
								ScanResult scanResult = getScanResultWithSpecifiedAp(mWifiTestInfo.getSpecifiedAp());
								int slevel = scanResult.level;
								if (slevel <= mWifiTestInfo.getStartSignalLevel()
										&& slevel >= mWifiTestInfo.getEndSignalLevel()) {
									mConnectResultInfo.setResultInfo(true, "Wifi: " + mWifiTestInfo.getSpecifiedAp() + ", dBm: "
											+ slevel, true);
									endConnectTest();
								} else {
									String errMsg = mContext.getString(
											R.string.wifi_err_signal_outrange,
											slevel);
									mConnectResultInfo.setResultInfo(false, errMsg, true);
									endConnectTest();
								}
							}
						};
						mWifiHandler.postDelayed(checkLevelAction,
								WIFI_LEVEL_TIMEOUT);
					}
					return;
				}

				WifiConfiguration wifiConfig = getConfig(scanResult);
				if (wifiConfig == null) {
					return;
				}
				if (!isConnecting) {
					isConnecting = true;
					mWifiHandler.stopScan();
					setConnectTestTimeOut(DEFAULT_WIFI_TIMEOUT);
					WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
					if (mWifiManager.isWifiEnabled() && wifiInfo != null
							&& wifiInfo.getNetworkId() >= 0) {// 假如当前已连接，需要先断开
						mWifiManager.forget(wifiInfo.getNetworkId(), null);
					}
					updateDetail(mContext.getString(
							R.string.wifi_connecting_ap, mWifiTestInfo.getSpecifiedAp()));
					mWifiManager.connect(wifiConfig, mConnectListener);
				}
			} else if (mWifiTestInfo.isNeedConnectAp()
					&& WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
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

						if (level > mWifiTestInfo.getStartSignalLevel()
								|| level < mWifiTestInfo.getEndSignalLevel()) {
							ScanResult scanResult = getScanResultWithSpecifiedAp(mWifiTestInfo.getSpecifiedAp());
							level = scanResult.level;
						}

						if (level <= mWifiTestInfo.getStartSignalLevel()
								&& level >= mWifiTestInfo.getEndSignalLevel()) {
							mConnectResultInfo.setResultInfo(true,"Wifi: " + mWifiTestInfo.getSpecifiedAp() + "," + ipBuf
									+ ", dBm: " + level, true);
							endConnectTest();
						} else {// 信号不符合，延迟三秒再次检测
							setConnectTestTimeOut(WIFI_LEVEL_TIMEOUT + 1000);
							Runnable checkLevelAction = new Runnable() {
								public void run() {
									LogUtil.d(this,
											"Delay check wifi signal after connect. ");
									ScanResult scanResult = getScanResultWithSpecifiedAp(mWifiTestInfo.getSpecifiedAp());
									int slevel = scanResult.level;
									if (slevel <= mWifiTestInfo.getStartSignalLevel()
											&& slevel >= mWifiTestInfo.getEndSignalLevel()) {
										mConnectResultInfo.setResultInfo(true,"Wifi: " + mWifiTestInfo.getSpecifiedAp()
												+ "," + ipBuf + ", dBm: "+ slevel, true);
										endConnectTest();
									} else {
										String errMsg = mContext
												.getString(
														R.string.wifi_err_signal_outrange,
														slevel);
										mConnectResultInfo.setResultInfo(false,errMsg, true);
										endConnectTest();
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
										+ mWifiTestInfo.getStartSignalLevel() + ", endLevel: "
										+ mWifiTestInfo.getEndSignalLevel());
					}
					isConnecting = false;
				}
			} else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION
					.equals(action)) {

			}else if (WifiManager.RSSI_CHANGED_ACTION.equals(action))
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
				LogUtil.d(WifiNetworkManager.this,"mSpecifiedAp="+mWifiTestInfo.getSpecifiedAp()+" ,wifiInfo.getSSID()="+mWifiSsid);
				if(mWifiManager.isWifiEnabled() && wifiInfo != null && mWifiTestInfo.getSpecifiedAp().equals(mWifiSsid) && wifiInfo.getIpAddress() != 0)
				{
					LogUtil.d(WifiNetworkManager.this,"mWifiManager.isWifiEnabled()="+mWifiManager.isWifiEnabled()+"  ,wifiInfo != null ="+(wifiInfo != null)
							+"   ,wifiInfo.getIpAddress()="+wifiInfo.getIpAddress());
					int rssi = wifiInfo.getRssi();
					int ipAddr = wifiInfo.getIpAddress();
					final StringBuffer ipBuf = new StringBuffer();
					ipBuf.append(ipAddr & 0xff).append('.')
					.append((ipAddr >>>= 8) & 0xff).append('.')
					.append((ipAddr >>>= 8) & 0xff).append('.')
					.append((ipAddr >>>= 8) & 0xff);

					int level = rssi;// ;scanResult.level;

					if (level > mWifiTestInfo.getStartSignalLevel()
							|| level < mWifiTestInfo.getEndSignalLevel()) {
						ScanResult scanResult = getScanResultWithSpecifiedAp(mWifiTestInfo.getSpecifiedAp());
						level = scanResult.level;
					}

					if (level <= mWifiTestInfo.getStartSignalLevel()
							&& level >= mWifiTestInfo.getEndSignalLevel()) {
						mConnectResultInfo.setResultInfo(true,"Wifi: " + mWifiTestInfo.getSpecifiedAp() + "," + ipBuf
								+ ", dBm: " + level, true);
						endConnectTest();
					} else {// 信号不符合，延迟三秒再次检测
						setConnectTestTimeOut(WIFI_LEVEL_TIMEOUT + 1000);
						Runnable checkLevelAction = new Runnable() {
							public void run() {
								LogUtil.d(WifiNetworkManager.this,
										"Delay check wifi signal after connect. ");
								ScanResult scanResult = getScanResultWithSpecifiedAp(mWifiTestInfo.getSpecifiedAp());
								int slevel = scanResult.level;
								if (slevel <= mWifiTestInfo.getStartSignalLevel()
										&& slevel >= mWifiTestInfo.getEndSignalLevel()) {
									mConnectResultInfo.setResultInfo(true,"Wifi: " + mWifiTestInfo.getSpecifiedAp()
											+ "," + ipBuf + ", dBm: "+ slevel, true);
									endConnectTest();
								} else {
									String errMsg = mContext
											.getString(
													R.string.wifi_err_signal_outrange,
													slevel);
									mConnectResultInfo.setResultInfo(false,errMsg, true);
									endConnectTest();
								}
							}
						};
						mWifiHandler.postDelayed(checkLevelAction,
								WIFI_LEVEL_TIMEOUT);
					}
					//mWifiManager.forget(wifiInfo.getNetworkId(), null);
					LogUtil.d(
							WifiNetworkManager.this,
							"Current wifi ap NetID: "
									+ wifiInfo.getNetworkId() + ", level: "
									+ level + ", startLevel: "
									+ mWifiTestInfo.getStartSignalLevel() + ", endLevel: "
									+ mWifiTestInfo.getEndSignalLevel());
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
	private ScanResult getScanResultWithSpecifiedAp(String specifiedAp) {
		List<ScanResult> resultList = mWifiManager.getScanResults();
		if ((resultList != null) && (!resultList.isEmpty())) {
			for (ScanResult scanResult : resultList) {
				if (specifiedAp != null && specifiedAp.equals(scanResult.SSID)) {
					return scanResult;
				}
			}
		}
		return null;
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
			String errmsg = mContext.getString(R.string.wifi_err_connect,
					mWifiTestInfo.getSpecifiedAp(), mWifiTestInfo.getPassword());
			mConnectResultInfo.setResultInfo(false,errmsg, true);
			endConnectTest();
			
		}
	};

	private WifiConfiguration getConfig(ScanResult sresult) {
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = convertToQuotedString(sresult.SSID);
		int mAccessPointSecurity = getSecurity(sresult);
		if (mAccessPointSecurity == SECURITY_NONE) {
			config.allowedKeyManagement.set(KeyMgmt.NONE);
		} else {
			int length = mWifiTestInfo.getPassword().length();
			if (StringUtils.isEmptyObj(mWifiTestInfo.getPassword())) {
				mConnectResultInfo.setResultInfo(false, mContext.getString(R.string.wifi_err_pwd_unspecified), true);
				endConnectTest();
				return null;
			}
			switch (mAccessPointSecurity) {
			case SECURITY_WEP:
				config.allowedKeyManagement.set(KeyMgmt.NONE);
				config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
				config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
				// WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
				if ((length == 10 || length == 26 || length == 58)
						&& mWifiTestInfo.getPassword().matches("[0-9A-Fa-f]*")) {
					config.wepKeys[0] = mWifiTestInfo.getPassword();
				} else {
					config.wepKeys[0] = '"' + mWifiTestInfo.getPassword() + '"';
				}
				break;

			case SECURITY_PSK:
				config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
				if (mWifiTestInfo.getPassword().matches("[0-9A-Fa-f]{64}")) {
					config.preSharedKey = mWifiTestInfo.getPassword();
				} else {
					config.preSharedKey = '"' + mWifiTestInfo.getPassword() + '"';
				}
				break;

			case SECURITY_EAP:
				mConnectResultInfo.setResultInfo(false, mContext.getString(R.string.wifi_err_pwd_unspecified), true);
				endConnectTest();
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
		//				config.proxySettings = ProxySettings.UNASSIGNED;
		//				config.ipAssignment = IpAssignment.UNASSIGNED;
		//				config.linkProperties = new LinkProperties();
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
