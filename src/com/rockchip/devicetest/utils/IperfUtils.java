package com.rockchip.devicetest.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.EthernetManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.util.Log;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.testcase.impl.WifiTest;
import com.rockchip.devicetest.utils.SystemUtils;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;

public class IperfUtils {
	private Context mContext;
	public  final static String IPERF_DIR_PATH =  "/data/data/com.rockchip.devicetest/test";
	public  final static String IPERF_FILE_PATH =  "/data/data/com.rockchip.devicetest/test/iperf";
	public  final static String IPERF_FILE_NAME =  "iperf";
	private  String mServerIP = "192.168.0.1";
	private  String mServerPort = "8000";
	private static final String IPERF_VERSION ="2.0.5";

	public static final int TYPE_TCP = 0;
	public static final int TYPE_UDP = 1;
	public static final int TYPE_UDP_B = 2;
	private EthernetManager mEthManager;
	private WifiManager mWifiManager;

	public IperfUtils(Context context) {
		super();
		// TODO Auto-generated constructor stub
		mContext = context;
		mEthManager = (EthernetManager) mContext
				.getSystemService(Context.ETHERNET_SERVICE);
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		initIperf();

	}

	public void initIperf() {

		File iperf = new File(IPERF_FILE_PATH);
		boolean needupdate = !IPERF_VERSION.equals(SharedPreferencesEdit.getInstance().getIperfVersion());
		
		if(!iperf.exists() || iperf.length() == 0 || needupdate)
		{
			if(needupdate)SharedPreferencesEdit.getInstance().setIperfVersion(IPERF_VERSION);
			FileUtils.copyFromAsset(mContext, IPERF_FILE_NAME, IPERF_DIR_PATH, true);
		}
	}
	public void setServerInfo(String mServerIP,String mServerPort)
	{
		this.mServerIP = mServerIP;
		this.mServerPort = mServerPort;
	}

	public String getTestCmd(int type)
	{
		String mCommand = null;
		switch (type) {
		case TYPE_TCP:
			mCommand = IPERF_FILE_NAME+" -c " + mServerIP + " -i 1 -w 1M  -f m -P 2 -p "
					+ mServerPort;
			break;
		case TYPE_UDP:
			mCommand = IPERF_FILE_NAME+" -c " + mServerIP
			+ " -i 1 -u -w 1M  -f m -p " + mServerPort;
			break;
		case TYPE_UDP_B:
			mCommand = IPERF_FILE_NAME+" -c " + mServerIP
			+ " -i 1 -u -w 1M -b 10M  -f m -p " + mServerPort;
			break;
		default:
			break;
		}

		return mCommand;
	}

	/***
	 * 检查命令合法性
	 * @param cmd
	 * @return
	 */
	public static boolean checkCmd(String cmd)
	{
		if(cmd != null && cmd.length() >0)
		{
			return cmd
					.matches("(iperf )?((-[s,-server])|(-[c,-client] ([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5]))|(-[c,-client] \\w{1,63})|(-[h,-help]))(( -[f,-format] [bBkKmMgG])|(\\s)|( -[l,-len] \\d{1,5}[KM])|( -[B,-bind] \\w{1,63})|( -[r,-tradeoff])|( -[v,-version])|( -[N,-nodelay])|( -[T,-ttl] \\d{1,8})|( -[U,-single_udp])|( -[d,-dualtest])|( -[w,-window] \\d{1,5}[KM])|( -[n,-num] \\d{1,10}[KM])|( -[p,-port] \\d{1,5})|( -[L,-listenport] \\d{1,5})|( -[t,-time] \\d{1,8})|( -[i,-interval] \\d{1,4})|( -[u,-udp])|( -[b,-bandwidth] \\d{1,20}[bBkKmMgG])|( -[m,-print_mss])|( -[P,-parallel] d{1,2})|( -[M,-mss] d{1,20}))*");
		}
		return false;
	}


	/*	*//**
	 * 获取自动测试组合，自动测试为测试lan和wifi的tcp各一次。会根据当前连的网络，来调整wifi和lan的测试顺序
	 * @return
	 *//*
	public ArrayList<String> getAutoTestCmd()
	{
		if(isNetworkConnected(mContext))
		{
			ArrayList<String> cmdList = new ArrayList<String>();


		}
		return null;
	}*/




	public static boolean isNetworkConnected(Context context) { 
		if (context != null) { 
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context 
					.getSystemService(Context.CONNECTIVITY_SERVICE); 
			NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo(); 
			if (mNetworkInfo != null) { 
				return mNetworkInfo.isAvailable(); 
			} 
		} 
		return false; 
	}
	
