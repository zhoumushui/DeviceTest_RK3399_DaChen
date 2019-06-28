package com.rockchip.devicetest.utils;

import java.net.Inet4Address;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.net.EthernetManager;

import android.net.IpConfiguration;
import android.net.IpConfiguration.IpAssignment;
import android.net.IpConfiguration.ProxySettings;
import android.net.NetworkUtils;
import android.net.LinkAddress;
import java.net.InetAddress;
import android.net.StaticIpConfiguration;

public class NetworkUtil {
	public static boolean isIPAddress(String ipaddr) {
		boolean flag = false;
		Pattern pattern = Pattern.compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
		Matcher m = pattern.matcher(ipaddr);
		flag = m.matches();
		return flag;
	}

	public static boolean isEthAvailable(EthernetManager ethManager)
	{
		if(ethManager == null) return false;

		return ethManager.isAvailable();
	}
	public static void setEthernetEnabled(EthernetManager ethManager,boolean enabled){
		if(ethManager == null) return;
		
		
	}

	public static boolean ethUseStaticIP(EthernetManager ethManager)
	{
		if(ethManager == null) return false;
		boolean useStatic=(ethManager.getConfiguration().ipAssignment == IpAssignment.STATIC) ? true : false;
		return useStatic;
	}

	public static boolean ethUseDhcpIP(EthernetManager ethManager)
	{
		if(ethManager == null) return false;
		boolean useDhcp=(ethManager.getConfiguration().ipAssignment == IpAssignment.DHCP) ? true : false;
		return useDhcp;
	}


	public static void setEthModeDHCP(EthernetManager ethManager)
	{
		if(ethManager == null) return;
		ethManager.setConfiguration(new IpConfiguration(IpAssignment.DHCP, ProxySettings.NONE,null,null));
	}

	public static void setEthModeStaticIp(EthernetManager ethManager)
	{
		setEthModeStaticIp(ethManager, null);
	}
	public static void setEthModeStaticIp(EthernetManager ethManager,StaticIpConfiguration mStaticIpConfiguration)
	{
		if(ethManager == null) return;
		ethManager.setConfiguration(new IpConfiguration(IpAssignment.STATIC, ProxySettings.NONE,mStaticIpConfiguration,null));
	}

	public static  StaticIpConfiguration setEthStaticIpConfiguration(String mEthIpAddress,String mEthNetmask,String mEthGateway,String mEthdns1,String mEthdns2) {

		StaticIpConfiguration mStaticIpConfiguration =new StaticIpConfiguration();
		/*
		 * get ip address, netmask,dns ,gw etc.
		 */	 
		Inet4Address inetAddr = getIPv4Address(mEthIpAddress);
		int prefixLength = maskStr2InetMask(mEthNetmask); 
		InetAddress gatewayAddr =getIPv4Address(mEthGateway); 
		InetAddress dnsAddr = getIPv4Address(mEthdns1);

		if (inetAddr.getAddress().toString().isEmpty() || prefixLength ==0 || gatewayAddr.toString().isEmpty()
				|| dnsAddr.toString().isEmpty()) {
			return null;
		}

		String dnsStr2=mEthdns2;  
		mStaticIpConfiguration.ipAddress = new LinkAddress(inetAddr, prefixLength);
		mStaticIpConfiguration.gateway=gatewayAddr;
		mStaticIpConfiguration.dnsServers.add(dnsAddr);

		if (!dnsStr2.isEmpty()) {
			mStaticIpConfiguration.dnsServers.add(getIPv4Address(dnsStr2));
		} 
		return mStaticIpConfiguration;
	}
    private static Inet4Address getIPv4Address(String text) {
        try {
            return (Inet4Address) NetworkUtils.numericToInetAddress(text);
        } catch (IllegalArgumentException|ClassCastException e) {
            return null;
        }
    }
    public static  int maskStr2InetMask(String maskStr) {
    	StringBuffer sb ;
    	String str;
    	int inetmask = 0; 
    	int count = 0;
    	/*
    	 * check the subMask format
    	 */
      	Pattern pattern = Pattern.compile("(^((\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])\\.){3}(\\d|[01]?\\d\\d|2[0-4]\\d|25[0-5])$)|^(\\d|[1-2]\\d|3[0-2])$");
    	if (pattern.matcher(maskStr).matches() == false) {
    		return 0;
    	}
    	
    	String[] ipSegment = maskStr.split("\\.");
    	for(int n =0; n<ipSegment.length;n++) {
    		sb = new StringBuffer(Integer.toBinaryString(Integer.parseInt(ipSegment[n])));
    		str = sb.reverse().toString();
    		count=0;
    		for(int i=0; i<str.length();i++) {
    			i=str.indexOf("1",i);
    			if(i==-1)  
    				break;
    			count++;
    		}
    		inetmask+=count;
    	}
    	return inetmask;
    }
    
  //将子网掩码转换成ip子网掩码形式，比如输入32输出为255.255.255.255  
    public static String interMask2String(int prefixLength) {
        String netMask = null;
		int inetMask = prefixLength;
		
		int part = inetMask / 8;
		int remainder = inetMask % 8;
		int sum = 0;
		
		for (int i = 8; i > 8 - remainder; i--) {
			sum = sum + (int) Math.pow(2, i - 1);
		}
		
		if (part == 0) {
			netMask = sum + ".0.0.0";
		} else if (part == 1) {
			netMask = "255." + sum + ".0.0";
		} else if (part == 2) {
			netMask = "255.255." + sum + ".0";
		} else if (part == 3) {
			netMask = "255.255.255." + sum;
		} else if (part == 4) {
			netMask = "255.255.255.255";
		}

		return netMask;
	}

}
