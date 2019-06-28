package com.rockchip.devicetest.networktest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.smb.SmbDownLoadThread;
import com.rockchip.devicetest.smb.updateUIThread;
import com.rockchip.devicetest.utils.FileUtils;
import com.rockchip.devicetest.utils.IperfUtils;
import com.rockchip.devicetest.utils.IperfUtils.IperfPack;
import com.rockchip.devicetest.utils.IperfUtils2;
import com.rockchip.devicetest.utils.PingUtils;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.EthernetManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

public class BaseNetworkManager {
	
	public interface onTestListener{
		public void onConnectTestEnd(ResultInfo info);
		public void onSmbTestEnd(ResultInfo info);
		public void onIperfTestEnd(ResultInfo info);
		public void onPingTestEnd(ResultInfo info);
		public void onUpdateDetail(String detail);
		
	}
	public onTestListener listener;
	public void setOnTestListener(onTestListener listener){
		this.listener = listener;
	}
	
	public EthernetManager mEthManager;
	public WifiManager mWifiManager;
	public boolean hasRegistReceiver;
	public Context mContext;
	
	public ResultInfo mConnectResultInfo = new ResultInfo();
	public ResultInfo mSmbResultInfo = new ResultInfo();
	public ResultInfo mIperfResultInfo = new ResultInfo();
	Runnable mSmbTestRunnable ;
	public BaseNetworkManager(Context context) {
		super();
		mContext = context;
		// TODO Auto-generated constructor stub
		mEthManager = (EthernetManager) mContext
				.getSystemService(Context.ETHERNET_SERVICE);
	        
		mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	
	public void registerReceiver(){}
	public void unRegisterReceiver(){}
	
	ProgressDialog mConnectDialog;
	public void startConnectTest(){
		mConnectDialog = ProgressDialog.show(mContext, "网络测试中", "正在尝试连接网络!", false);
		mConnectDialog.show();
		registerReceiver();
	}
	public void endConnectTest()
	{
		mTimeHandler.removeMessages(MSG_TEST_TIMEOUT);
		if(mConnectDialog != null){
			mConnectDialog.dismiss();
			mConnectDialog = null;
		} 
		if(listener != null)listener.onConnectTestEnd(mConnectResultInfo);
		unRegisterReceiver();
	}
	
	public void updateDetail(String detail)
	{
		if(listener != null) listener.onUpdateDetail(detail);
	}
	
	private static final int MSG_TEST_TIMEOUT = 0;
	Handler mTimeHandler = new Handler()
	{

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_TEST_TIMEOUT:
				mConnectResultInfo.setSuccess(false);
				mConnectResultInfo.setResult(mContext.getString(R.string.network_test_connect_timeout));
				mConnectResultInfo.setTestDone(true);
				endConnectTest();
				break;
			}
		}
		
	};
	public void setConnectTestTimeOut(long millsec)
	{
		mTimeHandler.removeMessages(MSG_TEST_TIMEOUT);
		mTimeHandler.sendEmptyMessageDelayed(MSG_TEST_TIMEOUT, millsec);
	}
	
	private BroadcastReceiver mSmbReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub
			Bundle bundle = arg1.getExtras();
			if(bundle != null && bundle.containsKey("result") && bundle.containsKey("resultInfo"))
			{
				boolean result = bundle.getBoolean("result");
				String resultInfo =bundle.getString("resultInfo");
				mSmbResultInfo.setTestDone(true);
				mSmbResultInfo.setSuccess(result);
				mSmbResultInfo.setResult(resultInfo);
				if(mDialog != null) 
					mDialog.dismiss();
				if(listener != null)listener.onSmbTestEnd(mSmbResultInfo);

			}
		}
	};
	boolean isSmbRegister = false;
	public void registerSmbReceiver()
	{
		if(!isSmbRegister){
			mContext.registerReceiver(mSmbReceiver, new IntentFilter("smb.test.done"));
			isSmbRegister = true;
		}
	}
	public void unRegisterSmbReceiver()
	{
		if(isSmbRegister)
		{
			isSmbRegister = false;
			mContext.unregisterReceiver(mSmbReceiver);
		}
	}
	
	
	private AlertDialog mDialog;
	private Button smbButton;
	private ProgressBar smbProgressBar ;
	private updateUIThread mUpdateUIThread  = null;
	public void startTestSmb(final String smbPath)
	{
		registerSmbReceiver();
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.smb_title);
		builder.setPositiveButton(mContext.getString(R.string.pub_success), new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mSmbResultInfo.setTestDone(true);
				mSmbResultInfo.setSuccess(true);
				mSmbResultInfo.setResult("测试成功");
				if(listener != null)listener.onSmbTestEnd(mSmbResultInfo);
				
			}
		});
		builder.setNegativeButton(mContext.getString(R.string.pub_fail), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				mSmbResultInfo.setTestDone(true);
				mSmbResultInfo.setSuccess(false);
				mSmbResultInfo.setResult("测试失败");
				if(listener != null)listener.onSmbTestEnd(mSmbResultInfo);
			}
		});
		builder.setCancelable(false);
		builder.setMessage(R.string.smb_msg);
		mDialog = builder.create();	
		mDialog.setOnShowListener(new AlertDialog.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(mContext, SmbTestActivity.class);
				intent.putExtra("smbPath", smbPath);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);
			}
		});
		mDialog.show();
	}
	

	
	
	
	IperfUtils2 mIperfUtils2;
	public void startTestIperf(String server_ip,String port)
	{
		if(mIperfUtils2 == null)
		{
			mIperfUtils2 = new IperfUtils2(mContext, new onResultInfoListener() {
				
				@Override
				public void onResult(ResultInfo info) {
					// TODO Auto-generated method stub
					if(listener != null) listener.onIperfTestEnd(info);
				}
			});
		}
		mIperfUtils2.release();
		mIperfUtils2.startIperfTest(server_ip, port);
		
	}
	
	PingUtils mPingUtils;
	public void startTestPing(String server_ip,String package_size) 
	{
		if(mPingUtils == null)
		{
			mPingUtils = new PingUtils(mContext, new onResultInfoListener() {

				@Override
				public void onResult(ResultInfo info) {
					// TODO Auto-generated method stub
					if(listener != null) listener.onPingTestEnd(info);
				}
			});
		}
		mPingUtils.release();
		mPingUtils.startPingTest(server_ip, package_size);
	}
	

}