	public static boolean isEth(Context context) {   
		ConnectivityManager cm = (ConnectivityManager) context   
				.getSystemService(Context.CONNECTIVITY_SERVICE);   
		NetworkInfo networkINfo = cm.getActiveNetworkInfo();   
		if (networkINfo != null   
				&& networkINfo.getType() == ConnectivityManager.TYPE_ETHERNET) {   
			Log.v("IperfTest", "networkINfo.getType()="+networkINfo.getType());
			return true;   
		}   
		return false;   
	}

	public static boolean isWifi(Context context) {   
		ConnectivityManager cm = (ConnectivityManager) context   
				.getSystemService(Context.CONNECTIVITY_SERVICE);   
		NetworkInfo networkINfo = cm.getActiveNetworkInfo();   
		if (networkINfo != null   
				&& networkINfo.getType() == ConnectivityManager.TYPE_WIFI) {   
			Log.v("IperfTest", "networkINfo.getType()="+networkINfo.getType());
			return true;   
		}   
		return false;   
	}

	public  boolean checkConnectSsid(String ssid)
	{
		WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

		String mWifiSsid = null;
		if (wifiInfo != null && wifiInfo.getWifiSsid() != null) {
			String unicode = wifiInfo.getWifiSsid().toString();
			if (!WifiUtils.isEmpty(unicode)) {
				mWifiSsid = unicode;
			} else {
				mWifiSsid = wifiInfo.getWifiSsid().getHexString();
			}
		}
		return ssid.equals(mWifiSsid) ||WifiUtils.checkApName(ssid, mWifiSsid);
	}

	// 关闭wifi，开启以太网
	public void ethConnected() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
		
		//如果eth已经打开则不进行操作
		if(NetworkUtil.isEthAvailable(mEthManager)){
			//mEthManager.setEthernetEnabled(true);
		}else{
			//mEthManager.setInterfaceEnable("eth0",true); //zouxf
        	//mEthManager.setEthernetEnabled(true);
		}
	}
	
	public void wifiConnected() {
		
		
		if(NetworkUtil.isEthAvailable(mEthManager)){
			//mEthManager.setEthernetEnabled(false);
        	//mEthManager.setInterfaceEnable("eth0",false); //zouxf
		}
		if(!mWifiManager.isWifiEnabled())//如果wifi已经打开则不进行操作
			mWifiManager.setWifiEnabled(true);		
		
		
		
	}
	


	public IperfPack parseIperfMsg(String msg)
	{
		if(msg != null && msg.length() >0)
		{
			String[] values = matcher(msg);
			if(values.length == 5)
			{
				IperfPack p = new IperfPack();
				p.setStartTime(Float.valueOf(values[1]));
				p.setEndTime(Float.valueOf(values[2]));
				p.setTransfer(Float.valueOf(values[3]));
				p.setBandwidth(Float.valueOf(values[4]));

				return p;

			}
		}
		return null;
	}

	private static Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");

	private static String[] matcher(String input) {
		Matcher matcher = pattern.matcher(input);
		List<String> list = new ArrayList();
		while (matcher.find()) {
			list.add(matcher.group());
		}

		return list.toArray(new String[0]);
	}

	public class IperfPack {
		float startTime;
		float endTime;
		float transfer;
		float bandwidth;

		public IperfPack() {
			super();
			// TODO Auto-generated constructor stub
		}

		public IperfPack(float startTime, float endTime, float transfer,
				float bandwidth) {
			super();
			this.startTime = startTime;
			this.endTime = endTime;
			this.transfer = transfer;
			this.bandwidth = bandwidth;
		}



		public float getStartTime() {
			return startTime;
		}

		public void setStartTime(float startTime) {
			this.startTime = startTime;
		}

		public float getEndTime() {
			return endTime;
		}

		public void setEndTime(float endTime) {
			this.endTime = endTime;
		}

		public float getTransfer() {
			return transfer;
		}

		public void setTransfer(float transfer) {
			this.transfer = transfer;
		}

		public float getBandwidth() {
			return bandwidth;
		}

		public void setBandwidth(float bandwidth) {
			this.bandwidth = bandwidth;
		}

		@Override
		public String toString() {
			return "packtest [startTime=" + startTime + ", endTime=" + endTime
					+ ", transfer=" + transfer + ", bandwidth=" + bandwidth
					+ "]";
		}

	}
}
