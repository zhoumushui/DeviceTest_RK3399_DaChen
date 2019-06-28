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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;








import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.serialport.SerialPort;
import com.rockchip.devicetest.serialport.SerialPortFinder;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.StringUtils;

public class SerialPortTest extends BaseTestCase {

	private static final String TAG = "SerialPortTest";

	class TestInfo{
		String name;
		String path;

		public TestInfo(String name, String path) {
			super();
			this.name = name;
			this.path = path;
		}
		public String getPath() {
			return path;
		}
		public void setPath(String path) {
			this.path = path;
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
	}

	private String serial_port_path;
	private ArrayList<TestInfo> serialPortList;
	private String cmd1;
	private String cmd2;
	private int baudrate;//4800,9600,19200
	private static final String DEFAULT_CMD1="1122334455";
	private static final String DEFAULT_CMD2="5544332211";
	private static final int DEFAULT_BAUDRATE=19200;

	private static final int TEST_CMD1 = 0;
	private static final int TEST_CMD2 = 1;


	private final static int MSG_TEST_SERIAL_PORT_START = 0;
	private final static int MSG_TEST_SERIAL_PORT_END = 1;
	private final static int MSG_TEST_CMD_START = 2;
	private final static int MSG_TEST_CMD_END = 3;
	private final static int MSG_CHECK_TEST_SERIAL_PORT_END = 4;
	private final static int DEFAULT_TEST_TIME_OUT = 3000;
	private int curTestCmd = TEST_CMD1;
	private int curSerialPort = 0;

	private StringBuilder cmdResult;;
	private StringBuilder testCallBack ;

	private boolean cmd1Success = false;
	private boolean cmd2Success = false;

	private boolean testSuccess = true;

	private SerialPort mSerialPort = null;
	public SerialPortFinder mSerialPortFinder = new SerialPortFinder();

	public SerialPortTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		mContext = context;

	}

	@Override
	public void onTestInit() {
		super.onTestInit();
		cmdResult = new StringBuilder();
		Map<String, String> attachParams = mTestCaseInfo.getAttachParams();
		serial_port_path = attachParams.get(ParamConstants.SERIAL_PORT_PATH);
		//serial_port_path = "/dev/ttyS0;/dev/ttyS3;/dev/ttySAC0;/dev/ttySAC1";
		cmd1 = StringUtils.getStringValue(
				attachParams.get(ParamConstants.SERIAL_PORT_CMD1),
				DEFAULT_CMD1);
		cmd2 = StringUtils.getStringValue(
				attachParams.get(ParamConstants.SERIAL_PORT_CMD2),
				DEFAULT_CMD2);	
		baudrate = StringUtils.parseInt(
				attachParams.get(ParamConstants.SERIAL_PORT_BAUDRATE),
				DEFAULT_BAUDRATE);
		Log.v(TAG, "serial_port_path="+serial_port_path);
		Log.v(TAG, "cmd1="+cmd1);
		Log.v(TAG, "cmd2="+cmd2);
		Log.v(TAG, "baudrate="+baudrate);
		testSuccess = true;
		parseSerialPort();
		curSerialPort = 0;
		curTestCmd = TEST_CMD1;
		testCallBack = new StringBuilder();

	}
	private void parseSerialPort()
	{
		if(serial_port_path == null || serial_port_path.length() == 0)return;
		serialPortList = new ArrayList<TestInfo>();
		if(serial_port_path.indexOf(";") < 0)
		{
			File f = new File(serial_port_path);
			serialPortList.add(new TestInfo(f.getName(), f.getAbsolutePath()));
		}else{
			String[] strarray=serial_port_path.split(";"); 
			for (int i = 0; i < strarray.length; i++)
			{
				if(strarray[i].trim().length() == 0)continue;
				File f = new File(strarray[i]);
				serialPortList.add(new TestInfo(f.getName(), f.getAbsolutePath()));

			}

		}
	}

	@Override
	public boolean onTesting() {
		if(serialPortList == null || serialPortList.size() == 0)onTestFail(R.string.serial_port_error1);

		mHandler.sendEmptyMessage(MSG_TEST_SERIAL_PORT_START);
		return true;
	}
	private boolean setSerialPort(String path)
	{
		if (mReadThread != null)
			mReadThread.interrupt();		
		if(mSerialPort != null)mSerialPort.close();
		mSerialPort = null;
		try {
			mSerialPort = new SerialPort(new File(path), baudrate, 0);
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();

			mReadThread = new ReadThread();
			mReadThread.start();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(TAG, "open serialport("+path +") error:"+e.toString());
			//testCallBack.append("open serialport error:"+e.toString());
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(TAG, "open serialport("+path +") error:"+e.toString());
			//testCallBack.append("open serialport error:"+e.toString());
			return false;
		}
		return true;
		/*if(mSerialPort != null)
		{
			Log.v(TAG, "mSerialPort !=null");
			return true;
		}else{
			Log.v(TAG, "mSerialPort ==null");
			return false;
		}		*/
	}
	private void sendMsg(String cmd)
	{
		char[] text = new char[cmd.length()];
		for (int i = 0; i < cmd.length(); i++) {
			text[i] = cmd.charAt(i);
		}
		try {

			mOutputStream.write(new String(text).getBytes());
			mOutputStream.write('\n');

		} catch (IOException e) {
			e.printStackTrace();
			onTestFail("error:"+e.toString());
		}
	}

