/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月12日 下午2:08:44  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月12日      fxw         1.0         create
 *******************************************************************/

package com.rockchip.devicetest.testcase.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.EthernetManager;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemProperties;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.NetworkUtil;
import com.rockchip.devicetest.utils.StringUtils;

import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import com.rockchip.devicetest.utils.SystemUtils;
import android.net.wifi.WifiManager;
import android.net.IpConfiguration;
import java.net.InetAddress;
import android.net.NetworkUtils;
import android.net.LinkAddress;
import android.net.StaticIpConfiguration;

public class LanTest extends BaseTestCase {

	/*LAN测试超时时间30s！*/
	private static final int LAN_TEST_TIMEOUT = 30000;
	/*刚切换成以太网时网速可能未达峰值，增加此延时操作！*/
	private static final int LAN_TEST_GET_SPEED_TIME_DELAY = 3000;
	private EthernetManager mEthManager;
	private WifiManager mWifiManager;
	private boolean hasRegister;

	private static final String TAG = "LanTest";
	private static final boolean DEBUG = true;
	private String mConnectMode;

	private String mEthIpAddress;
	private String mEthNetmask;
	private String mEthGateway;
	private String mEthdns1;
	private String mEthdns2;
	private String mIsSpeed1000;
	
	StaticIpConfiguration mStaticIpConfiguration;
	
	private final static String nullIpInfo = "0.0.0.0";


	private String[] mSettingValues;

	private ContentResolver cr;

