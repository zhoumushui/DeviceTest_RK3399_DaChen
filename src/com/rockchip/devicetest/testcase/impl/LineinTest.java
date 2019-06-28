/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月13日 上午9:04:27  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月13日      fxw         1.0         create
 *******************************************************************/

package com.rockchip.devicetest.testcase.impl;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;

public class LineinTest extends BaseTestCase {

	
	public LineinTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		
	}

	public boolean onTesting() {
		// View view = mLayoutInflater.inflate(R.layout.test_key, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.line_in_title);
		// builder.setView(view);
		builder.setMessage(R.string.line_in_msg);
		builder.setPositiveButton(mContext.getString(R.string.pub_success),
				new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				onTestSuccess();
			}
		});
		builder.setNegativeButton(mContext.getString(R.string.pub_fail),
				new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				onTestFail(0);
			}
		});
		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();
		dialog.getWindow().getDecorView().findViewById(android.R.id.button1)
		.requestFocus();
		return super.onTesting();
	}

	public boolean onTestHandled(TestResult result) {
		return super.onTestHandled(result);
	}

}
