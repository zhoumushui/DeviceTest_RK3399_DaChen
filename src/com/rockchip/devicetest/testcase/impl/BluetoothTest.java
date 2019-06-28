/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月7日 上午11:14:09  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月7日      fxw         1.0         create
 *******************************************************************/

package com.rockchip.devicetest.testcase.impl;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.StringUtils;

public class BluetoothTest extends BaseTestCase {

	private static final int BLUETOOTH_TEST_TIMEOUT = 10000;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBTDevice;
	private Context mContext;

	private boolean isTestFinish = false;
	private boolean hasRegisterReceiver = false;
	private boolean stop = false;
	private int mTestCount;

	private StringBuffer buffer = new StringBuffer();

	public BluetoothTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		this.mContext = context;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			LogUtil.show(context, "该设备不支持蓝牙功能");
		}
	}

	// 注册广播消息
	private void registerBTReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		/*intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);*/
		mContext.registerReceiver(mReceiver, intentFilter);
	}

	@Override
	public void onTestInit() {
		super.onTestInit();
	}

	@Override
	public boolean onTesting() {
		if (mBluetoothAdapter == null) {
			onTestFail(R.string.pub_test_no_exist);
			return super.onTesting();
		}
		if (!hasRegisterReceiver) {
			registerBTReceiver();
			hasRegisterReceiver = true;
		}
		if (mBluetoothAdapter.isEnabled()) {
			onTestSuccess();
			// 蓝牙打开成功后，恢复默认状态
			mBluetoothAdapter.disable();
		} else{
			mBluetoothAdapter.enable();
			setTestTimeout(BLUETOOTH_TEST_TIMEOUT);
		}
		return super.onTesting();
	}

	@Override
	public boolean onTestHandled(TestResult result) {
		if (hasRegisterReceiver) {
			mContext.unregisterReceiver(mReceiver);
			hasRegisterReceiver = false;
		}
		super.onTestHandled(result);
		return true;
	}

	// 停止测试
	public void stop() {
		super.stop();
		if (hasRegisterReceiver) {
			mContext.unregisterReceiver(mReceiver);
			hasRegisterReceiver = false;
		}
	}
	
	

	@Override
	public void onTestSuccess() {
		// TODO Auto-generated method stub
		super.onTestSuccess();
		//if(mBluetoothAdapter != null)mBluetoothAdapter.disable();
	}

	@Override
	public void onTestFail(String detail) {
		// TODO Auto-generated method stub
		super.onTestFail(detail);
		//if(mBluetoothAdapter != null)mBluetoothAdapter.disable();
	}

	



	/**
	 * Bluetooth广播消息接收处理
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
	            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				if (state == BluetoothAdapter.STATE_ON) {
					onTestSuccess();
				} else if (state == BluetoothAdapter.ERROR) {
					onTestFail(0);
				}
			} 
		}
	};
}
