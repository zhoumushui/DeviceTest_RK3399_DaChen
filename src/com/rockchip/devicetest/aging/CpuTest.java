/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月14日 下午5:42:07  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月14日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.aging;


import com.rockchip.devicetest.R;
import com.rockchip.devicetest.aging.cpu.CpuInfoReader;
import com.rockchip.devicetest.aging.cpu.LinpackLoop;
import com.rockchip.devicetest.utils.SharedPreferencesEdit;
import com.rockchip.devicetest.utils.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

public class CpuTest extends BaseAgingTest {

	public static final int UPDATE_DELAY = 1000;
	private TextView mCpuModelText;
	private TextView mCpuCoreNumText;
	private TextView mCpuFreqText;
	private TextView mCpuCurrFreqText;
	private TextView mCpuUsageText;
	private TextView mCpuTemperatureText;
	private TextView mCpuTemperatureTitleText;
	private TextView mSystemRunTimeText;
	private TextView mSystemAbnormalRestartTimeText;
	private TextView AgingTestTimesTextView;
	private boolean isRunning;
	private long[] mCpuInfo = new long[2];
	private Handler mMainHandler = new Handler();
	
	public CpuTest(AgingConfig config, AgingCallback agingCallback){
		super(config, agingCallback);
	}
	
	@Override
	public void onCreate(Context context, View view) {
		// TODO Auto-generated method stub
		mCpuModelText = (TextView)view.findViewById(R.id.tv_cpu_model);
		mCpuCoreNumText = (TextView)view.findViewById(R.id.tv_cpu_corenum);
		mCpuFreqText = (TextView)view.findViewById(R.id.tv_cpu_freq);
		mCpuCurrFreqText = (TextView)view.findViewById(R.id.tv_cpu_currfreq);
		mCpuUsageText = (TextView)view.findViewById(R.id.tv_cpu_usage);
		mCpuTemperatureText = (TextView)view.findViewById(R.id.tv_cpu_temperature);
		mCpuTemperatureTitleText = (TextView)view.findViewById(R.id.tv_cpu_temperature_title);
		mSystemRunTimeText = (TextView)view.findViewById(R.id.system_run_time);
		mSystemAbnormalRestartTimeText = (TextView)view.findViewById(R.id.system_abnormal_restart_times);
		AgingTestTimesTextView = (TextView) view.findViewById(R.id.aging_test_times_view);
		
		isRunning = true;
	}


	@Override
	public void onStart() {
		int coreNum = CpuInfoReader.getCpuCores();
		String freqRange = CpuInfoReader.getCpuMinFreq()/1000+"~"+CpuInfoReader.getCpuMaxFreq()/1000+" MHz";
		mCpuModelText.setText(CpuInfoReader.getCpuModel());//CPU型号
		mCpuCoreNumText.setText(coreNum<=0?"Unknow":coreNum+"");//CPU核心数
		mCpuFreqText.setText(freqRange);//CPU频率范围
		mCpuCurrFreqText.setText(CpuInfoReader.getCpu0CurrentFreq()/1000+" MHz"+","+CpuInfoReader.getCpu4CurrentFreq()/1000+" MHz");//CPU当前频率
		mSystemAbnormalRestartTimeText.setText(""+SharedPreferencesEdit.getInstance().getSystemAbnormalRestartTime());
		AgingTestTimesTextView.setText(""+SharedPreferencesEdit.getInstance().getmAgingTestTimes());
		mMainHandler.postDelayed(mUpdateAction, 50);//CPU使用率
		for(int i=0; i<0; i++){
			new Thread(){
				public void run() {
					while(isRunning){
						LinpackLoop.main();
					}
				};
			}.start();
		}
	}
	
	private Runnable mUpdateAction = new Runnable(){
		public void run() {
			updateCpuUsage();
			updateCpuTemperature();
			updateSystemInfo();
			mMainHandler.postDelayed(this, UPDATE_DELAY);
			mCpuCurrFreqText.setText(CpuInfoReader.getCpu0CurrentFreq()/1000+" MHz"+","+CpuInfoReader.getCpu4CurrentFreq()/1000+" MHz");//CPU当前频率
		};
	};
	
	public void updateCpuUsage(){
		long[] cpuInfo = CpuInfoReader.getCpuTime();
		if(cpuInfo[0]==0||cpuInfo[1]==0){
			return;
		}
		if(mCpuInfo[0]==0||mCpuInfo[1]==0){
			mCpuInfo = cpuInfo;
			return;
		}
		long totalTime = cpuInfo[0]-mCpuInfo[0];
		long iddleTime = cpuInfo[1]-mCpuInfo[1];
		int percent = (int)((totalTime-iddleTime)*1.00f/totalTime*100);
		if(percent==0) percent = 1;
		mCpuUsageText.setText(percent+"%");
		mCpuInfo = cpuInfo;
	}

	public void updateCpuTemperature(){
		String temperature = CpuInfoReader.getCpuTemperature();
		if(temperature != null && temperature.length()>0) {
			mCpuTemperatureText.setText(temperature);
			mCpuTemperatureText.setVisibility(View.VISIBLE);
			mCpuTemperatureTitleText.setVisibility(View.VISIBLE);
		} else {
			mCpuTemperatureText.setVisibility(View.GONE);
			mCpuTemperatureTitleText.setVisibility(View.GONE);
		}
		
	}
	
	public void updateSystemInfo(){
		mSystemRunTimeText.setText(StringUtils.generateTime(SystemClock.elapsedRealtime()));
		mSystemAbnormalRestartTimeText.setText(""+SharedPreferencesEdit.getInstance().getSystemAbnormalRestartTime());

	}
	
	@Override
	public void onStop() {
		isRunning = false;
	}

	@Override
	public void onDestroy() {

	}

	@Override
	public void onFailed() {
		isRunning = false;
	}


}
