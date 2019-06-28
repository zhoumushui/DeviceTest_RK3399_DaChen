package com.rockchip.devicetest.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.networktest.ResultInfo;
import com.rockchip.devicetest.networktest.onResultInfoListener;
import com.rockchip.devicetest.utils.IperfUtils.IperfPack;

import android.os.AsyncTask;

public class IperfUtils2 {
	private Context mContext;
	private static final String TAG = "PingUtils";
	Button mStartBtn ,mSuccessBtn,mFailBtn;
	private ScrollView mScrollView;
	private ResultInfo mResultInfo;
	private TextView mInfoView;
	private Dialog mIperfDialog;
	private IperfTask mIperfTask;
	private static final String PING_CMD_DEFAULT = "ping -c 10 -w 100 -s %1s %2s";
	private String mIperfCmd = null;

	private onResultInfoListener mListener;
	public IperfUtils2(Context context,onResultInfoListener listener) {
		super();
		this.mContext = context;
		this.mListener = listener;
		mResultInfo = new ResultInfo();
	}
	public void startIperfTest(String server_ip,String port)
	{
		IperfUtils mIperfUtils = new IperfUtils(mContext);
		mIperfUtils.setServerInfo(server_ip, port);
		mIperfCmd = mIperfUtils.getTestCmd(IperfUtils.TYPE_TCP);
		Log.v(TAG, "startIperfTest mIperfCmd:"+mIperfCmd);
		View view = LayoutInflater.from(mContext).inflate(R.layout.test_network_iperf, null);
		mStartBtn = (Button) view.findViewById(R.id.iperfBtnStart);
		mStartBtn.setEnabled(false);
		mSuccessBtn = (Button) view.findViewById(R.id.iperfBtnSuccess);
		mFailBtn = (Button) view.findViewById(R.id.iperfBtnFail);
		
		mInfoView = (TextView)view.findViewById(R.id.iperfTestInfo);
		mScrollView = (ScrollView)view.findViewById(R.id.iperfScroller);
		
		mStartBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if ((mContext.getString(R.string.start)).equals(mStartBtn
						.getText().toString())) {
					mStartBtn.setEnabled(false);
					startTest();
				} else {
					stopTest();
					mStartBtn.setText(R.string.start);
				}
			}
		});

		mSuccessBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopTest();
				dismissDialog();
				mResultInfo.setTestDone(true);
				mResultInfo.setSuccess(true);
				mResultInfo.setResult("");
				if(mListener != null)mListener.onResult(mResultInfo);
			}
		});
		mFailBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopTest();
				dismissDialog();
				mResultInfo.setTestDone(true);
				mResultInfo.setSuccess(false);
				mResultInfo.setResult("");
				if(mListener != null)mListener.onResult(mResultInfo);
			}
		});
		if(mIperfDialog == null)
		{
			mIperfDialog = new Dialog(mContext, R.style.MyDialog);
			mIperfDialog.setContentView(view);
			mIperfDialog.setCancelable(false);
		}
		mIperfDialog.show();
		startTest();
	}
	private void dismissDialog() {
		if (mIperfDialog != null) {
			mIperfDialog.dismiss();
		}
	}
	private void startTest()
	{
		
		mIperfTask = new IperfTask(mInfoView, mScrollView);	
		mInfoView.setText("");
		mInfoView.append("start iperf test " + " \n");
		mInfoView.append(mIperfCmd + " \n\n");
//		mIperfTask.execute(mIperfTestCmd);	
		mIperfTask.executeOnExecutor(Executors.newCachedThreadPool(), mIperfCmd);
		Log.v(TAG, "mPingTask execute");
		
	}
	private void stopTest()
	{
		if (mIperfTask != null) {
			mIperfTask.cancel(true);
		}
		mIperfTask = null;
	}
	
	public void release()
	{
		dismissDialog();
		stopTest();
	}
	
	class IperfTask extends AsyncTask<String, String, String> {

		Process process = null;
		BufferedReader reader = null;
		TextView mTestInfo;
		ScrollView mScroller;
		ArrayList<IperfPack> packList;

		public IperfTask(TextView mTestInfo , ScrollView mScroller) {
			super();
			// TODO Auto-generated constructor stub
			this.mTestInfo = mTestInfo;
			this.mScroller = mScroller;
			packList = new ArrayList<IperfUtils.IperfPack>();
		}

		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			String mCommand = arg0[0];
			if(mCommand == null || mCommand.length() == 0)
			{
				publishProgress("Error: invalid syntax. Please try again.\n\n");
				return null;
			}else if (!mCommand
					.matches("(iperf )?((-[s,-server])|(-[c,-client] ([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5]))|(-[c,-client] \\w{1,63})|(-[h,-help]))(( -[f,-format] [bBkKmMgG])|(\\s)|( -[l,-len] \\d{1,5}[KM])|( -[B,-bind] \\w{1,63})|( -[r,-tradeoff])|( -[v,-version])|( -[N,-nodelay])|( -[T,-ttl] \\d{1,8})|( -[U,-single_udp])|( -[d,-dualtest])|( -[w,-window] \\d{1,5}[KM])|( -[n,-num] \\d{1,10}[KM])|( -[p,-port] \\d{1,5})|( -[L,-listenport] \\d{1,5})|( -[t,-time] \\d{1,8})|( -[i,-interval] \\d{1,4})|( -[u,-udp])|( -[b,-bandwidth] \\d{1,20}[bBkKmMgG])|( -[m,-print_mss])|( -[P,-parallel] d{1,2})|( -[M,-mss] d{1,20}))*")) {
				publishProgress("Error: invalid syntax. Please try again.\n\n");
				return null;
			}
			try {
				String[] commands = mCommand.split(" ");
				List<String> commandList = new ArrayList<String>(
						Arrays.asList(commands));
				if (commandList.get(0).equals((String) "iperf")) {
					commandList.remove(0);
				}
				commandList.add(0, IperfUtils.IPERF_FILE_PATH);
				Log.v("sjfiperf", "doInBackground 3");
				process = new ProcessBuilder().command(commandList)
						.redirectErrorStream(true).start();
				reader = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
				int read;
				char[] buffer = new char[4096];
				boolean start = false;
				StringBuffer output = new StringBuffer();
				while ((read = reader.read(buffer)) > 0 && !isCancelled()) {
					output.append(buffer, 0, read);
					publishProgress(output.toString());
					output.delete(0, output.length());
				}
				reader.close();
				reader = null;
				process.destroy();
				process = null;
			} catch (IOException e) {
				publishProgress("\nError occurred while accessing system resources, please reboot and try again.");
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void onProgressUpdate(String... strings) {
			mTestInfo.append(strings[0]);
			mScroller.post(new Runnable() {
				public void run() {
					mScroller.smoothScrollTo(0, mTestInfo.getBottom());
				}
			});
		}

		@Override
		public void onCancelled() {
			// The running process is destroyed and system resources are freed.
			try{
				if(reader != null) reader.close();
			}
			 catch (IOException e) {
					e.printStackTrace();
				}
			
			if (process != null) {
				process.destroy();
				try {
					process.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			mTestInfo.append("\nOperation aborted.\n\n");

			mScroller.post(new Runnable() {
				public void run() {
					mScroller.smoothScrollTo(0, mTestInfo.getBottom());
				}
			});
		}
		
		

		@Override
		public void onPostExecute(String result) {
			if (process != null) {
				process.destroy();

				try {
					process.waitFor();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//mTestInfo.append(packList.get(packList.size()-1).toString());
			
			}
			mTestInfo.append("\nTest is done.\n\n");
			mStartBtn.setText(R.string.start);
			mStartBtn.setEnabled(true);
			mScroller.post(new Runnable() {
				public void run() {
					mScroller.smoothScrollTo(0, mTestInfo.getBottom());
				}
			});
		}
	}

}
