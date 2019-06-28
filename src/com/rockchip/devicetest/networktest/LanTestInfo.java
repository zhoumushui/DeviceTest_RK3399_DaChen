package com.rockchip.devicetest.networktest;

import com.rockchip.devicetest.utils.NetworkUtil;

public class LanTestInfo {

	private String mConnectMode;
	private String mEthIpAddress;
	private String mEthNetmask;
	private String mEthGateway;
	private String mEthdns1;
	private String mEthdns2;
	private boolean mIsSpeed1000;
	private final static String nullIpInfo = "0.0.0.0";
	@Override
	public String toString() {
		return "LanTestInfo [mConnectMode=" + mConnectMode + ", mEthIpAddress="
				+ mEthIpAddress + ", mEthNetmask=" + mEthNetmask
				+ ", mEthGateway=" + mEthGateway + ", mEthdns1=" + mEthdns1
				+ ", mEthdns2=" + mEthdns2 + ", mIsSpeed1000=" + mIsSpeed1000
				+ "]";
	}
	public LanTestInfo(String mConnectMode, String mEthIpAddress,
			String mEthNetmask, String mEthGateway, String mEthdns1,
			String mEthdns2, boolean mIsSpeed1000) {
		super();
		this.mConnectMode = mConnectMode;
		this.mEthIpAddress = mEthIpAddress;
		this.mEthNetmask = mEthNetmask;
		this.mEthGateway = mEthGateway;
		this.mEthdns1 = mEthdns1;
		this.mEthdns2 = mEthdns2;
		this.mIsSpeed1000 = mIsSpeed1000;
	}
	public String getConnectMode() {
		return mConnectMode;
	}
	public boolean isConnectStatic()
	{
		return "static".equals(mConnectMode);
	}
	public void setConnectMode(String mConnectMode) {
		if(mConnectMode == null) mConnectMode = nullIpInfo;
		else this.mConnectMode = mConnectMode;
	}
	public String getEthIpAddress() {
		return mEthIpAddress;
	}
	public void setEthIpAddress(String mEthIpAddress) {
		if(mEthIpAddress == null) mEthIpAddress = nullIpInfo;
		else this.mEthIpAddress = mEthIpAddress;
	}
	public String getEthNetmask() {
		return mEthNetmask;
	}
	public void setEthNetmask(String mEthNetmask) {
		if(mEthNetmask == null) mEthNetmask = nullIpInfo;
		else this.mEthNetmask = mEthNetmask;
	}
	public String getEthGateway() {
		return mEthGateway;
	}
	public void setEthGateway(String mEthGateway) {
		if(mEthGateway == null) mEthGateway = nullIpInfo;
		else this.mEthGateway = mEthGateway;
	}
	public String getEthdns1() {
		return mEthdns1;
	}
	public void setEthdns1(String mEthdns1) {
		if(mEthdns1 == null) mEthdns1 = nullIpInfo;
		else this.mEthdns1 = mEthdns1;
	}
	public String getEthdns2() {
		return mEthdns2;
	}
	public void setEthdns2(String mEthdns2) {
		if(mEthdns2 == null) mEthdns2 = nullIpInfo;
		else this.mEthdns2 = mEthdns2;
	}
	public boolean testSpeed1000() {
		return mIsSpeed1000;
	}
	public void setIsSpeed1000(boolean mIsSpeed1000) {
		this.mIsSpeed1000 = mIsSpeed1000;
	}
	
	public boolean isVaildLanInfo()
	{
		return NetworkUtil.isIPAddress(mEthIpAddress)
				&& NetworkUtil.isIPAddress(mEthNetmask)
				&& NetworkUtil.isIPAddress(mEthGateway)
				&& NetworkUtil.isIPAddress(mEthdns1)
				&& NetworkUtil.isIPAddress(mEthdns2);
	}
	
	

}
