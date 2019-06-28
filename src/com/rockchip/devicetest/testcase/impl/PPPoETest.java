package com.rockchip.devicetest.testcase.impl;

import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.net.EthernetManager;
import android.net.wifi.WifiEnterpriseConfig;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.StringUtils;

public class PPPoETest extends BaseTestCase {

	// Message
	public static final int MSG_START_SCAN_WIFI = 1;

	// Combo scans can take 5-6s to complete - set to 10s.
	private static final int WIFI_RESCAN_INTERVAL_MS = 10 * 1000;
	private static final int DEFAULT_WIFI_TIMEOUT = 20 * 1000;
	private static final int WIFI_LEVEL_TIMEOUT = 3 * 1000;

	private WifiManager mWifiManager;
	//private WifiHandler mWifiHandler;
	private String mSpecifiedAp;
	private String mPassword;
	private int mStartSignalLevel;
	private int mEndSignalLevel;
	private boolean hasRegisterReceiver;
	private boolean isConnecting;
	private boolean needConnectAp;

	private Context mContext;
	private RadioButton radioWifi;
	private RadioButton radioEthernet;
	private EditText etUser;
	private EditText etPassword;
	private TextView tvUser;
	private TextView tvPassword;
	private View view;

	private EthernetManager mEthernetManager;

	public PPPoETest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		this.mContext = context;
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
	//	mWifiHandler = new WifiHandler(handler.getLooper());
		init();
	}

	private void init() {
		view = LayoutInflater.from(mContext).inflate(R.layout.test_pppoe, null);
		radioWifi = (RadioButton) view.findViewById(R.id.radioWifi);
		radioEthernet = (RadioButton) view.findViewById(R.id.radioEthernet);
		etUser = (EditText) view.findViewById(R.id.etUser);
		etPassword = (EditText) view.findViewById(R.id.etPassword);
		tvUser = (TextView) view.findViewById(R.id.tvUser);
		tvPassword = (TextView) view.findViewById(R.id.tvPassword);
	}

	@Override
	public void onTestInit() {
		// TODO Auto-generated method stub
		super.onTestInit();
	}

	@Override
	public boolean onTesting() {
		// TODO Auto-generated method stub
		isConnecting = false;
		onTestFail(R.string.wifi_err_attach_params);
		 return super.onTesting();
//		// Check params
//		if (mTestCaseInfo == null || mTestCaseInfo.getAttachParams() == null) {
//			onTestFail(R.string.wifi_err_attach_params);
//			return false;
//		}
//		etUser.setText("tchip-C");
//		etPassword.setText("tchip2102");
//		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
//		builder.setTitle("PPPoE连接测试");
//		builder.setView(view);
//		builder.setPositiveButton("开始测试", new OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				if (radioWifi.isChecked()) {
//					mSpecifiedAp = etUser.getText().toString().trim();
//					mPassword = etPassword.getText().toString().trim();
//					Log.i("test", mSpecifiedAp);
//				} else if (radioEthernet.isChecked()) {
//					etUser.setVisibility(View.GONE);
//					etPassword.setVisibility(View.GONE);
//					tvUser.setVisibility(View.GONE);
//					tvPassword.setVisibility(View.GONE);
//					Log.i("test", "Ethernet");
//				}
//			}
//		});
//		builder.setNegativeButton("取消测试", new OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				// TODO Auto-generated method stub
//				etUser.setText("");
//				etPassword.setText("");
//			}
//		});
//		builder.create().show();
//
//		Map<String, String> attachParams = mTestCaseInfo.getAttachParams();
//		if (StringUtils.isEmptyObj(mSpecifiedAp)) {
//			onTestFail(R.string.wifi_err_ap_not_specified);
//			return false;
//		}
//
//		// Check connect wifi ap
//		needConnectAp = "1".equals(attachParams
//				.get(ParamConstants.WIFI_CONNECT));
//
//		// Check command parameter
//		mStartSignalLevel = StringUtils.parseInt(
//				attachParams.get(ParamConstants.WIFI_DB_START), 999);
//		mEndSignalLevel = StringUtils.parseInt(
//				attachParams.get(ParamConstants.WIFI_DB_END), 999);
//		if (mStartSignalLevel == 999 || mEndSignalLevel == 999
//				| mStartSignalLevel < mEndSignalLevel) {// [-30]----[-70]
//			onTestFail(R.string.wifi_err_cmd_params);
//			return false;
//		}
//		if (!hasRegisterReceiver) {
//			IntentFilter intentFilter = new IntentFilter();
//			intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
//			intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
//			// intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
//			// intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
//			intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
//			intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//			// intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
//			mContext.registerReceiver(mReceiver, intentFilter);
//			hasRegisterReceiver = true;
//		}
//
//		EthernetManager ethManager = (EthernetManager) mContext
//				.getSystemService(Context.ETHERNET_SERVICE);
//		int ethEnabler = ethManager.getEthernetIfaceState();
//		if (ethEnabler == 1) {// 以太网已开启,需要先关闭以太网测试
//			ethManager.setEthernetEnabled(false);
//		}
//
//		if (!mWifiManager.isWifiEnabled()) {
//			mWifiManager.setWifiEnabled(true);
//		}
//		setTestTimeout(DEFAULT_WIFI_TIMEOUT);
//		return true;
	}

