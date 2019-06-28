/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月14日 上午11:12:27  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月14日      fxw         1.0         create
 *******************************************************************/

package com.rockchip.devicetest.testcase.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import com.rockchip.devicetest.ConfigFinder;
import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.StringUtils;

public class UsbTest extends BaseTestCase {

	private static final String TAG = "UsbTest";
	private static final String TEST_STRING = "Rockchip UsbHostTest File";
	private StringBuilder mDetailInfo;

	private static final int DEFAULT_USB_PORT_COUNT = 3;
	private static final String DEFAULT_USB_MOUNT_PORT = "mnt/usb_storage/";
	private static final String DEFAULT_USB_MOUNT_DISK_SUFFIX = "USB_DISK";
	
	private static final String NO_DISK_SUFFIX = "NO_DISK_SUFFIX";
	private static final boolean DEFAULT_USB_NEED_CHEEK_FLASH = false;
	
	

	//private String mountPort;
	private int portCount;
	//private String diskSuffix;
	private boolean needCheckFlash;
	//private boolean no_disk_suffix = false;

	private StorageManager mStorageManager;

	public UsbTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		mDetailInfo = new StringBuilder();
		mStorageManager = (StorageManager) context
				.getSystemService(Activity.STORAGE_SERVICE);
	}

	@Override
	public void onTestInit() {
		super.onTestInit();
	}

	public boolean onTesting() {
		List<String> usbPathList = ConfigFinder.getAliveUsbPath(mContext);
		if (usbPathList.isEmpty()) {
			onTestFail(R.string.usb_err_unmount);
			return false;
		}
		for (String string : usbPathList) {
			Log.v(TAG, "path:"+string);
		}

		Map<String, String> attachParams = mTestCaseInfo.getAttachParams();
//		mountPort = StringUtils.getStringValue(
//				attachParams.get(ParamConstants.USB_MOUNT_PORT),
//				DEFAULT_USB_MOUNT_PORT);
		portCount = StringUtils.parseInt(
				attachParams.get(ParamConstants.USB_PORT_COUNT),
				DEFAULT_USB_PORT_COUNT);
//		diskSuffix = StringUtils.getStringValue(
//				attachParams.get(ParamConstants.USB_MOUNT_DISK_SUFFIX),
//				DEFAULT_USB_MOUNT_DISK_SUFFIX);
		needCheckFlash = StringUtils.getBooleanValue(
				attachParams.get(ParamConstants.USB_NEED_CHECK_FLASH),
				DEFAULT_USB_NEED_CHEEK_FLASH);

		boolean result = true;
		if(needCheckFlash)
		{
			usbPathList.add(Environment.getExternalStorageDirectory().getPath());
		}
		
		
//		 no_disk_suffix = NO_DISK_SUFFIX.equals(diskSuffix);
//		if(no_disk_suffix)
//		{
////			if(!usbPathList.contains(Environment.getHostStorageDirectory().getAbsolutePath()))
////			{
////				usbPathList.add(Environment.getHostStorageDirectory().getAbsolutePath());
////			}
//		}
		
		int usize = usbPathList.size();
		mDetailInfo.delete(0, mDetailInfo.length());

		
		
		Log.i("UsbTest", "size:" + usize);

//		int disksize = 0;
//		int diskindex = 0;
//		if(!no_disk_suffix)
//		{
//			for (int i = 0; i < usize; i++) {
//
//				String diskpath = usbPathList.get(i);
//				if (diskpath.indexOf(diskSuffix) != -1) {
//					disksize += 1;
//					int start = diskpath.lastIndexOf(diskSuffix);
//					String index_str = diskpath.substring(start
//							+ diskSuffix.length());
//					int index = 0;
//					try {
//						index = Integer.parseInt(index_str);
//					} catch (NumberFormatException e) {
//						// TODO: handle exception
//					}
//					if (index >= diskindex)
//						diskindex = index;
//				}
//			}
//		}else{
//			disksize = 1;
//		}

		usize = usbPathList.size();
		for (int i = 0; i < usize; i++) {

			String path = usbPathList.get(i);
//			if (!needCheckFlash && new File(mountPort).getAbsolutePath().indexOf(path) == -1)
//				continue;
			// boolean testRes = testUsbDevice(usbPathList.get(i), usize == 1 ?
			// 0
			// : i + 1);
			boolean testRes = testUsbDevice(usbPathList.get(i), i + 1);
			if (testRes == false) {
				result = false;
			}
		}
		if (result) {
			if (usize == portCount) {
				onTestSuccess(mDetailInfo.toString());
			} else if (usize == (portCount - 1)) {
				// 弹出对话框，让测试人员判断是否连接鼠标
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle(R.string.pub_prompt);
				builder.setMessage(R.string.usb_message);
				builder.setPositiveButton(R.string.pub_success,
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								mDetailInfo
										.append("."
												+ getString(R.string.usb_to_otg_success));
								onTestSuccess(mDetailInfo.toString());
							}
						});
				builder.setNegativeButton(R.string.pub_fail,
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								mDetailInfo.append("."
										+ getString(R.string.usb_count_err));
								onTestFail(mDetailInfo.toString());
							}
						});
				builder.setCancelable(false);
				AlertDialog dialog = builder.create();
				dialog.show();
				dialog.getWindow().getDecorView()
						.findViewById(android.R.id.button1).requestFocus();
			} else {
				mDetailInfo.append(getString(R.string.usb_count_err));
				// 测试的usb数量少于usb端口数量
				onTestFail(mDetailInfo.toString());
			}
			;
		} else {
			onTestFail(mDetailInfo.toString());
		}
		return result;
	}

	// 测试失败
	public void onTestFail(int errResID, int usbIndex) {
		if (usbIndex == 0) {// 只有一个U盘
			mDetailInfo.append(getString(errResID));
		} else {
			mDetailInfo.append(getString(R.string.cmd_usb) + usbIndex + ": "
					+ getString(errResID) + ". ");
		}
	}

	// 测试成功
	public void onTestSuccess(int usbIndex, String usbPath) {

		StorageVolume[] volList = mStorageManager.getVolumeList();
		String usbLabel = String.valueOf(usbIndex);
		for (int i = 0; i < volList.length; i++) {
			StorageVolume volume = volList[i];
			if (volume.getPath().equals(usbPath)) {
				if (volume.getUserLabel() != null
						&& volume.getUserLabel().length() > 0) {
					usbLabel = volume.getUserLabel();
				}
			}
		}

		mDetailInfo.append(getString(R.string.cmd_usb_label) + usbLabel + ": "
				+ getString(R.string.pub_success) + ". ");
	}

	/**
	 * 测试USB
	 * 
	 * @return
	 */
	public boolean testUsbDevice(String usbPath, int usbIndex) {
//		Process process;
		String temp;
		Log.v(TAG, "usbIndex=" + usbIndex);
		Log.v(TAG, "usbPath=" + usbPath);
		return testReadAndWrite(usbPath, usbIndex);
//		Runtime runtime = Runtime.getRuntime();
//		try {
//			process = runtime.exec("/system/bin/ls " + usbPath);
//			BufferedReader reader = new BufferedReader(new InputStreamReader(
//					process.getInputStream()));
//			int count = 0;
//			while ((temp = reader.readLine()) != null) {
//				Log.v(TAG, "temp=" + temp);
//				//if ((temp.startsWith("udisk") && !temp.equals("udisk"))) {
//					usbPath += "/" + temp;
//					process.destroy();
//					reader.close();
//					return testReadAndWrite(usbPath, usbIndex);
//				//}
//				count++;
//			}
////			if ((count > 0 && usbPath.indexOf("USB_DISK") != -1)
////					|| usbPath.indexOf("internal_sd") != -1 || no_disk_suffix) {
////				return testReadAndWrite(usbPath, usbIndex);
////			}
//			Log.i("UsbTest", "usbPath->" + usbPath);
//			onTestFail(R.string.usb_err_noexist, usbIndex);
//			return false;
//		} catch (IOException e) {
//			e.printStackTrace();
//			onTestFail(R.string.pub_exception, usbIndex);
//			return false;
//		}
	}

	public boolean testReadAndWrite(String usbPath, int usbIndex) {
		return dotestReadAndWrite(usbPath, usbIndex);
	}

	private boolean dotestReadAndWrite(String usbPath, int usbIndex) {
		String directoryName = usbPath + "/rktest";

		File directory = new File(directoryName);
		if (!directory.isDirectory()) { // Create Test Dir
			if (!directory.mkdirs()) {
				onTestFail(R.string.sd_err_mkdir, usbIndex);
				return false;
			}
		}
		File f = new File(directoryName, "UsbHostTest.txt");
		try {
			// Remove stale file if any
			if (f.exists()) {
				f.delete();
			}
			if (!f.createNewFile()) { // Create Test File
				onTestFail(R.string.sd_err_mkfile, usbIndex);
				return false;
			} else {
				boolean writeResult = doWriteFile(f.getAbsoluteFile()
						.toString());
				if (!writeResult) {
					onTestFail(R.string.sd_err_write, usbIndex);
					return false;
				}
				String readResult = doReadFile(f.getAbsoluteFile().toString());
				if (readResult == null) {
					onTestFail(R.string.sd_err_read, usbIndex);
					return false;
				}
				if (!readResult.equals(TEST_STRING)) {
					onTestFail(R.string.sd_err_match, usbIndex);
					return false;
				}
				onTestSuccess(usbIndex, usbPath);
				return true;
			}
		} catch (IOException ex) {
			onTestFail(R.string.pub_exception, usbIndex);
			return false;
		} finally {
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
	 * 
	 * @param filename
	 * @return
	 */
	public boolean doWriteFile(String filename) {
		try {
			OutputStreamWriter osw = new OutputStreamWriter(
					new FileOutputStream(filename));
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
	 * 
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
