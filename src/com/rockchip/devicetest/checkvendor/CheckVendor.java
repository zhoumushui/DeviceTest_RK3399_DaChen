package com.rockchip.devicetest.checkvendor;

public class CheckVendor {
	static {
		System.loadLibrary("drm_devicetest");
	}
	
	public native int check();
}