	@Override
	public boolean onTestHandled(TestResult result) {
		if (mReadThread != null)
			mReadThread.interrupt();		
		if(mSerialPort != null)mSerialPort.close();
		mSerialPort = null;
		return super.onTestHandled(result);
	}

	protected OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;

	private class ReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while (!isInterrupted()) {
				int size;
				try {
					byte[] buffer = new byte[64];
					if (mInputStream == null) {
						return;
					}
					size = mInputStream.read(buffer);
					if (size > 0) {
						onDataReceived(buffer, size);
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}
	private static final String ERROR_MSG_KEY = "error_msg_key";
	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case MSG_TEST_SERIAL_PORT_START:
				cmd1Success = false;
				cmd2Success = false;
				curTestCmd = TEST_CMD1;
				boolean set_port = setSerialPort(serialPortList.get(curSerialPort).getPath());
				mHandler.sendEmptyMessageDelayed(MSG_CHECK_TEST_SERIAL_PORT_END, DEFAULT_TEST_TIME_OUT);
				if(set_port)
					mHandler.sendEmptyMessage(MSG_TEST_CMD_START);
				else{
					Message message = Message.obtain();  
					message.what = MSG_TEST_SERIAL_PORT_END;  
					Bundle bundleData = new Bundle();  
					bundleData.putString(ERROR_MSG_KEY, "Open Error");  
					message.setData(bundleData); 
					mHandler.sendMessage(message);
				}
				break;
			case MSG_TEST_SERIAL_PORT_END:
				mHandler.removeMessages(MSG_CHECK_TEST_SERIAL_PORT_END);
				testSuccess = testSuccess && cmd1Success && cmd2Success;
				Bundle b = msg.getData();
				String error = b.getString(ERROR_MSG_KEY, "");
				if(error.length() > 0)
				{
					testCallBack.append(serialPortList.get(curSerialPort).getName()+":"+((cmd1Success && cmd2Success)?"success":"fail")+"-"+error+";");
				}else{
					testCallBack.append(serialPortList.get(curSerialPort).getName()+":"+((cmd1Success && cmd2Success)?"success":"fail")+";");
				}				
				if(++curSerialPort >= serialPortList.size())
				{
					if(testSuccess)
						onTestSuccess(testCallBack.toString());
					else
					{
						onTestFail(testCallBack.toString());	
					}
				}else{
					mHandler.sendEmptyMessage(MSG_TEST_SERIAL_PORT_START);
				}
				break;
			case MSG_TEST_CMD_START:
				cmdResult = new StringBuilder();
				if(curTestCmd == TEST_CMD1)
				{
					sendMsg(cmd1);
				}else{//TEST_CMD2
					sendMsg(cmd2);
				}

				break;
			case MSG_TEST_CMD_END:
				if(curTestCmd == TEST_CMD1)
				{
					cmd1Success = cmd1.equals(cmdResult.toString());
					//testCallBack.append("cmd1="+cmd1+",result="+cmdResult.toString()+","+cmd1Success+";");
					curTestCmd = TEST_CMD2;
					mHandler.sendEmptyMessage(MSG_TEST_CMD_START);
				}else{//TEST_CMD2
					cmd2Success = cmd2.equals(cmdResult.toString());
					//testCallBack.append("cmd2="+cmd2+",result="+cmdResult.toString()+","+cmd2Success+";");
					mHandler.sendEmptyMessage(MSG_TEST_SERIAL_PORT_END);
				}
				break;
			case MSG_CHECK_TEST_SERIAL_PORT_END:
				Message message = Message.obtain();  
				message.what = MSG_TEST_SERIAL_PORT_END;  
				Bundle bundleData = new Bundle();  
				bundleData.putString(ERROR_MSG_KEY, "TimeOut");  
				message.setData(bundleData); 
				mHandler.sendMessage(message);
				break;
			default:
				break;
			}

		}};


		protected void onDataReceived(final byte[] buffer, final int size) {
			String last_char = null;
			String result = new String(buffer, 0, size);
			if(size == 1)
			{
				last_char = new String(buffer, 0, size);

			}else if(size > 1){
				last_char = new String(buffer, size-1, 1);
			}
			if(cmdResult != null && result != null)
			cmdResult.append(result.trim());
			//Log.v(TAG, "onDataReceived :"+result+",size="+size+",last_string="+last_char);
			if("\r\n".equals(last_char) || "\n".equals(last_char))
			{
				Log.v(TAG, "onDataReceived :ths last char is \\r\\n");
				mHandler.sendEmptyMessage(MSG_TEST_CMD_END);
			}


		}


}
