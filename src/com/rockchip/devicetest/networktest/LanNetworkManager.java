package com.rockchip.devicetest.networktest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.ParamConstants;

import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.NetworkUtil;
import com.rockchip.devicetest.utils.StringUtils;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.net.EthernetManager;
import android.provider.Settings.System;
import android.util.Log;
import android.net.IpConfiguration;
import android.net.NetworkUtils;
import android.net.LinkAddress;
import android.net.StaticIpConfiguration;
import android.os.SystemProperties;

public class LanNetworkManager extends BaseNetworkManager{



	private static final int MSG_DELAY_TEST = 0;
	private static final int LAN_TEST_GET_SPEED_TIME_DELAY = 3000;
	private static final int LAN_TEST_TIMEOUT = 30000;
	
	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_DELAY_TEST:
				startTestEthernet();
				break;
			default:
				break;
			}

		}

	};
	private LanTestInfo mLanTestInfo;


	public LanNetworkManager(Context context, LanTestInfo mLanTestInfo) {
		super(context);
		// TODO Auto-generated constructor stub
		this.mLanTestInfo = mLanTestInfo;
		LogUtil.v(LanNetworkManager.this, "mLanTestInfo:"+mLanTestInfo.toString());
		if (mWifiManager !=null && mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}
	@Override
	public void startConnectTest()
	{
		super.startConnectTest();
		setConnectTestTimeOut(LAN_TEST_TIMEOUT);
		LogUtil.d(LanNetworkManager.this, "startConnectTest");
		boolean  ethEnabler =NetworkUtil.isEthAvailable(mEthManager);
		LogUtil.d(LanNetworkManager.this, "startConnectTest ethEnabler:"+ethEnabler);
		if (ethEnabler) {// 以太网已开启
			boolean mConnect = (mEthManager.getEthernetConnectState() == EthernetManager.ETHER_STATE_CONNECTED);
			if (!mConnect) {// waiting
				//mEthManager.setEthernetEnabled(true);
			} else {
				//刚切换成以太网时网速可能未达峰值，增加此延时操作！
				delayStartTestEth();
			}
		} else {
			//mEthManager.setInterfaceEnable("eth0",true);  //zouxf
			//mEthManager.setEthernetEnabled(true);
		}
	}

	
	


	@Override
	public void endConnectTest() {
		// TODO Auto-generated method stub
		super.endConnectTest();
	}
	@Override
	public void registerReceiver() {
		// TODO Auto-generated method stub
		super.registerReceiver();
		if(hasRegistReceiver)return;	
		IntentFilter ifilter = new IntentFilter(EthernetManager.ETHERNET_STATE_CHANGED_ACTION);
		mContext.registerReceiver(mEthernetReceiver, ifilter);
		hasRegistReceiver = true;
	}




	@Override
	public void unRegisterReceiver() {
		// TODO Auto-generated method stub
		super.unRegisterReceiver();
		if(!hasRegistReceiver)return;
		mContext.unregisterReceiver(mEthernetReceiver);
		hasRegistReceiver = false;
	}



	private void delayStartTestEth()
	{
		mHandler.removeMessages(MSG_DELAY_TEST);
		mHandler.sendEmptyMessageDelayed(MSG_DELAY_TEST, LAN_TEST_GET_SPEED_TIME_DELAY);
	}
	public void startTestEthernet() {
		LogUtil.d(LanNetworkManager.this, "startTestEthernet");
		/*	if(mLanTestInfo.isConnectStatic())
		{
			if(mLanTestInfo.isVaildLanInfo())
			{
				System.putInt(mContext.getContentResolver(), System.ETHERNET_STATIC_IP, mLanTestInfo.getEthIpAddress());
				System.putInt(mContext.getContentResolver(), System.ETHERNET_STATIC_GATEWAY, mLanTestInfo.getEthGateway());
				System.putInt(mContext.getContentResolver(), System.ETHERNET_STATIC_NETMASK, mLanTestInfo.getEthNetmask());
				System.putInt(mContext.getContentResolver(), System.ETHERNET_STATIC_DNS1, mLanTestInfo.getEthdns1());
				System.putInt(mContext.getContentResolver(), System.ETHERNET_STATIC_DNS2, mLanTestInfo.getEthdns2());
				System.putInt(mContext.getContentResolver(), System.ETHERNET_USE_STATIC_IP, 1);
			}else{
				System.putInt(mContext.getContentResolver(), System.ETHERNET_USE_STATIC_IP, 0);
				mConnectResultInfo.setTestDone(true);
				mConnectResultInfo.setSuccess(false);
				mConnectResultInfo.setResult("set static ip,but laninfo is invild");
				LogUtil.v(LanNetworkManager.class, "set static ip,but laninfo is invild");

			}
		}else{
			System.putInt(mContext.getContentResolver(), System.ETHERNET_USE_STATIC_IP, 0);

		}
		 */
		String mEthIpAddress =null;
		if ( isStaticIp()) {
			mEthIpAddress = getEthIp();
			mEthIpAddress += "(static)";
		} else {
			mEthIpAddress = getEthIpFromDhcp();
		}
		
		Log.v("sjf", "isStaticIp():"+isStaticIp()+",mEthIpAddress:"+mEthIpAddress);
		if(!NetworkUtil.isIPAddress(mEthIpAddress))
		{
			mConnectResultInfo.setSuccess(false);
			mConnectResultInfo.setResult(mContext.getString(R.string.lan_err_ip));
		}else{
			String ipdetail = mContext.getString(R.string.lan_ip_address,
					mEthIpAddress);
			if(mLanTestInfo.testSpeed1000())
			{
				boolean speed1000 = "1000".equals(getEthernetSpeed().trim());
				if(speed1000)
				{
					mConnectResultInfo.setSuccess(true);
					mConnectResultInfo.setResult(mContext
							.getString(R.string.ethernet_speed)
							+ getEthernetSpeed() + "Mb/s,"+ipdetail);

				}else{
					mConnectResultInfo.setSuccess(true);
					mConnectResultInfo.setResult(mContext.getString(R.string.ethernet_speed_no_1000)+ ","+ipdetail);
				}
			}else{
				mConnectResultInfo.setSuccess(true);
				mConnectResultInfo.setResult(ipdetail);
			}
		}
		mConnectResultInfo.setTestDone(true);
		endConnectTest();
}

	private boolean isStaticIp(){
		IpConfiguration ipconfig = mEthManager.getConfiguration();
		if(ipconfig==null) return false ;
		if (ipconfig.getIpAssignment()==IpConfiguration.IpAssignment.STATIC) {
			return true ;
		} else {
			return false;
		}
	}

	private  String getEthIp() {
		String mEthIpAddress = "0.0.0.0";
		try{
			IpConfiguration ipconfig = mEthManager.getConfiguration();
			StaticIpConfiguration sic = ipconfig.getStaticIpConfiguration();
			mEthIpAddress = sic.ipAddress.getAddress().getHostAddress();
		}catch(Exception ex){
		}
		return mEthIpAddress;
	}

	public String getEthIpFromDhcp(){	
		String iface = "eth0";
		Log.v("sjf","Build.VERSION.SDK_INT:"+Build.VERSION.SDK_INT);
		String mEthIpAddress;
		if(Build.VERSION.SDK_INT  >= Build.VERSION_CODES.N)
		{
			mEthIpAddress = mEthManager.getIpAddress();
		}else{
			mEthIpAddress = SystemProperties.get("dhcp."+ iface +".ipaddress");	
		}
		
		if ((mEthIpAddress == null) || (mEthIpAddress.length()==0) ){ 
			mEthIpAddress = "0.0.0.0";
		}
		return mEthIpAddress;


	}
	
	private String getEthernetSpeed() {
		String temp = "";
		String filePath = "/sys/class/net/eth0/speed";
		try {
			File file = new File(filePath);
			if (!file.exists()) {
				// temp = "no file";
			}
			FileInputStream fin = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fin);
			BufferedReader br = new BufferedReader(isr);
			String t = "";
			while ((t = br.readLine()) != null) {

				temp += t;
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
					delayStartTestEth();
				}
			}
		}
	};

}
