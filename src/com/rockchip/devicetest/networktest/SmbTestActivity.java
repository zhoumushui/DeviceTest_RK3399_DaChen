package com.rockchip.devicetest.networktest;

import java.io.File;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.smb.updateUIThread;
import com.rockchip.devicetest.utils.FileUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SmbTestActivity extends Activity implements android.view.View.OnClickListener{

	Context mContext;
	public ResultInfo mSmbResultInfo = new ResultInfo();
	Runnable mSmbTestRunnable ;
	String smbPath;

	TextView mSmbHiht;
	TextView mSmbPath;
	Button mSmbSuccess,mSmbFail;
	ProgressBar mSmbProgressBar ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_smb_test);
		initView();
		mContext = this;
		Intent intent = getIntent();
		smbPath = intent.getStringExtra("smbPath");
		if (TextUtils.isEmpty(smbPath)) {
			smbPath = "smb://192.168.1.85/shared/4K_H264_bird.mp4";
		}
		startTestSmb(smbPath);
	}
	private void initView()
	{
		mSmbHiht = (TextView)findViewById(R.id.smb_hiht);
		mSmbPath = (TextView)findViewById(R.id.smb_path);
		mSmbSuccess = (Button)findViewById(R.id.smb_success);
		mSmbFail  = (Button)findViewById(R.id.smb_fail);
		mSmbProgressBar = (ProgressBar)findViewById(R.id.smb_pb);

		mSmbSuccess.setOnClickListener(this);
		mSmbFail.setOnClickListener(this);
	}
	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()) {
		case R.id.smb_success:
			if(mUpdateUIThread != null)
				mUpdateUIThread.stopUpdateUI();		
			sendResult(true, mSmbHiht.getText().toString());
			Process.killProcess(Process.myPid());
			break;
		case R.id.smb_fail:
			if(mUpdateUIThread != null)
				mUpdateUIThread.stopUpdateUI();	
			sendResult(false, mSmbHiht.getText().toString());
			Process.killProcess(Process.myPid());
			break;
		default:
			break;
		}

	}

	private void sendResult(boolean result,String resultInfo)
	{
		  Intent intent = new Intent("smb.test.done");
          Bundle bundle = new Bundle();
          bundle.putBoolean("result", result);
          bundle.putString("resultInfo",resultInfo);
          intent.putExtras(bundle);
          sendBroadcast(intent);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		smbPath = intent.getStringExtra("smbPath");
		if (TextUtils.isEmpty(smbPath)) {
			smbPath = "smb://192.168.1.85/shared/4K_H264_bird.mp4";
		}
		startTestSmb(smbPath);
		super.onNewIntent(intent);
	}
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) { //监控/拦截/屏蔽返回键
			return true; 
		} 
		return super.onKeyDown(keyCode, event);
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Process.killProcess(Process.myPid());
	}

	private updateUIThread mUpdateUIThread  = null;
	public void startTestSmb(final String smbPath)
	{
		mSmbPath.setText(smbPath);
		mSmbTestRunnable = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(smbPath != null && smbPath.length() >0)
				{
					mUpdateUIThread = new updateUIThread(mSmbHandler,smbPath, FileUtils.setMkdir(mContext)+File.separator, FileUtils.getFileName(smbPath));
				}else{
					mSmbHiht.setText("读取smb文件错误，请检查文件是否存在");
				}
				mUpdateUIThread.start();
			}
		};
		mSmbHandler.postDelayed(mSmbTestRunnable, 1000);
	}

	private Handler mSmbHandler = new Handler(){
		@Override 
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FileUtils.startDownloadMeg:
				mSmbProgressBar.setMax(mUpdateUIThread.getFileSize());   //开始
				//mSmbHandler.sendEmptyMessageDelayed(FileUtils.updateDownloadMeg, 1000);
				break;
			case FileUtils.updateDownloadMeg:
				//mUpdateUIThread.updateDownloadDatial();
				if(!mUpdateUIThread.isCompleted())   //下载
				{
					mSmbProgressBar.setProgress(mUpdateUIThread.getDownloadSize());
					mSmbHiht.setText("下载速度:"+mUpdateUIThread.getDownloadSpeedStr()+"   ,下载进度:"+mUpdateUIThread.getDownloadPercent()+"%");
					//mSmbHandler.sendEmptyMessageDelayed(FileUtils.updateDownloadMeg, 1000);
				}else{
					mSmbHiht.setText("下载完成:"+mUpdateUIThread.getDownloadSpeedStr());
					//mSmbHandler.removeMessages(FileUtils.updateDownloadMeg);
				} 
				break;
			case FileUtils.endDownloadMeg:  
				//mSmbHandler.removeMessages(FileUtils.updateDownloadMeg);
				//Toast.makeText(mContext, "下载完成", Toast.LENGTH_SHORT).show();

				break;
			case FileUtils.noFoundFile:
				//mSmbHandler.removeMessages(FileUtils.updateDownloadMeg);
				mSmbHiht.setText("读取smb文件错误，请检查文件是否存在");
				break;
			}
			super.handleMessage(msg);
		}
	};


}
