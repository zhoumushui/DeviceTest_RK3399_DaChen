/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月13日 上午9:27:11  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月13日      fxw         1.0         create
 *******************************************************************/   

package com.rockchip.devicetest.testcase.impl;


import twinone.lib.androidtools.shell.Command;
import twinone.lib.androidtools.shell.Shell;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.hardware.Camera.CameraInfo;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;

import com.rockchip.devicetest.CameraTestActivity;
import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.LogUtil;

public class CameraTest extends BaseTestCase {
	private AlertDialog mDialog;

	public CameraTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
	}

	@Override
	public void onTestInit() {
		super.onTestInit();
		clearCameraProfile();
	}

	
	@Override
	public boolean onTestHandled(TestResult result) {
		// TODO Auto-generated method stub
		clearCameraProfile();
		return super.onTestHandled(result);
	}

	@Override
	public boolean onTesting() {
		if(mDialog!=null){
			mDialog.show();
			return true;
		}
		boolean hasCamera =  hasCamera();

		if(hasCamera)
		{

			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(R.string.camera_title);
			builder.setMessage(R.string.camera_msg);
			builder.setPositiveButton(mContext.getString(R.string.pub_success), new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {				
					onTestSuccess();
				}
			});
			builder.setNegativeButton(mContext.getString(R.string.pub_fail), new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {				
					onTestFail(0);
				}
			});
			builder.setCancelable(false);
			mDialog = builder.create();
			//在dialog show后再启动Camera，以确保Camera在后open！
			mDialog.setOnShowListener(new OnShowListener() {

				@Override
				public void onShow(DialogInterface arg0) {
					// TODO Auto-generated method stub
					LogUtil.d(CameraTest.this, "start CameraTestActivity");
//					Intent intent = new Intent(Intent.ACTION_MAIN)
//				            .setClassName("com.rockchip.devicetest", "com.rockchip.devicetest.CameraTestActivity")
//				            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//					mContext.startActivity(intent); 
					Intent intent = new Intent()
		            .setClass(mContext, CameraTestActivity.class)
		            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS );
					mContext.startActivity(intent); 
				}
			});
			mDialog.show();
		}else{
			onTestFail("no camera connect");
		}

		return true;
	}
	private void clearCameraProfile()
	{
		Log.d("CameraTest","clearCameraProfile");
		Shell mShell = new Shell();
		Command ls_cmd = mShell.execute("vm -c 'ls /data/camera/media_profiles.xml'");
		if(ls_cmd.exitStatus ==0)
		{
			Log.d("CameraTest"," camera profile exists,delete now");
			Command rm_cmd = mShell.execute("vm -c 'rm /data/camera/media_profiles.xml'");
			if(rm_cmd.exitStatus == 0)
			{
				Log.d("CameraTest","delete camera profile successed");
			}else{
				Log.d("CameraTest","delete camera profile failed");
			}
		}else{
			Log.d("CameraTest"," camera profile not exists");
		}
		
	}

	private boolean hasCamera() {
		int n = android.hardware.Camera.getNumberOfCameras();
		return (n > 0);
	}

	private boolean hasBackCamera() {
		int n = android.hardware.Camera.getNumberOfCameras();
		CameraInfo info = new CameraInfo();
		for (int i = 0; i < n; i++) {
			android.hardware.Camera.getCameraInfo(i, info);
			if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
				return true;
			}
		}
		return false;
	}

}
