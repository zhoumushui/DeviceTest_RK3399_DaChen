package com.rockchip.devicetest.utils;

import java.util.List;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.testcase.impl.WifiTest.WifiHandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
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
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;

public class WifiUtils {

	public interface WifiConnectionListener{
		public void onSuccess(String ssid);
		public void onFailure();
		
	}
	WifiConnectionListener mConnectionListener;
	public void setOnWifiConnectionListener(WifiConnectionListener mConnectionListener){
		this.mConnectionListener = mConnectionListener;
	}
	
	
	private Context mContext;
	private boolean hasRegisterReceiver = false;
	private WifiHandler mWifiHandler;
	private WifiManager mWifiManager;
	private String mSpecifiedAp;
	private String mPassword;

	public WifiUtils(Context mContext) {
		super();
		this.mContext = mContext;
		mWifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		mWifiHandler = new WifiHandler();
	}
	
	public void connectWifi(String ssid,String psd)
	{
		mSpecifiedAp = ssid;
		mPassword = psd;
		WifiConfiguration wifiConfig ;
		ScanResult  scanresult = getScanResultWithSpecifiedAp(mSpecifiedAp);
		if(scanresult != null && (wifiConfig= getConfig(scanresult,mPassword)) !=null && !isConnecting)
		{
			WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
			if (mWifiManager.isWifiEnabled() && wifiInfo != null
					&& wifiInfo.getNetworkId() >= 0) {// 假如当前已连接，需要先断开
				mWifiManager.disconnect();//(wifiInfo.getNetworkId(), null);
			}
		
			mWifiManager.connect(wifiConfig, mConnectListener);
			isConnecting = true;
		}else{
			mWifiHandler.startScan();
		}
	}
	
	WifiManager.ActionListener mConnectListener = new WifiManager.ActionListener() {
		public void onSuccess() {
			if(mConnectionListener != null)mConnectionListener.onSuccess(mSpecifiedAp);
			isConnecting = false;
		}

		public void onFailure(int reason) {
			if(mConnectionListener != null)mConnectionListener.onFailure();
			isConnecting = false;
			
		}
		
		
	};

	public void registerReceiver()
	{
		if (!hasRegisterReceiver) {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
			intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
			intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
			intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			mContext.registerReceiver(mReceiver, intentFilter);
			hasRegisterReceiver = true;
		}
	}
	public void unregisterReceiver()
	{
		if (hasRegisterReceiver) {
			mContext.unregisterReceiver(mReceiver);
			hasRegisterReceiver = false;
		}
		mWifiHandler.stopScan();
	}
	
	
	boolean isConnecting = false;

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
				
				ScanResult  scanresult = getScanResultWithSpecifiedAp(mSpecifiedAp);
				if(scanresult == null) return;
				
				WifiConfiguration wifiConfig = getConfig(scanresult,mPassword);
				if (wifiConfig == null)return;
				
				if (!isConnecting) {
					isConnecting = true;
					mWifiHandler.stopScan();
					WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
					if (mWifiManager.isWifiEnabled() && wifiInfo != null
							&& wifiInfo.getNetworkId() >= 0) {// 假如当前已连接，需要先断开
						mWifiManager.disconnect();//mWifiManager.forget(wifiInfo.getNetworkId(), null);
					}
					
					mWifiManager.connect(wifiConfig, mConnectListener);

				} else if (( WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action) )) {
					NetworkInfo info = (NetworkInfo) intent
							.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);




				} 
			} else if (( WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action) )) {
				NetworkInfo info = (NetworkInfo) intent
						.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
				
			}
		}
	};
	
	// Message
	public static final int MSG_START_SCAN_WIFI = 1;
	public static final int MSG_CHECK_NEXT_WIFI = 2;

	// Combo scans can take 5-6s to complete - set to 10s.
	private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;
	private static final int DEFAULT_WIFI_TIMEOUT = 20 * 1000;
	private static final int WIFI_LEVEL_TIMEOUT = 3 * 1000;
	
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
					return;
				}
				sendEmptyMessageDelayed(MSG_START_SCAN_WIFI,
						WIFI_RESCAN_INTERVAL_MS);
				break;
			}
		}
	}
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
	public static boolean  checkApName(String specifiedAp ,String curSsid)
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


	private WifiConfiguration getConfig(ScanResult sresult,String mPassword) {
		if(sresult == null)return null;
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = convertToQuotedString(sresult.SSID);
		int mAccessPointSecurity = getSecurity(sresult);
		if (mAccessPointSecurity == SECURITY_NONE) {
			config.allowedKeyManagement.set(KeyMgmt.NONE);
		} else {
			int length =mPassword.length();
			if (StringUtils.isEmptyObj(mPassword)) {
				//onTestFail(R.string.wifi_err_pwd_unspecified);
				return null;
			}
			switch (mAccessPointSecurity) {
			case SECURITY_WEP:
				config.allowedKeyManagement.set(KeyMgmt.NONE);
				config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
				config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
				// WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
				if ((length == 10 || length == 26 || length == 58)
						&&mPassword.matches("[0-9A-Fa-f]*")) {
					config.wepKeys[0] = mPassword;
				} else {
					config.wepKeys[0] = '"' + mPassword + '"';
				}
				break;

			case SECURITY_PSK:
				config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
				if (mPassword.matches("[0-9A-Fa-f]{64}")) {
					config.preSharedKey = mPassword;
				} else {
					config.preSharedKey = '"' + mPassword + '"';
				}
				break;


			default:
				return null;
			}
		}
		

		config.setProxySettings(ProxySettings.UNASSIGNED);
		config.setIpAssignment(IpAssignment.UNASSIGNED);
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
