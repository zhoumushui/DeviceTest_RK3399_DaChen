package com.rockchip.devicetest;

import com.rockchip.devicetest.testcase.LEDSettings;
import com.rockchip.devicetest.utils.SystemUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
public class UnSafeShutDownActivity extends Activity{

	Bundle testBundle;
	AlertDialog mUnSafeDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.unsafeshutwon);
		testBundle = getIntent().getExtras();
		mUnSafeDialog = new AlertDialog.Builder(this)
		.setTitle(R.string.unsafe_dialog_title)
		.setMessage(R.string.unsafe_dialog_msg)
		.setNegativeButton(android.R.string.cancel,new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				//mUnSafeDialog.dismiss();
			}
		})
			.setPositiveButton(android.R.string.ok,new OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				SystemUtils.startLogSave();
				Intent agingIntent = new Intent();
				agingIntent.setClass(UnSafeShutDownActivity.this, AgingTestActivity.class);
				if(testBundle != null)
					agingIntent.putExtras(testBundle);
				agingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(agingIntent);
			}
		})
		.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss(DialogInterface arg0) {
				// TODO Auto-generated method stub
				UnSafeShutDownActivity.this.finish();
			}
		})
		.create();
		mUnSafeDialog.setCancelable(false);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(mUnSafeDialog != null)
		{
			mUnSafeDialog.show();
		}
		LEDSettings.onLed();
	}
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if(mUnSafeDialog != null && mUnSafeDialog.isShowing())mUnSafeDialog.dismiss();
		
		LEDSettings.offLed();
	}


}
