package com.rockchip.devicetest.testcase.impl;

import twinone.lib.androidtools.shell.Command;
import twinone.lib.androidtools.shell.Shell;
import android.app.AlertDialog;
import android.app.KeyguardManager.KeyguardLock;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.view.LayoutTypecCheck;
import com.rockchip.devicetest.view.LayoutTypecCheck.CheckMode;
import com.rockchip.devicetest.view.LayoutTypecCheck.OnMyClickListener;

public class TypecTest extends BaseTestCase{

	private LayoutInflater mLayoutInflater;
	private View mView;
	private AlertDialog mDialog;
	public TypecTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		// TODO Auto-generated constructor stub
		mLayoutInflater = LayoutInflater.from(context);
	}

	private LayoutTypecCheck mDp1;
	private LayoutTypecCheck mDp2;
	private LayoutTypecCheck mUsb1;
	private LayoutTypecCheck mUsb2;


	@Override
	public boolean onTesting() {
		// TODO Auto-generated method stub
		LogUtil.d(TypecTest.this, "onTesting");
		if(mDialog!=null){
			mDialog.show();
			return true;
		}
		mView = mLayoutInflater.inflate(R.layout.test_typec, null);
		mDp1 = (LayoutTypecCheck)mView.findViewById(R.id.typec_dp_1);
		mDp2 = (LayoutTypecCheck)mView.findViewById(R.id.typec_dp_2);
		mUsb1 = (LayoutTypecCheck)mView.findViewById(R.id.typec_usb_1);
		mUsb2 = (LayoutTypecCheck)mView.findViewById(R.id.typec_usb_2);
		initClickListener();

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.typec_test_title);
		builder.setView(mView);

		builder.setCancelable(false);
		mDialog = builder.create();
	//	mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);
		mDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
		mDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {

				return true;
			}
		});
		mDialog.show();
		return true;
	}
	
	

	@Override
	public boolean onTestHandled(TestResult result) {
		// TODO Auto-generated method stub
		if(mDialog != null)
		{
		    if(mDialog.isShowing())mDialog.dismiss();
		    mDialog = null;
		}
		return super.onTestHandled(result);
	}



	private void initClickListener()
	{
		mDp1.setOnMyClickListener(new OnMyClickListener() {
			@Override
			public void onBtnPositiveClick(Button btn) {
				// TODO Auto-generated method stub
				mDp1.setCheckMode(CheckMode.SUCCESS);
				mDp1.setButtonBackgroundDown(btn);
				checkTestDone();
			}

			@Override
			public void onBtnNegativeClick(Button btn) {
				// TODO Auto-generated method stub
				mDp1.setCheckMode(CheckMode.FAIL);
				mDp1.setButtonBackgroundDown(btn,Color.RED);
				checkTestDone();
			}
		});

		mDp2.setOnMyClickListener(new OnMyClickListener() {
			@Override
			public void onBtnPositiveClick(Button btn) {
				// TODO Auto-generated method stub
				mDp2.setCheckMode(CheckMode.SUCCESS);
				mDp2.setButtonBackgroundDown(btn);
				checkTestDone();
				
			}

			@Override
			public void onBtnNegativeClick(Button btn) {
				// TODO Auto-generated method stub
				mDp2.setCheckMode(CheckMode.FAIL);
				mDp2.setButtonBackgroundDown(btn,Color.RED);
				checkTestDone();
			}
		});

		mUsb1.setOnMyClickListener(new OnMyClickListener() {
			@Override
			public void onBtnPositiveClick(Button btn) {
				// TODO Auto-generated method stub
				if(isTypecUsbMounted())
				{
					mUsb1.setCheckMode(CheckMode.SUCCESS);
					btn.setText(R.string.typec_btn_success);
					mUsb1.setButtonBackgroundDown(btn, Color.GREEN);
				}else
				{
					mUsb1.setCheckMode(CheckMode.FAIL);
					btn.setText(R.string.typec_btn_fail);
					mUsb1.setButtonBackgroundDown(btn, Color.RED);
				}
				
				checkTestDone();
			}

			@Override
			public void onBtnNegativeClick(Button btn) {
				// TODO Auto-generated method stub

			}
		});

		mUsb2.setOnMyClickListener(new OnMyClickListener() {

			@Override
			public void onBtnPositiveClick(Button btn) {
				// TODO Auto-generated method stub
				if(isTypecUsbMounted())
				{
					mUsb2.setCheckMode(CheckMode.SUCCESS);
					btn.setText(R.string.typec_btn_success);
					mUsb2.setButtonBackgroundDown(btn, Color.GREEN);
				}else
				{
					mUsb2.setCheckMode(CheckMode.FAIL);
					btn.setText(R.string.typec_btn_fail);
					mUsb2.setButtonBackgroundDown(btn, Color.RED);
				}
				
				checkTestDone();
			}

			@Override
			public void onBtnNegativeClick(Button btn) {
				// TODO Auto-generated method stub

			}


		});


	}
	private static final String PREFIX = "dev/block/platform/usb@fe800000/fe800000.dwc3/xhci-hcd.2.auto/sd*";
	boolean isTypecUsbMounted()
	{
		Shell mShell = new Shell();
		Command cmd = mShell.execute("ls "+PREFIX);
		LogUtil.d(TypecTest.this, "isTypecUsbMounted cmd.exitStatus:"+cmd.exitStatus);
		if(cmd.exitStatus == 0)
		{
			LogUtil.d(TypecTest.this, "isTypecUsbMounted cmd.output.length:"+cmd.output.length);
			return cmd.output.length >=1;
		}

		return false;
	}
	
	private void checkTestDone()
	{
		if(mDp1.isChecked()
				&&mDp2.isChecked()
				&&mUsb1.isChecked()
				&&mUsb2.isChecked()){
			boolean result = true;
			StringBuilder result_str = new StringBuilder();
			
			result = result && mDp1.isCheckSuccess();
			result_str.append("dp正面:"+mDp1.getCheckMode().getValueStr()+";");
			
			result = result && mDp2.isCheckSuccess();
			result_str.append("dp反面:"+mDp2.getCheckMode().getValueStr()+";");
			
			result = result && mUsb1.isCheckSuccess();
			result_str.append("Usb正面:"+mUsb1.getCheckMode().getValueStr()+";");
			
			result = result && mUsb2.isCheckSuccess();
			result_str.append("Usb反面:"+mUsb2.getCheckMode().getValueStr()+";");
			
			if(result)
			{
				onTestSuccess(result_str.toString());
			}else{
				onTestFail(result_str.toString());
			}
		}
	}

}
