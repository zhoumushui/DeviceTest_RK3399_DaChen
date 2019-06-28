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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.input.InputManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rockchip.devicetest.IndexActivity;
import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.smb.updateUIThread;
import com.rockchip.devicetest.testcase.BaseTestCase;
import android.net.EthernetManager;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.IperfUtils;
import com.rockchip.devicetest.utils.NetworkUtil;
import com.rockchip.devicetest.utils.SystemUtils;

public class SmbTest extends BaseTestCase {
	private static final String TAG = "SmbTest";
	private static final String DEFAULT_SMB_PATH = "smb://192.168.1.1/USB_Storage/test.mp4";
	private LayoutInflater mLayoutInflater;
	private View mView;

	private AlertDialog mDialog;
	private Context mContext;
	private Button button;
	private ProgressBar pb ;
	private TextView pathEditText;
	private updateUIThread mUpdateUIThread  = null;
	private String smb_path = null;
	private EthernetManager mEthManager;
	private WifiManager mWifiManager;

	public SmbTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		mContext = context;
		mLayoutInflater = LayoutInflater.from(context);
		mEthManager = (EthernetManager) mContext
				.getSystemService(Context.ETHERNET_SERVICE);
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
	}

	@Override
	public void onTestInit() {
		super.onTestInit();
		Map<String, String> attachParams = mTestCaseInfo.getAttachParams();
		smb_path = attachParams.get(ParamConstants.SMB_PATH);	
	}

	@Override
	public boolean onTesting() {
		
		ethConnected();

		mView = mLayoutInflater.inflate(R.layout.test_smb, null);
		button = (Button) mView.findViewById(R.id.button);
		pb = (ProgressBar)mView.findViewById(R.id.pb);
		pathEditText = (TextView)mView.findViewById(R.id.path);
		pathEditText.setText(smb_path);

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.smb_title);
		builder.setView(mView);
		builder.setPositiveButton(mContext.getString(R.string.pub_success), new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if(mUpdateUIThread != null)
					mUpdateUIThread.stopUpdateUI();				
				onTestSuccess(button.getText().toString());
				
			}
		});
		builder.setNegativeButton(mContext.getString(R.string.pub_fail), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(mUpdateUIThread != null)
					mUpdateUIThread.stopUpdateUI();			
				onTestFail(button.getText().toString());
				
			}
		});
		builder.setCancelable(false);
		mDialog = builder.create();	
		mDialog.show();
		mUpdateUIThread = null;
		checkRunnableHandler.postDelayed(checkRunnable,1000);

		return true;
	}
	@Override
	public boolean onTestHandled(TestResult result) {
		SystemUtils.setEthEnable(mContext,true);
		return super.onTestHandled(result);
	}



	private Handler handler = new Handler(){
		@Override 
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FileUtils.startDownloadMeg:
				pb.setMax(mUpdateUIThread.getFileSize());   //开始
				break;
			case FileUtils.updateDownloadMeg:
				if(!mUpdateUIThread.isCompleted())   //下载
				{
					Log.e(TAG, "DownloadSize="+mUpdateUIThread.getDownloadSize());
					pb.setProgress(mUpdateUIThread.getDownloadSize());
					button.setText("下载速度:"+mUpdateUIThread.getDownloadSpeedStr()+"   ,下载进度:"+mUpdateUIThread.getDownloadPercent()+"%");
				}else{
					button.setText("下载完成:"+mUpdateUIThread.getDownloadSpeedStr());
				} 
				break;
			case FileUtils.endDownloadMeg:  
				//Toast.makeText(mContext, "下载完成", Toast.LENGTH_SHORT).show();

				break;
			case FileUtils.noFoundFile:
				button.setText("读取smb文件错误，请检查文件是否存在");
				break;
			}
			super.handleMessage(msg);
		}
	};

	// 关闭wifi，开启以太网
	public void ethConnected() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
		//int ethEnabler = mEthManager.getEthernetConnectState();
		//如果eth已经打开则不进行操作
		if(NetworkUtil.isEthAvailable(mEthManager)){
			//mEthManager.setEthernetEnabled(true);
		}else{
			//mEthManager.setInterfaceEnable("eth0",true);  //zouxf
        	//mEthManager.setEthernetEnabled(true);
		}
	}

	private static final int MAX_RETRY_COUNT = 25;//切换网络连接时的超时时间
	private int retry_count = 0;
	private Handler checkRunnableHandler = new Handler();
	private Runnable testRunnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(mUpdateUIThread == null)
			{
				String path = smb_path;

				if(path != null && path.length() >0)
				{
					mUpdateUIThread = new updateUIThread(handler,path, FileUtils.setMkdir(mContext)+File.separator, FileUtils.getFileName(path));
				}else{
					mUpdateUIThread = new updateUIThread(handler,DEFAULT_SMB_PATH, FileUtils.setMkdir(mContext)+File.separator, FileUtils.getFileName(DEFAULT_SMB_PATH));
				}
				mUpdateUIThread.start();
			} 

		}
	};
	private Runnable checkRunnable = new Runnable() {

		@Override
		public void run() {
			retry_count++;
			if(IperfUtils.isNetworkConnected(mContext) && IperfUtils.isEth(mContext))//以太网已连接
			{
				checkRunnableHandler.postDelayed(testRunnable, 5000);
			}else if(retry_count <= MAX_RETRY_COUNT){
				checkRunnableHandler.postDelayed(checkRunnable, 1000);							
			}else {
				button.setText("下载失败，请检查网络连接！");
			}

		}
	};
}
