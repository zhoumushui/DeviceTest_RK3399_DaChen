package com.rockchip.devicetest.testcase.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import twinone.lib.androidtools.shell.Command;
import twinone.lib.androidtools.shell.Shell;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.storage.StorageManager;

import com.rockchip.devicetest.ConfigFinder;
import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.testcase.impl.SerialPortTest.TestInfo;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.StorageUtils;
import com.rockchip.devicetest.utils.StringUtils;
import com.rockchip.devicetest.R;

public class SSDTest extends BaseTestCase {
	/***
	 * 清除缓存echo 3 > proc/sys/vm/drop_caches 测试写速度:busybox dd if=/dev/zero
	 * of=/storage/0c3ca7fc-62c4-49c6-bec8-853972aff390/test1 bs=1048576
	 * count=1024 conv=sync 测试读速度:busybox dd
	 * if=/storage/0c3ca7fc-62c4-49c6-bec8-853972aff390/test1 of=/dev/null
	 * conv=sync
	 */

	private static final String TEST_STRING = "SSDTest File";

	private static final int DEFAULT_READ_SPEED = 60;// MB/s
	private static final int DEFAULT_WRITE_SPEED = 100;// MB/s
	private int readSpeed;
	private int writeSpeed;
	private String pcie_uuids;
	ArrayList<String> pcie_uuid_list = new ArrayList<String>();

	private StorageManager mStorageManager = null;

	Shell mShell;
	Handler mHandler;

	public SSDTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		mShell = new Shell();
		mHandler = new Handler();

		if (mStorageManager == null)
			mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
	}

	String mPciePath = null;

	private void parseUuisList() {
		if (pcie_uuids == null || pcie_uuids.length() == 0)
			return;
		pcie_uuid_list.clear();
		if (pcie_uuids.indexOf(";") < 0) {
			pcie_uuid_list.add(pcie_uuids);
		} else {
			String[] strarray = pcie_uuids.split(";");
			for (int i = 0; i < strarray.length; i++) {
				if (strarray[i].trim().length() == 0)
					continue;
				pcie_uuid_list.add(strarray[i]);
			}

		}
	}

	@Override
	public boolean onTesting() {
		// TODO Auto-generated method stub

		Map<String, String> attachParams = mTestCaseInfo.getAttachParams();
		readSpeed = StringUtils
				.parseInt(attachParams.get(ParamConstants.READ_SPEED),
						DEFAULT_READ_SPEED);

		writeSpeed = StringUtils.parseInt(
				attachParams.get(ParamConstants.WRITE_SPEED),
				DEFAULT_WRITE_SPEED);
		pcie_uuids = StringUtils.getStringValue(
				attachParams.get(ParamConstants.PCIE_UUID), "1234567890");
		parseUuisList();
		// mPciePath = ConfigFinder.getAlivePciePath(mContext,pcie_uuid_list);
		mPciePath = "/mnt/media_rw/ssd_inode";//StorageUtils.getSSDDir(mStorageManager);
		LogUtil.d(SSDTest.this, "readSpeed:" + readSpeed + " ,writeSpeed:"
				+ writeSpeed + ",pcie_uuid:" + pcie_uuids);
		if (mPciePath == null) {
			onTestFail(R.string.pcie_err_unmount);
			return false;
		}
		LogUtil.d(SSDTest.this, "mPciePath:" + mPciePath);
		boolean rw = dotestReadAndWrite(mPciePath);
		LogUtil.d(SSDTest.this, "rw:" + rw);
		if (rw) {
			// Command clean_cache =
			// mShell.execute("echo 3 > proc/sys/vm/drop_caches");
			// LogUtil.d(PcieTest.this,
			// "clean_cache.exitStatus:"+clean_cache.exitStatus);
			mHandler.post(testRunnable);
		}
		// onTestSuccess();

		return true;
	}

	Runnable testRunnable = new Runnable() {

		@Override
		public void run() {
			Command read = mShell
					.execute("busybox dd if=/dev/zero of=" + mPciePath
							+ "/test123456 bs=1048576 count=1024 conv=sync");

			Command write = mShell.execute("busybox dd if=" + mPciePath
					+ "/test123456  of=/dev/null conv=sync");
			
			LogUtil.d(SSDTest.this, "write.exitStatus:" + write.exitStatus);
			float write_speed = 0;
			if (write.exitStatus == 0) {
				String msg = write.output[write.output.length - 1];
				LogUtil.d(SSDTest.this, "write.output:" + msg);
				write_speed = parseSpeed(msg);
				if (msg.endsWith("MB/s")) {
					// 为MB时不处理
				} else if (msg.endsWith("GB/s")) {
					write_speed = write_speed * 1024;
				} else if (msg.endsWith("KB/s")) {
					write_speed = write_speed / 1024;
				}
				LogUtil.d(SSDTest.this, "write_speed:" + write_speed);
			}

			LogUtil.d(SSDTest.this, "read.exitStatus:" + read.exitStatus);
			float read_speed = 0;
			if (read.exitStatus == 0) {
				String msg = read.output[read.output.length - 1];
				LogUtil.d(SSDTest.this, "read.output:" + msg);
				read_speed = parseSpeed(msg);
				if (msg.endsWith("MB/s")) {
					// 为MB时不处理
				} else if (msg.endsWith("GB/s")) {
					read_speed = read_speed * 1024;
				} else if (msg.endsWith("KB/s")) {
					read_speed = read_speed / 1024;
				}
				LogUtil.d(SSDTest.this, "read_speed:" + read_speed);
			}
			if (read_speed >= readSpeed && write_speed >= writeSpeed)
				onTestSuccess(mContext.getResources().getString(
						R.string.pcie_test_result, writeSpeed, write_speed,
						readSpeed, read_speed));
			else
				onTestFail(mContext.getResources().getString(
						R.string.pcie_test_result, writeSpeed, write_speed,
						readSpeed, read_speed));
		}
	};
	private static Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");

	private static String[] matcher(String input) {
		Matcher matcher = pattern.matcher(input);
		ArrayList<String> list = new ArrayList<String>();
		while (matcher.find()) {
			list.add(matcher.group());
		}

		return list.toArray(new String[0]);
	}

	private float parseSpeed(String msg) {
		if (msg == null || msg.length() == 0)
			return 0;
		String[] values = matcher(msg);
		if (values != null && values.length == 4) {
			return Float.valueOf(values[3]);
		}
		return 0;

	}

	private boolean dotestReadAndWrite(String path) {
		String directoryName = path + "/rktest";

		File directory = new File(directoryName);
		if (!directory.isDirectory()) { // Create Test Dir
			if (!directory.mkdirs()) {
				onTestFail(R.string.pcie_err_mkdir);
				return false;
			}
		}
		File f = new File(directoryName, "SSDTest.txt");
		try {
			// Remove stale file if any
			if (f.exists()) {
				f.delete();
			}
			if (!f.createNewFile()) { // Create Test File
				onTestFail(R.string.pcie_err_mkfile);
				return false;
			} else {
				boolean writeResult = FileUtils.doWriteFile(f.getAbsoluteFile()
						.toString(), TEST_STRING);
				if (!writeResult) {
					onTestFail(R.string.pcie_err_write);
					return false;
				}
				String readResult = FileUtils.doReadFile(f.getAbsoluteFile()
						.toString());
				if (readResult == null) {
					onTestFail(R.string.pcie_err_read);
					return false;
				}
				if (!readResult.equals(TEST_STRING)) {
					onTestFail(R.string.pcie_err_match);
					return false;
				}
				return true;
			}
		} catch (IOException ex) {
			onTestFail(R.string.pub_exception);
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

}
