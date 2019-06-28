package com.rockchip.devicetest.testcase.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import android.net.NetworkInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.ConnectivityManager;
import android.net.EthernetManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.IperfUtils;
import com.rockchip.devicetest.utils.IperfUtils.IperfPack;
import com.rockchip.devicetest.utils.LogUtil;
import com.rockchip.devicetest.utils.SystemUtils;
import com.rockchip.devicetest.utils.WifiUtils;
import com.rockchip.devicetest.model.TestResult;

public class IperfTest extends BaseTestCase {


	private Dialog dialog = null;
	private View view;
	private String mServerIP;
	private String mServerPort;

	private TextView mWifiTestInfo;
	private ScrollView mWifiScrollView;
	private TextView mWifi5GTestInfo;
	private ScrollView mWifi5GScrollView;
	private TextView mLanTestInfo;
	private ScrollView mLanScrollView;
	private Button btnStart;
	private Button btnSuccess;
	private Button btnFail;




	private final static int IFACE_LAN = 0;
	private final static int IFACE_WIFI = 1;
	private final static int IFACE_WIFI_5G = 2;
	private final static int IFACE_NULL = -1;

	private int mCurTestIface = IFACE_LAN;//当前测试的接口
	private int mFirstTestIface = IFACE_LAN;//按顺序自动测试WIFI和LAN，会根据当前链接的网络调整顺序
	private int mSecondTestIface = IFACE_WIFI;
	private int mThirdTestIface = IFACE_WIFI_5G;

	IperfTask iperfTask = null;
	String testCmd = null;

	private Context mContext;

	private IperfUtils mIperfUtils; 
	private Handler controlHandler;
	private Runnable checkRunnable;
	private Runnable testRunnable;

	private int MAX_RETRY_COUNT = 35;//切换网络连接时的超时时间
	private int retry_count = 0;
	
	
	private String mSpecifiedAp;
	private String mSpecifiedAp_5G;
	private String mPassword;
	private boolean needCheck5G = true;



	private final static int MSG_TEST_IF_NEED = 1;
	
	private WifiUtils mWifiUtils;

	public IperfTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		// TODO Auto-generated constructor stub
		mContext = context;
		mIperfUtils = new IperfUtils(mContext);
		mWifiUtils = new WifiUtils(mContext);

		initTest();
		
