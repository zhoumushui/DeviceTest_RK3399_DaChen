package com.rockchip.devicetest.testcase.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.SettingUtil;
import com.rockchip.devicetest.utils.StringUtils;

public class RecordTest extends BaseTestCase {

	private static final String TAG = "RecordTest";
	private static final boolean DEBUG = true;
	private static final int RECORD_MAX_TIME = 60000;
	private AlertDialog dialog = null;
	private View view;

	private Button btnRecord;
	private Button btnPlay;
	private Button btnSuccess;
	private Button btnFail;

	private Context mContext;

	//语音文件保存路径  
	private String FileName = null;  

	//语音操作对象  
	private MediaPlayer mPlayer = null;  
	private MediaRecorder mRecorder = null;  
	int recordTimeMax = 0;

	private void LOGV(String msg) {
		if (DEBUG)
			Log.v(TAG, msg);
	}

	public RecordTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	private void startTest() {	

		//设置sdcard的路径  
		FileName = Environment.getExternalStorageDirectory().getAbsolutePath();  
		FileName += "/audiorecordtest.3gpp";  

		view = LayoutInflater.from(mContext).inflate(R.layout.test_record, null);

		btnRecord = (Button) view.findViewById(R.id.btn_record);
		btnPlay = (Button) view.findViewById(R.id.btn_play);
		btnSuccess = (Button) view.findViewById(R.id.btnSuccess);
		btnFail = (Button) view.findViewById(R.id.btnFail);

		btnRecord.setOnClickListener(mOnClickListener);
		btnPlay.setOnClickListener(mOnClickListener);
		btnSuccess.setOnClickListener(mOnClickListener);
		btnFail.setOnClickListener(mOnClickListener);

	}

	View.OnClickListener mOnClickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v == btnRecord) {
				stopPlay();
				startRecord();		
				SettingUtil.SaveFileToNode(new File("/proc/in_sel"), "2");
			} else if (v == btnPlay) {
				stopRecord();
				startPlay();
			} else if (v == btnSuccess) {
				dismiss();
				onTestSuccess();
			}else if (v == btnFail) {
				dismiss();
				onTestFail(0);
			} else {

			}
		}
	};

	private static final int MSG_STOP_RECORD = 0;
	@SuppressLint("HandlerLeak")
	public Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what){
			case MSG_STOP_RECORD:				
				stopRecord();
				Toast.makeText(mContext, "The record has enough "+recordTimeMax/1000+"s, stop recording automatically!", Toast.LENGTH_LONG).show();
				break;

			default:
				Log.e(TAG, "mHandler msg.what:"+ msg.what);
				break;
			}
		}
	};

	public void startRecord()
	{
		btnRecord.setEnabled(false);
		mRecorder = new MediaRecorder();  
		//		mRecorder.reset();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);  
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);  
		mRecorder.setOutputFile(FileName);  
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);  
		try {  
			mRecorder.prepare();  
			mRecorder.start();  
		} catch (IOException e) {  
			Toast.makeText(mContext, "Record Failed!", Toast.LENGTH_LONG).show();
			Log.e(TAG, "Record Failed!");  
		}  
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_STOP_RECORD), recordTimeMax);		
	}

	public void stopRecord()
	{
		mHandler.removeMessages(MSG_STOP_RECORD);
		if(mRecorder != null) {		
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;			
		}
		btnRecord.setEnabled(true);
	}

	public void startPlay()
	{
		btnPlay.setEnabled(false);
		mPlayer = new MediaPlayer(); 
		//		mPlayer.reset();
		try{  
			mPlayer.setDataSource(FileName);  
			mPlayer.prepare();  
			mPlayer.start();  
		}catch(IOException e){  
			Toast.makeText(mContext, "Play Record Failed!", Toast.LENGTH_LONG).show();
			Log.e(TAG,"Play Record Failed!");  
		} 
	}

	public void stopPlay()
	{
		if(mPlayer != null) {
			mPlayer.stop();  
			mPlayer.release();
			mPlayer = null;
		}
		btnPlay.setEnabled(true);
	}

	@Override
	public boolean onTesting() {
		// TODO Auto-generated method stub
		if (mTestCaseInfo == null || mTestCaseInfo.getAttachParams() == null) {
			onTestFail(R.string.memory_err_attach_params);
			return false;
		}
		// Check specified wifi ap
		Map<String, String> attachParams = mTestCaseInfo.getAttachParams();
		recordTimeMax = StringUtils.parseInt(attachParams.get(ParamConstants.RECORD_TIME_MAX), RECORD_MAX_TIME);
		LOGV("recordTimeMax"+recordTimeMax);
		startTest();
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.record);
		// builder.setMessage(R.string.iperf_msg);
		builder.setView(view);
		builder.setCancelable(false);
		dialog = builder.create();
		dialog.show();
		// dialog.getWindow().setLayout(width / 2, height - 100);
		return super.onTesting();
	}


	private void dismiss() {
		if (dialog != null) {
			dialog.dismiss();
		}		
	}


}
