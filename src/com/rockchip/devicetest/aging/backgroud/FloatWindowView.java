package com.rockchip.devicetest.aging.backgroud;

import java.lang.reflect.Field;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.service.WatchdogService;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FloatWindowView extends LinearLayout {

	/**
	 * 记录小悬浮窗的宽度
	 */
	public static int viewWidth;

	/**
	 * 记录小悬浮窗的高度
	 */
	public static int viewHeight;

	/**
	 * 记录系统状态栏的高度
	 */
	 private static int statusBarHeight;

	/**
	 * 用于更新小悬浮窗的位置
	 */
	private WindowManager windowManager;

	/**
	 * 小悬浮窗的参数
	 */
	private WindowManager.LayoutParams mParams;

	/**
	 * 记录当前手指位置在屏幕上的横坐标值
	 */
	private float xInScreen;

	/**
	 * 记录当前手指位置在屏幕上的纵坐标值
	 */
	private float yInScreen;

	/**
	 * 记录手指按下时在屏幕上的横坐标的值
	 */
	private float xDownInScreen;

	/**
	 * 记录手指按下时在屏幕上的纵坐标的值
	 */
	private float yDownInScreen;

	/**
	 * 记录手指按下时在小悬浮窗的View上的横坐标的值
	 */
	private float xInView;

	/**
	 * 记录手指按下时在小悬浮窗的View上的纵坐标的值
	 */
	private float yInView;
	
	private View float_agingtest_layout;
	private Context mContext;
	
	public FloatWindowView(Context context) {
		super(context);
		mContext = context;
		windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		LayoutInflater.from(context).inflate(R.layout.float_window, this);
		float_agingtest_layout = findViewById(R.id.float_agingtest_layout);
		viewWidth = float_agingtest_layout.getLayoutParams().width;
		viewHeight = float_agingtest_layout.getLayoutParams().height;
//		TextView percentView = (TextView) findViewById(R.id.percent);
//		percentView.setText(MyWindowManager.getUsedPercentValue(context));
		Button exitBtn = (Button)this.findViewById(R.id.exit);
		exitBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();//启动后台服务
				intent.putExtra("STOP_TEST", true);
				intent.setClass(mContext, AgingTestService.class);
				mContext.startService(intent);;
			}
		});
	}
	
	public View getFloatLayout()
	{
		return float_agingtest_layout;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
			xInView = event.getX();
			yInView = event.getY();
			xDownInScreen = event.getRawX();
			yDownInScreen = event.getRawY() - getStatusBarHeight();
			xInScreen = event.getRawX();
			yInScreen = event.getRawY() - getStatusBarHeight();
			break;
		case MotionEvent.ACTION_MOVE:
			xInScreen = event.getRawX();
			yInScreen = event.getRawY() - getStatusBarHeight();
			// 手指移动的时候更新小悬浮窗的位置
			updateViewPosition();
			break;
		case MotionEvent.ACTION_UP:
			// 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
			if (xDownInScreen == xInScreen && yDownInScreen == yInScreen) {
				//openBigWindow();
			}
			break;
		default:
			break;
		}
		return true;
	}

	/**
	 * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
	 * 
	 * @param params
	 *            小悬浮窗的参数
	 */
	public void setParams(WindowManager.LayoutParams params) {
		mParams = params;
	}

	/**
	 * 更新小悬浮窗在屏幕中的位置。
	 */
	private void updateViewPosition() {
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		mParams.x = screenWidth-viewWidth - (int) (xInScreen - xInView);
		mParams.y = (int) (yInScreen - yInView);
		//Log.v("sjf","screenWidth:"+screenWidth+"viewWidth:"+viewWidth+",mParams.x:"+mParams.x+",xInScreen:"+xInScreen+",xInView:"+xInView);
		windowManager.updateViewLayout(this, mParams);
	}

//	/**
//	 * 打开大悬浮窗，同时关闭小悬浮窗。
//	 */
//	private void openBigWindow() {
//		MyWindowManager.createBigWindow(getContext());
//		MyWindowManager.removeSmallWindow(getContext());
//	}

	/**
	 * 用于获取状态栏的高度。
	 * 
	 * @return 返回状态栏高度的像素值。
	 */
	private int getStatusBarHeight() {
		if (statusBarHeight == 0) {
			try {
				Class<?> c = Class.forName("com.android.internal.R$dimen");
				Object o = c.newInstance();
				Field field = c.getField("status_bar_height");
				int x = (Integer) field.get(o);
				statusBarHeight = getResources().getDimensionPixelSize(x);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return statusBarHeight;
	}

}