		mWifiUtils.registerReceiver();

	}
	
	@Override
    public boolean onTestHandled(TestResult result) {
       SystemUtils.setEthEnable(mContext,true);
       mWifiUtils.unregisterReceiver();
        return super.onTestHandled(result);
    }


	private void stopTest() {
		controlHandler.removeCallbacks(checkRunnable);
		controlHandler.removeCallbacks(testRunnable);
		if (iperfTask != null) {
			iperfTask.cancel(true);
		}
		mCurTestIface = IFACE_NULL;
		iperfTask = null;
	}

	private void initTest() {
		Map<String, String> attachParams = mTestCaseInfo.getAttachParams();
		mServerIP = attachParams.get(ParamConstants.SERVER_IP);
		mServerPort =attachParams.get(ParamConstants.SERVER_PORT);
		
		mSpecifiedAp = attachParams.get(ParamConstants.WIFI_AP);
	    mPassword = attachParams.get(ParamConstants.WIFI_PSW);
	    mSpecifiedAp_5G = mSpecifiedAp+"_5G";
	    	    
		needCheck5G = "1".equals(attachParams
				.get(ParamConstants.WIFI_NEED_CHECK_5G));
		


		mIperfUtils.setServerInfo(mServerIP, mServerPort);
		LogUtil.d(this, "server ip --->" + mServerIP);
		LogUtil.d(this, "server port--->" + mServerPort);
		view = LayoutInflater.from(mContext).inflate(R.layout.test_iperf, null);
		btnStart = (Button) view.findViewById(R.id.btnStart);
		btnStart.setEnabled(false);
		btnSuccess = (Button) view.findViewById(R.id.btnSuccess);
		btnFail = (Button) view.findViewById(R.id.btnFail);

		mLanTestInfo = (TextView)view.findViewById(R.id.lanTestInfo);
		mLanScrollView = (ScrollView)view.findViewById(R.id.lanScroller);

		mWifiTestInfo = (TextView)view.findViewById(R.id.wifiTestInfo);
		mWifiScrollView = (ScrollView)view.findViewById(R.id.wifiScroller);
		
		mWifi5GTestInfo = (TextView)view.findViewById(R.id.wifi5G_TestInfo);
		mWifi5GScrollView = (ScrollView)view.findViewById(R.id.wifi5G_Scroller);

		if(!needCheck5G)
		{
			view.findViewById(R.id.wifi5G_lauout).setVisibility(View.GONE);
		}

		btnStart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if ((mContext.getString(R.string.start)).equals(btnStart
						.getText().toString())) {
					btnStart.setEnabled(false);
					startTestIperf();
				} else {
					stopTest();
					btnStart.setText(R.string.start);
				}
			}


		});

		btnSuccess.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopTest();
				dismiss();
				onTestSuccess();
			}
		});
		btnFail.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				stopTest();
				dismiss();
				onTestFail(0);
			}
		});

		checkRunnable = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(mCurTestIface == IFACE_NULL)
					return;
				btnStart.setText(R.string.abort);
				btnStart.setEnabled(true);
				if(mCurTestIface == IFACE_LAN)
				{
					if(IperfUtils.isNetworkConnected(mContext))//以太网已连接
					{
						mLanTestInfo.append("switch to eth success" + " \n\n");
						mLanTestInfo.append(testCmd + " \n\n");
						controlHandler.removeCallbacks(testRunnable);
						controlHandler.post(testRunnable);
					}else{
						if(retry_count == MAX_RETRY_COUNT)
						{
							mLanTestInfo.append("connect to eth timeout,please trtry" + " \n\n");
							//btnStart.setText(R.string.start);
							controlHandler.sendEmptyMessage(MSG_TEST_IF_NEED);
						}else{
							retry_count ++;
							controlHandler.postDelayed(checkRunnable, 1000);							
						}
					}
				}else if(mCurTestIface == IFACE_WIFI)
				{
					if(IperfUtils.isNetworkConnected(mContext) && (!needCheck5G ||mIperfUtils.checkConnectSsid(mSpecifiedAp) ) )//wifi已连接
					{
						mWifiTestInfo.append("switch to wifi success" + " \n\n");
						mWifiTestInfo.append(testCmd + " \n\n");
						controlHandler.removeCallbacks(testRunnable);
						controlHandler.post(testRunnable);
					}else{
						if(retry_count == MAX_RETRY_COUNT)
						{
							mWifiTestInfo.append("connect to wifi timeout,please trtry" + " \n\n");
							//btnStart.setText(R.string.start);
							controlHandler.sendEmptyMessage(MSG_TEST_IF_NEED);
						}else{
							retry_count ++;
							controlHandler.postDelayed(checkRunnable, 1000);
						}
					}
				}else if(mCurTestIface == IFACE_WIFI_5G)
				{
					if(IperfUtils.isNetworkConnected(mContext) &&  mIperfUtils.checkConnectSsid(mSpecifiedAp_5G))//wifi已连接
					{
						mWifi5GTestInfo.append("switch to wifi success" + " \n\n");
						mWifi5GTestInfo.append(testCmd + " \n\n");
						controlHandler.removeCallbacks(testRunnable);
						controlHandler.post(testRunnable);
					}else{
						if(retry_count == MAX_RETRY_COUNT)
						{
							mWifi5GTestInfo.append("connect to wifi timeout,please trtry" + " \n\n");
							//btnStart.setText(R.string.start);
							controlHandler.sendEmptyMessage(MSG_TEST_IF_NEED);
						}else{
							retry_count ++;
							controlHandler.postDelayed(checkRunnable, 1000);
						}
					}
			}}
		};

		testRunnable = new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(mCurTestIface == IFACE_NULL)
					return;
				iperfTask.execute(testCmd);				
			}
		};

		controlHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);
				
					
					if(mCurTestIface == IFACE_NULL)
					{
						mCurTestIface = mFirstTestIface;
						testIface(mCurTestIface);
					}else if(mCurTestIface == mFirstTestIface) {
						mCurTestIface = mSecondTestIface;
						testIface(mCurTestIface);
					}else if(mCurTestIface == mSecondTestIface && needCheck5G)
					{
						mCurTestIface = mThirdTestIface;
						testIface(mCurTestIface);
					}else{
						btnStart.setText(R.string.start);
					}
				}

		};


	}

	public void startTestIperf()
	{
		LogUtil.d(this, "IperfUtils.isNetworkConnected(mContext)="+IperfUtils.isNetworkConnected(mContext)
				+"  ,IperfUtils.isWifi(mContext)="+IperfUtils.isWifi(mContext));
		if(IperfUtils.isNetworkConnected(mContext))
		{
			if(IperfUtils.isWifi(mContext))
			{
				if(!needCheck5G)
				{
					mFirstTestIface = IFACE_WIFI;
					mSecondTestIface = IFACE_LAN;
					mThirdTestIface = IFACE_WIFI_5G;		
				}
				else if(mIperfUtils.checkConnectSsid(mSpecifiedAp_5G))
				{
					mFirstTestIface = IFACE_WIFI_5G;
					mSecondTestIface = IFACE_WIFI;
					mThirdTestIface = IFACE_LAN;
		
				}else{
					mFirstTestIface = IFACE_WIFI;
					mSecondTestIface = IFACE_WIFI_5G;
					mThirdTestIface = IFACE_LAN;		
				}

			}else{
				mFirstTestIface = IFACE_LAN;
				mSecondTestIface = IFACE_WIFI;
				mThirdTestIface = IFACE_WIFI_5G;
			}
		}else{//如果无网络则按照初始顺序
			mFirstTestIface = IFACE_LAN;
			mSecondTestIface = IFACE_WIFI;
			mThirdTestIface = IFACE_WIFI_5G;
		}
		mCurTestIface = IFACE_NULL;

		LogUtil.d(this, "startTestIperf mFirstTestIface="+mFirstTestIface+"  ,mSecondTestIface="+mSecondTestIface+"   ,mCurTestIface="+mCurTestIface);

		controlHandler.sendEmptyMessage(MSG_TEST_IF_NEED);
		mLanTestInfo.setText("");
		mWifiTestInfo.setText("");
		mWifi5GTestInfo.setText("");

	}


	private void testIface(int iface)
	{
		retry_count = 0;
		if(iperfTask != null)
		{
			iperfTask.cancel(true);
			iperfTask = null;
		}
		testCmd = mIperfUtils.getTestCmd(IperfUtils.TYPE_TCP);

		if(iface == IFACE_LAN)
		{
			mIperfUtils.ethConnected();
			mLanTestInfo.setText("");
			mLanTestInfo.append("start switching to eth" + " \n");
			iperfTask = new IperfTask(mLanTestInfo, mLanScrollView);			
			controlHandler.postDelayed(checkRunnable,1000);
		}else if (iface == IFACE_WIFI)
		{
			mIperfUtils.wifiConnected();
			
				
			if(needCheck5G)
				mWifiUtils.connectWifi(mSpecifiedAp, mPassword);
			mWifiTestInfo.setText("");
			mWifiTestInfo.append("start switching to wifi" + " \n");
			iperfTask = new IperfTask(mWifiTestInfo, mWifiScrollView);
			controlHandler.postDelayed(checkRunnable,1000);
		}else if(iface == IFACE_WIFI_5G && needCheck5G)
		{
			mIperfUtils.wifiConnected();
			mWifiUtils.connectWifi(mSpecifiedAp_5G, mPassword);
			mWifi5GTestInfo.setText("");
			mWifi5GTestInfo.append("start switching to wifi" + " \n");
			iperfTask = new IperfTask(mWifi5GTestInfo, mWifi5GScrollView);
			controlHandler.postDelayed(checkRunnable,1000);
		}


	}


	public void enableBtn()
	{
		btnStart.setEnabled(true);
		btnSuccess.setEnabled(true);
		btnFail.setEnabled(true);

	}


	@Override
	public boolean onTesting() {
		// TODO Auto-generated method stub

		/*	AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.iperf);
		builder.setView(view);
		builder.setCancelable(false);
		dialog = builder.create();



		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				width / 2, height / 2);*/
		if(dialog == null)
		{
			dialog = new Dialog(mContext, R.style.MyDialog);
			dialog.setContentView(view);
			dialog.setCancelable(false);
		}
		dialog.show();
		startTestIperf();

		return super.onTesting();
	}

	private void dismiss() {
		if (dialog != null) {
			dialog.dismiss();
		}
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
					//解析返回数据的代码，暂时不需要
					/*String[] subs = output.toString().split("\n");
					for(int i=0;i<subs.length;i++)
					{
						String line = subs[i];

						if(start)
						{
							if(line.indexOf("[  3]") != -1)
							{
								IperfPack p = mIperfUtils.parseIperfMsg(line);
								if(p != null)packList.add(p);
							}
						}else{
							if(line.indexOf("[ ID]") != -1)
							{
								start = true;
							}
						}
						//if(!start)
							publishProgress(line+"\r\n");
					}*/
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
			btnStart.setText(R.string.start);
			mScroller.post(new Runnable() {
				public void run() {
					mScroller.smoothScrollTo(0, mTestInfo.getBottom());
				}
			});

			controlHandler.sendEmptyMessage(MSG_TEST_IF_NEED);
		}



	}
}
