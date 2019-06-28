/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月5日 下午5:52:00  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月5日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.testcase.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.content.Context;

import com.rockchip.devicetest.ConfigFinder;
import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.LogUtil;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import android.os.storage.StorageVolume;
import android.os.storage.IMountService;
import android.os.storage.StorageManager;
import android.os.storage.StorageEventListener;

public class SdCardTest extends BaseTestCase {

	public static final int INDEX_SDCARD = 1;
	
	private static final String TEST_STRING = "Rockchip UsbHostTest File";
	private String sdcard_path;
	
	public SdCardTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
	}
	


	@Override
	public boolean onTesting() {
		// TODO Auto-generated method stub
		
		sdcard_path = ConfigFinder.getAliveSdcardPath(mContext);
		LogUtil.d(SdCardTest.this,"sdcard_path:"+sdcard_path);
		if(sdcard_path == null || sdcard_path.length() == 0)
		{
			onTestFail(R.string.sd_err_unmount);
		}else{
			return testReadAndWrite(sdcard_path);
		}
		return super.onTesting();
	}

	@Override
	public boolean onTestHandled(TestResult result) {
		return super.onTestHandled(result);
	}
	
	/**
	 * 测试SDCARD
	 * @return
	 */
//	public boolean testSdcard() {
//		try {
//			String externalVolumeState = mStorageManager.getVolumeState(sdcard_path);
//			if (!externalVolumeState.equals(Environment.MEDIA_MOUNTED)) {
//				onTestFail(R.string.sd_err_unmount);
//				return false;
//			}
//			
//			return testReadAndWrite(sdcard_path);
//		} catch (Exception rex) {
//			onTestFail(R.string.pub_exception);
//			return false;
//		}
//	}

	public boolean testReadAndWrite(String directoryName) {
		return dotestReadAndWrite(directoryName);
	}

	private boolean dotestReadAndWrite(String directoryName) {
		File directory = new File(directoryName + "/rktest");
		if (!directory.isDirectory()) {
			if (!directory.mkdirs()) {
				onTestFail(R.string.sd_err_mkdir);
				return false;
			}
		}
		File f = new File(directoryName, "storagetest.txt");
		try {
			if (f.exists()) {
				f.delete();
			}
			if (!f.createNewFile()) {
				onTestFail(R.string.sd_err_mkfile);
				return false;
			} 
			boolean writeResult = doWriteFile(f.getAbsoluteFile().toString());
			if(!writeResult){
				onTestFail(R.string.sd_err_write);
				return false;
			}
			String readResult = doReadFile(f.getAbsoluteFile().toString());
			if(readResult==null){
				onTestFail(R.string.sd_err_read);
				return false;
			}
			if(!readResult.equals(TEST_STRING)) {
				onTestFail(R.string.sd_err_match);
				return false;
			}
			onTestSuccess();
			return true;
		} catch (IOException ex) {
			onTestFail(R.string.pub_exception);
			return false;
		} finally{
			if (f.exists()) {
				f.delete();
			}
			if (directory.exists()) {
				directory.delete();
			}
		}
	}

	/**
	 * 写入测试数据
	 * @param filename
	 * @return
	 */
	public boolean doWriteFile(String filename) {
		try {
			OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(filename));
			osw.write(TEST_STRING, 0, TEST_STRING.length());
			osw.flush();
			osw.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * 读取测试数据
	 * @param filename
	 * @return
	 */
	public String doReadFile(String filename) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename)));
			String data = null;
			StringBuilder temp = new StringBuilder();
			while ((data = br.readLine()) != null) {
				temp.append(data);
			}
			br.close();
			return temp.toString();
		} catch (Exception e) {
			return null;
		}
	}

}