	public LanTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		mEthManager = (EthernetManager) mContext
				.getSystemService(Context.ETHERNET_SERVICE);
	        
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		
		cr = mContext.getContentResolver();
	}

	private void LOGV(String msg) {
		if (DEBUG)
			Log.v(TAG, msg);
	}

	@Override
	public boolean onTesting() {
		if(mEthManager == null) {
			onTestFail(R.string.pub_test_no_exist);
			return super.onTesting();
		}
		if (mWifiManager !=null && mWifiManager.isWifiEnabled()) {
                        mWifiManager.setWifiEnabled(false);
                }
//		IntentFilter ifilter = new IntentFilter(
//				EthernetDataTracker.ETHERNET_STATE_CHANGED_ACTION);
		IntentFilter ifilter = new IntentFilter(EthernetManager.ETHERNET_STATE_CHANGED_ACTION);
		mContext.registerReceiver(mEthernetReceiver, ifilter);
		hasRegister = true;
		setTestTimeout(LAN_TEST_TIMEOUT);

		if (NetworkUtil.isEthAvailable(mEthManager)) {// 以太网已开启
			boolean mConnect = (mEthManager.getEthernetConnectState() == EthernetManager.ETHER_STATE_CONNECTED);
			if (!mConnect) {// waiting
				// onTestFail(R.string.lan_err_disconnect);
				// return;
				//mEthManager.setEthernetEnabled(true);
			} else {
				testEthernet();
			}
		} else {
			//mEthManager.setInterfaceEnable("eth0",true);  //zouxf
        	//mEthManager.setEthernetEnabled(true);
		}
		return true;
	}

	public boolean onTestHandled(TestResult result) {
		if (hasRegister) {
			hasRegister = false;
			mContext.unregisterReceiver(mEthernetReceiver);
		}
		SystemUtils.setEthEnable(mContext,true);
		return super.onTestHandled(result);
	}

	public void stop() {
		if (hasRegister) {
			hasRegister = false;
			mContext.unregisterReceiver(mEthernetReceiver);
		}
		super.stop();
	}

	/**
	 * 测试以太网
	 */
	public void testEthernet() {
		/*刚切换成以太网时网速可能未达峰值，增加此延时操作！*/
		try {
			Thread.sleep(LAN_TEST_GET_SPEED_TIME_DELAY);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*根据配置文件设置动态／静态模式，并设置相关参数*/
		saveIpSettingsInfo();

		LOGV("speed--->" + getEthernetSpeed());

		//int useStaticIp = System.getInt(cr, System.ETHERNET_USE_STATIC_IP,
		//		0);
		int useStaticIp = isStaticIp();
		LOGV("useStaticIp--->" + useStaticIp);
		String ipaddress = null;
		if (useStaticIp == 1) {
			getEthInfoFromStaticIp();
			//ipaddress = System.getString(cr, System.ETHERNET_STATIC_IP);
			ipaddress = mEthIpAddress;
			ipaddress += "(static)";
			LOGV("static ip--->" + ipaddress);
		} else {
			getEthInfoFromDhcp();
			ipaddress = mEthIpAddress;
		}
		if (StringUtils.isEmptyObj(ipaddress)) {
			onTestFail(R.string.lan_err_ip);
		} else {
			String ethernetSpeed = mContext
					.getString(R.string.ethernet_speed)
					+ getEthernetSpeed() + "Mb/s,";
			String ipdetail = mContext.getString(R.string.lan_ip_address,
					ipaddress);
			boolean speed1000 = "1000".equals(getEthernetSpeed().trim());
			// 测试成功后关闭静态IP测试
			//if ("static".equals(mConnectMode.trim())) {
				//mEthManager.setEthernetEnabled(false);
        		//mEthManager.setInterfaceEnable("eth0",false);  //zouxf
			//}
			if(speed1000 
					|| !ParamConstants.TRUE.equals(mIsSpeed1000)) {
				onTestSuccess(ethernetSpeed + ipdetail);
			} else {
				onTestFail(ethernetSpeed + getString(R.string.ethernet_speed_no_1000));
			}
		}

	}
	
	private int isStaticIp(){
		IpConfiguration ipconfig = mEthManager.getConfiguration();
		if(ipconfig==null) return 0 ;
		if (ipconfig.getIpAssignment()==IpConfiguration.IpAssignment.STATIC) {
            return 1 ;
        } else {
        	return 0 ;
        }
	}
	
	public void getEthInfoFromStaticIp() {
		StaticIpConfiguration staticIpConfiguration=mEthManager.getConfiguration().getStaticIpConfiguration();

		if(staticIpConfiguration == null) {
			return ;
		}
		LinkAddress ipAddress = staticIpConfiguration.ipAddress;
		InetAddress gateway   = staticIpConfiguration.gateway;
		ArrayList<InetAddress> dnsServers=staticIpConfiguration.dnsServers;

		if( ipAddress !=null) {
			mEthIpAddress=ipAddress.getAddress().getHostAddress();
			mEthNetmask=NetworkUtil.interMask2String(ipAddress.getPrefixLength());
		}
		if(gateway !=null) {
			mEthGateway=gateway.getHostAddress();
		}
		mEthdns1=dnsServers.get(0).getHostAddress();

		if(dnsServers.size() > 1) { /* 只保留两个*/
			mEthdns2=dnsServers.get(1).getHostAddress();
		}		
	}

	public void getEthInfoFromDhcp(){	
		String tempIpInfo;
		String iface = "eth0";
			
		tempIpInfo = SystemProperties.get("dhcp."+ iface +".ipaddress");

		if ((tempIpInfo != null) && (!tempIpInfo.equals("")) ){ 
			mEthIpAddress = tempIpInfo;
	    	} else {  
	    		mEthIpAddress = nullIpInfo;
	    	}
					
		tempIpInfo = SystemProperties.get("dhcp."+ iface +".mask");	
		if ((tempIpInfo != null) && (!tempIpInfo.equals("")) ){
	            mEthNetmask = tempIpInfo;
	    	} else {           		
	    		mEthNetmask = nullIpInfo;
	    	}
						
		tempIpInfo = SystemProperties.get("dhcp."+ iface +".gateway");	
		if ((tempIpInfo != null) && (!tempIpInfo.equals(""))){
	        	mEthGateway = tempIpInfo;
	    	} else {
	    		mEthGateway = nullIpInfo;        		
	    	}

		tempIpInfo = SystemProperties.get("dhcp."+ iface +".dns1");
		if ((tempIpInfo != null) && (!tempIpInfo.equals(""))){
	       		mEthdns1 = tempIpInfo;
	    	} else {
	    		mEthdns1 = nullIpInfo;      		
	    	}

		tempIpInfo = SystemProperties.get("dhcp."+ iface +".dns2");
		if ((tempIpInfo != null) && (!tempIpInfo.equals(""))){
	       		mEthdns2 = tempIpInfo;
	    	} else {
	    		mEthdns2 = nullIpInfo;       		
	    	}
	    }
//	public String getEthInfoFromDhcp() {
//		String tempIpInfo;
//		String mEthIpAddress;
//		String iface = mEthManager.getEthernetIfaceName();
//
//		tempIpInfo = SystemProperties.get("dhcp." + iface + ".ipaddress");
//		if ((tempIpInfo != null) && (!tempIpInfo.equals(""))) {
//			mEthIpAddress = tempIpInfo;
//		} else {
//			mEthIpAddress = "";
//		}
//		return mEthIpAddress;
//	}

	private void saveIpSettingsInfo() {

		Map<String, String> attachParams = mTestCaseInfo.getAttachParams();
		mConnectMode = attachParams.get(ParamConstants.CONNECT_MODE);
		mEthIpAddress = attachParams.get(ParamConstants.STATIC_IP);
		mEthGateway = attachParams.get(ParamConstants.GATEWAY);
		mEthNetmask = attachParams.get(ParamConstants.NETMASK);
		mEthdns1 = attachParams.get(ParamConstants.DNS1);
		mEthdns2 = attachParams.get(ParamConstants.DNS2);

		LOGV("mConnectMode--->" + mConnectMode + "\n");
		LOGV("mEthIpAddress--->" + mEthIpAddress + "\n");
		LOGV("mEthGateway--->" + mEthGateway + "\n");
		LOGV("mEthNetmask--->" + mEthNetmask + "\n");
		LOGV("mEthdns1--->" + mEthdns1 + "\n");
		LOGV("mEthdns2--->" + mEthdns2 + "\n");

		/*根据mIsSpeed1000决定是否以千兆网为门槛作结果判定,
		 * 使用mIsSpeed1000参数的代码注意在此函数调用之后调用！*/
		mIsSpeed1000 = attachParams.get(ParamConstants.IS_SPEED_1000);
		LOGV("mIsSpeed1000--->" + mIsSpeed1000 + "\n");

		mSettingValues = new String[] { mEthIpAddress, mEthGateway,
				mEthNetmask, mEthdns1, mEthdns2 };

		// IP设置是否完全、有效
		if (!isIpDataInUiComplete()) {
			LogUtil.show(mContext, getString(R.string.pub_save_failed));
			return;
		}

		mStaticIpConfiguration = NetworkUtil.setEthStaticIpConfiguration(mEthIpAddress, mEthNetmask, mEthGateway, mEthdns1, mEthdns2);
		


		/* 保存 关键的 "是否使用静态 IP" 的配置. */
		//mConnectMode default is auto
		if(mConnectMode == null || mConnectMode.length()==0)
		{
			NetworkUtil.setEthModeDHCP(mEthManager);
		} else if ("static".equals(mConnectMode.trim())) {
			NetworkUtil.setEthModeStaticIp(mEthManager,mStaticIpConfiguration);
		} else if ("auto".equals(mConnectMode.trim())) {
			NetworkUtil.setEthModeDHCP(mEthManager);
		}
	}

	// ip地址是否完整、有效
	private boolean isIpDataInUiComplete() {

		/* 遍历 "mPreferenceKeys" 中除了 dns2 以外的 元素, ... */
		for (int i = 0; i < mSettingValues.length - 1; i++) {
			String text = mSettingValues[i];
			LOGV(mSettingValues[i] + "\n");
			/* 若当前 IP 参数 为 null 或者 为 空字串, 则 ... */
			if (null == text || TextUtils.isEmpty(text)) {
				/* 返回否定结果. */
				return false;
			} else if (!isValidIpAddress(text)) {
				return false;
			}
		}
		/* 返回肯定. */
		return true;
	}

	// ip 地址是否有效
	private boolean isValidIpAddress(String value) {

		int start = 0;
		int end = value.indexOf('.');
		int numBlocks = 0;

		while (start < value.length()) {

			if (-1 == end) {
				end = value.length();
			}

			try {
				int block = Integer.parseInt(value.substring(start, end));
				if ((block > 255) || (block < 0)) {
					Log.w(TAG, "isValidIpAddress() : invalid 'block', block = "
							+ block);
					return false;
				}
			} catch (NumberFormatException e) {
				Log.w(TAG, "isValidIpAddress() : e = " + e);
				return false;
			}

			numBlocks++;

			start = end + 1;
			end = value.indexOf('.', start);
		}

		return numBlocks == 4;
	}

	private String getEthernetSpeed() {
		String temp = "";
		String filePath = "/sys/class/net/eth0/speed";
		try {
			File file = new File(filePath);
			LOGV("filePath--->" + file.getPath());
			if (!file.exists()) {
				// temp = "no file";
			}
			FileInputStream fin = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fin);
			BufferedReader br = new BufferedReader(isr);
			String t = "";
			while ((t = br.readLine()) != null) {

				temp += t;
				LOGV("temp=" + temp);
			}
			br.close();
			isr.close();
			fin.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return temp;
	}

	private final BroadcastReceiver mEthernetReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action
					.equals(EthernetManager.ETHERNET_STATE_CHANGED_ACTION)) {
				int ethernetState = intent.getIntExtra(
						EthernetManager.EXTRA_ETHERNET_STATE, 0);
				if (ethernetState == EthernetManager.ETHER_STATE_CONNECTED) {
					testEthernet();
				}
			}
		}
	};
}
