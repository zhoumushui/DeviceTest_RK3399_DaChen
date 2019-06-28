package com.rockchip.devicetest.testcase.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.rockchip.devicetest.checkvendor.CheckVendor;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.LogUtil;

public class DeviceChkTest extends BaseTestCase {
	private static final String TAG = "DeviceChkTest";
	public DeviceChkTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		// TODO Auto-generated constructor stub
		initDevice();
	}
	
	@Override
    public boolean onTestHandled(TestResult result) {
        return super.onTestHandled(result);
	}

	public  final static String DEVICECHK_DIR_PATH =  "/data/data/com.rockchip.devicetest/test";
	public  final static String DEVICECHK_FILE_PATH =  "/data/data/com.rockchip.devicetest/test/devicechk";
	public  final static String DEVICECHK_FILE_NAME =  "devicechk";
	
	public void initDevice() {

		File iperf = new File(DEVICECHK_FILE_PATH);
		
		if(!iperf.exists() || iperf.length() == 0 )
		{
			FileUtils.copyFromAsset(mContext, DEVICECHK_FILE_NAME, DEVICECHK_DIR_PATH, true);
		}
	}


	@Override
	public boolean onTesting() {
		// TODO Auto-generated method stub
		CheckVendor mCheckVendor = new CheckVendor();
		int check = mCheckVendor.check();
		LogUtil.d(DeviceChkTest.this, "chek:"+check);
		if(check < 0)
		{
			onTestFail(0);
		}else
		{
			onTestSuccess();
		}
//		String result = execCmd(DEVICECHK_FILE_PATH);
//		Log.d(TAG, "run devicechk result:"+result);
//		if(result == null) onTestFail(0);
//		else{
//			int value = -1;
//			try {
//				value = Integer.valueOf(result.trim());
//			} catch (Exception e) {
//				// TODO: handle exception
//			}
//			
//			if(value < 0)
//			{
//				onTestFail(0);
//			}else
//			{
//				onTestSuccess();
//			}
//			
//		}
		return super.onTesting();
	}
	
	private String execCmd(String cmd)
	{
		Process process = null;
		String result = null;
		try {
			process = new ProcessBuilder().command(cmd)
					.redirectErrorStream(true).start();
			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			int read;
			char[] buffer = new char[4096];
			StringBuffer output = new StringBuffer();
			while ((read = reader.read(buffer)) > 0) {
				output.append(buffer, 0, read);
			}
			reader.close();
			process.destroy();
			process = null;
			reader = null;
			result = output.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(process != null)
			{
				process.destroy();
				process = null;
			}
		}
		return result;
	}
	class DevicechkTask extends AsyncTask<String, String, String>{

		Process process = null;
		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			String mCommand = arg0[0];
			if(mCommand == null || mCommand.length() == 0)
			{
				publishProgress("Error: invalid syntax. Please try again.\n\n");
				return null;
			}
			
			try {
				process = new ProcessBuilder().command(DEVICECHK_FILE_PATH)
						.redirectErrorStream(true).start();
				
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
				int read;
				char[] buffer = new char[4096];
				boolean start = false;
				StringBuffer output = new StringBuffer();
				while ((read = reader.read(buffer)) > 0) {
					output.append(buffer, 0, read);
					publishProgress(output.toString());
					output.delete(0, output.length());
				}
				reader.close();
				process.destroy();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		public void onProgressUpdate(String... strings) {
			
		}

		@Override
		public void onCancelled() {
			// The running process is destroyed and system resources are freed.
			if (process != null) {
				process.destroy();
				try {
					process.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		@Override
		public void onPostExecute(String result) {
			if (process != null) {
				process.destroy();

				try {
					process.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}	
			}
		}
		
	}
}
