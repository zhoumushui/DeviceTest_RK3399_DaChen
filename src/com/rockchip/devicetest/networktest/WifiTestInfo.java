package com.rockchip.devicetest.networktest;

public class WifiTestInfo {
	private String mSpecifiedAp;
	private String mPassword;
	private int mStartSignalLevel;
	private int mEndSignalLevel;
	private boolean mNeedConnectAp;
	public String getSpecifiedAp() {
		return mSpecifiedAp;
	}
	public void setSpecifiedAp(String mSpecifiedAp) {
		this.mSpecifiedAp = mSpecifiedAp;
	}
	public String getPassword() {
		return mPassword;
	}
	public void setPassword(String mPassword) {
		this.mPassword = mPassword;
	}
	public int getStartSignalLevel() {
		return mStartSignalLevel;
	}
	public void setStartSignalLevel(int mStartSignalLevel) {
		this.mStartSignalLevel = mStartSignalLevel;
	}
	public int getEndSignalLevel() {
		return mEndSignalLevel;
	}
	public void setEndSignalLevel(int mEndSignalLevel) {
		this.mEndSignalLevel = mEndSignalLevel;
	}
	public boolean isNeedConnectAp() {
		return mNeedConnectAp;
	}
	public void setNeedConnectAp(boolean mNeedConnectAp) {
		this.mNeedConnectAp = mNeedConnectAp;
	} 
	
	
}
