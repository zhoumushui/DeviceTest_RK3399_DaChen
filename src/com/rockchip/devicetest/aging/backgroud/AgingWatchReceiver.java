package com.rockchip.devicetest.aging.backgroud;

import com.rockchip.devicetest.service.WatchdogService;
import com.rockchip.devicetest.utils.AppUtils;
import com.rockchip.devicetest.utils.LogUtil;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

public class AgingWatchReceiver extends BroadcastReceiver{
	private static final int PERIOD = 15*1000; // 1 minutes
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		LogUtil.d(AgingWatchReceiver.this, "action:"+arg1.getAction());
		if("devicetest.aging.watchdog".equals(arg1.getAction()))
		{
			boolean watch_running = AppUtils.isServiceRunning(arg0.getApplicationContext(), "com.rockchip.devicetest.service.WatchdogService");
			LogUtil.d(AgingWatchReceiver.this, "watch_running:"+watch_running);
			if(!watch_running)
			{
				Intent agingIntent = new Intent();//启动后台服务
				agingIntent.setAction("com.rockchip.devicetest.action.WATCH_DOG");
				agingIntent.putExtra(WatchdogService.COMMAND, WatchdogService.CMD_RESTART_WATCHDOG);
				agingIntent.putExtra(WatchdogService.USE_3DTEST, true);
				agingIntent.setClass(arg0, WatchdogService.class);
				arg0.startService(agingIntent);
			}
			scheduleAlarms(arg0);
		}else if("devicetest.aging.stop.watchdog".equals(arg1.getAction()))
		{
			Intent agingIntent = new Intent();//agingTestActivity 进入后台时，关闭黄色led灯
			agingIntent.setAction("com.rockchip.devicetest.action.WATCH_DOG");
			agingIntent.putExtra(WatchdogService.COMMAND, WatchdogService.CMD_STOP_AGING);
			agingIntent.setClass(arg0, WatchdogService.class);
			arg0.startService(agingIntent);	
		}
	}
	static final int requestCode = 101;
	public  static void scheduleAlarms(Context ctxt) {
		AlarmManager mgr = (AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(ctxt, AgingWatchReceiver.class);
		i.setAction("devicetest.aging.watchdog");
		PendingIntent pi = PendingIntent.getBroadcast(ctxt, requestCode, i, 0);
		mgr.cancel(pi);
		mgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+PERIOD, pi);
	}
	public static void cancelAlarms(Context ctxt)
	{
		AlarmManager mgr = (AlarmManager) ctxt.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(ctxt, AgingWatchReceiver.class);
		i.setAction("devicetest.aging.watchdog");
		PendingIntent pi = PendingIntent.getBroadcast(ctxt, requestCode, i, 0);
		mgr.cancel(pi);
	}

}
