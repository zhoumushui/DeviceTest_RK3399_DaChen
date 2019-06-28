package com.rockchip.devicetest.networktest;

public class ResultInfo{
	boolean testDone;
	String result;
	boolean success;
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public void setTestDone(boolean testDone) {
		this.testDone = testDone;
	}
	public void setResultInfo(boolean success ,String result ,boolean testDone) {
		this.success = success;
		this.result = result;
		this.testDone = testDone;
	}
	@Override
	public String toString() {
		return "ResultInfo [testDone=" + testDone + ", result=" + result
				+ ", success=" + success + "]";
	}
	
}