//	@Override
//	public boolean onTestHandled(TestResult result) {
//		if (hasRegisterReceiver) {
//			mContext.unregisterReceiver(mReceiver);
//			hasRegisterReceiver = false;
//		}
//		super.onTestHandled(result);
//		return true;
//	}
//
//	/**
//	 * 停止测试
//	 */
//	public void stop() {
//		if (hasRegisterReceiver) {
//			mContext.unregisterReceiver(mReceiver);
//			hasRegisterReceiver = false;
//		}
//		mWifiHandler.removeMessages(MSG_START_SCAN_WIFI);
//		super.stop();
//	}
//
//	public class WifiHandler extends Handler {
//
//		public WifiHandler() {
//			super();
//		}
//
//		public WifiHandler(Looper looper) {
//			super(looper);
//		}
//
//		private int mRetry = 0;
//
//		void startScan() {
//			if (!hasMessages(MSG_START_SCAN_WIFI)) {
//				sendEmptyMessage(MSG_START_SCAN_WIFI);
//			}
//		}
//
//		void forceScan() {
//			removeMessages(MSG_START_SCAN_WIFI);
//			sendEmptyMessage(MSG_START_SCAN_WIFI);
//		}
//
//		void stopScan() {
//			mRetry = 0;
//			removeMessages(MSG_START_SCAN_WIFI);
//		}
//
//		void sendMessage(int what, int arg1) {
//			Message message = obtainMessage();
//			message.what = what;
//			message.arg1 = arg1;
//			message.sendToTarget();
//		}
//
//		@Override
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case MSG_START_SCAN_WIFI:
//				if (mWifiManager.startScan()) {
//					mRetry = 0;
//				} else if (++mRetry >= 3) {
//					mRetry = 0;
//					onTestFail(R.string.wifi_err_scan_fail);
//					return;
//				}
//				sendEmptyMessageDelayed(MSG_START_SCAN_WIFI,
//						WIFI_RESCAN_INTERVAL_MS);
//				break;
//			}
//		}
//	}
//
//	/**
//	 * Wifi广播消息接收处理
//	 */
//	BroadcastReceiver mReceiver = new BroadcastReceiver() {
//		public void onReceive(Context context, Intent intent) {
//
//			String action = intent.getAction();
//			if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
//				int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
//						WifiManager.WIFI_STATE_UNKNOWN);
//				if (state == WifiManager.WIFI_STATE_ENABLED) {
//					mWifiHandler.startScan();
//				}
//			} else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
//				ScanResult scanResult = getScanResultWithSpecifiedAp(mSpecifiedAp);
//				if (scanResult == null) {
//					return;
//				}
//				if (!needConnectAp) {// 不需要连接AP测试
//					int level = scanResult.level;
//					if (level <= mStartSignalLevel && level >= mEndSignalLevel) {
//						onTestSuccess("Wifi: " + mSpecifiedAp + ", dBm: "
//								+ level);
//					} else {// 信号不符合，延迟三秒再次检测
//						setTestTimeout(WIFI_LEVEL_TIMEOUT + 1000);
//						Runnable checkLevelAction = new Runnable() {
//							public void run() {
//								ScanResult scanResult = getScanResultWithSpecifiedAp(mSpecifiedAp);
//								int slevel = scanResult.level;
//								if (slevel <= mStartSignalLevel
//										&& slevel >= mEndSignalLevel) {
//									onTestSuccess("Wifi: " + mSpecifiedAp
//											+ ", dBm: " + slevel);
//								} else {
//									String errMsg = mContext.getString(
//											R.string.wifi_err_signal_outrange,
//											slevel);
//									onTestFail(errMsg);
//								}
//							}
//						};
//						mWifiHandler.postDelayed(checkLevelAction,
//								WIFI_LEVEL_TIMEOUT);
//					}
//					return;
//				}
//
//				WifiConfiguration wifiConfig = getConfig(scanResult);
//				if (wifiConfig == null) {
//					return;
//				}
//				if (!isConnecting) {
//					isConnecting = true;
//					mWifiHandler.stopScan();
//					setTestTimeout(DEFAULT_WIFI_TIMEOUT);
//					WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
//					if (mWifiManager.isWifiEnabled() && wifiInfo != null
//							&& wifiInfo.getNetworkId() >= 0) {// 假如当前已连接，需要先断开
//						mWifiManager.forget(wifiInfo.getNetworkId(), null);
//					}
//					updateDetail(mContext.getString(
//							R.string.wifi_connecting_ap, mSpecifiedAp));
//					mWifiManager.connect(wifiConfig, mConnectListener);
//				}
//			} else if (needConnectAp
//					&& WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
//				NetworkInfo info = (NetworkInfo) intent
//						.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
//				if (isConnecting && info.isConnected()) {
//					WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
//					if (mWifiManager.isWifiEnabled() && wifiInfo != null
//							&& wifiInfo.getIpAddress() != 0) {
//						int rssi = wifiInfo.getRssi();
//						int ipAddr = wifiInfo.getIpAddress();
//						final StringBuffer ipBuf = new StringBuffer();
//						ipBuf.append(ipAddr & 0xff).append('.')
//								.append((ipAddr >>>= 8) & 0xff).append('.')
//								.append((ipAddr >>>= 8) & 0xff).append('.')
//								.append((ipAddr >>>= 8) & 0xff);
//
//						int level = rssi;// ;scanResult.level;
//						if (level > mStartSignalLevel
//								|| level < mEndSignalLevel) {
//							ScanResult scanResult = getScanResultWithSpecifiedAp(mSpecifiedAp);
//							level = scanResult.level;
//						}
//
//						if (level <= mStartSignalLevel
//								&& level >= mEndSignalLevel) {
//							onTestSuccess("Wifi: " + mSpecifiedAp + "," + ipBuf
//									+ ", dBm: " + level);
//						} else {// 信号不符合，延迟三秒再次检测
//							setTestTimeout(WIFI_LEVEL_TIMEOUT + 1000);
//							Runnable checkLevelAction = new Runnable() {
//								public void run() {
//									ScanResult scanResult = getScanResultWithSpecifiedAp(mSpecifiedAp);
//									int slevel = scanResult.level;
//									if (slevel <= mStartSignalLevel
//											&& slevel >= mEndSignalLevel) {
//										onTestSuccess("Wifi: " + mSpecifiedAp
//												+ "," + ipBuf + ", dBm: "
//												+ slevel);
//									} else {
//										String errMsg = mContext
//												.getString(
//														R.string.wifi_err_signal_outrange,
//														slevel);
//										onTestFail(errMsg);
//									}
//								}
//							};
//							mWifiHandler.postDelayed(checkLevelAction,
//									WIFI_LEVEL_TIMEOUT);
//						}
//						mWifiManager.forget(wifiInfo.getNetworkId(), null);
//						LogUtil.d(
//								this,
//								"Current wifi ap NetID: "
//										+ wifiInfo.getNetworkId() + ", level: "
//										+ level + ", startLevel: "
//										+ mStartSignalLevel + ", endLevel: "
//										+ mEndSignalLevel);
//					}
//					isConnecting = false;
//				}
//			} else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION
//					.equals(action)) {
//
//			}
//		}
//	};
//
//	private ScanResult getScanResultWithSpecifiedAp(String specifiedAp) {
//		List<ScanResult> resultList = mWifiManager.getScanResults();
//		if ((resultList != null) && (!resultList.isEmpty())) {
//			for (ScanResult scanResult : resultList) {
//				if (specifiedAp != null && specifiedAp.equals(scanResult.SSID)) {
//					return scanResult;
//				}
//			}
//		}
//		return null;
//	}
//
//	// 连接WIFI处理
//	/**
//	 * These values are matched in string arrays -- changes must be kept in sync
//	 */
//	static final int SECURITY_NONE = 0;
//	static final int SECURITY_WEP = 1;
//	static final int SECURITY_PSK = 2;
//	static final int SECURITY_EAP = 3;
//
//	WifiManager.ActionListener mConnectListener = new WifiManager.ActionListener() {
//		public void onSuccess() {
//		}
//
//		public void onFailure(int reason) {
//			String msg = mContext.getString(R.string.wifi_err_connect,
//					mSpecifiedAp, mPassword);
//			onTestFail(msg);
//		}
//	};
//
//	private WifiConfiguration getConfig(ScanResult sresult) {
//		WifiConfiguration config = new WifiConfiguration();
//		config.SSID = convertToQuotedString(sresult.SSID);
//		int mAccessPointSecurity = getSecurity(sresult);
//		if (mAccessPointSecurity == SECURITY_NONE) {
//			config.allowedKeyManagement.set(KeyMgmt.NONE);
//		} else {
//			int length = mPassword.length();
//			if (StringUtils.isEmptyObj(mPassword)) {
//				onTestFail(R.string.wifi_err_pwd_unspecified);
//				return null;
//			}
//			switch (mAccessPointSecurity) {
//			case SECURITY_WEP:
//				config.allowedKeyManagement.set(KeyMgmt.NONE);
//				config.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
//				config.allowedAuthAlgorithms.set(AuthAlgorithm.SHARED);
//				// WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
//				if ((length == 10 || length == 26 || length == 58)
//						&& mPassword.matches("[0-9A-Fa-f]*")) {
//					config.wepKeys[0] = mPassword;
//				} else {
//					config.wepKeys[0] = '"' + mPassword + '"';
//				}
//				break;
//
//			case SECURITY_PSK:
//				config.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
//				if (mPassword.matches("[0-9A-Fa-f]{64}")) {
//					config.preSharedKey = mPassword;
//				} else {
//					config.preSharedKey = '"' + mPassword + '"';
//				}
//				break;
//
//			case SECURITY_EAP:
//				onTestFail(R.string.wifi_err_pwd_unsupport);
//				return null;
//			default:
//				return null;
//			}
//		}
//		config.proxySettings = ProxySettings.UNASSIGNED;
//		config.ipAssignment = IpAssignment.UNASSIGNED;
//		config.linkProperties = new LinkProperties();
//		return config;
//	}
//
//	static String convertToQuotedString(String string) {
//		return "\"" + string + "\"";
//	}
//
//	static int getSecurity(ScanResult result) {
//		if (result.capabilities.contains("WEP")) {
//			return SECURITY_WEP;
//		} else if (result.capabilities.contains("PSK")) {
//			return SECURITY_PSK;
//		} else if (result.capabilities.contains("EAP")) {
//			return SECURITY_EAP;
//		}
//		return SECURITY_NONE;
//	}
}
