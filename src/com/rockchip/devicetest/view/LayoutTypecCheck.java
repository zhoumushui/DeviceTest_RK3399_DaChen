package com.rockchip.devicetest.view;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.testcase.LEDSettings.LEDMode;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LayoutTypecCheck extends LinearLayout {
	private TextView mTitleView;
	private Button mCheckBtn1,mCheckBtn2;
	Drawable mBackground = null;
	public interface OnMyClickListener{
		public void onBtnPositiveClick(Button btn);
		public void onBtnNegativeClick(Button btn);
	}
	OnMyClickListener mOnMyClickListener;
	public void setOnMyClickListener(OnMyClickListener listener)
	{
		this.mOnMyClickListener = listener;
	}
	public enum CheckMode {
		SUCCESS("2"), FAIL("1"),UNCHECK("0");

		public String value;

		private CheckMode(String value) {
			this.value = value;
		}
		public String getValueStr()
		{
			if(this == CheckMode.SUCCESS)
			{
				return "成功";
			}else if(this == CheckMode.FAIL)
			{
				return "失败";
			}
			
			return "未检查";
		}

		public static CheckMode getMode(String mode) {
			for (CheckMode um : CheckMode.values()) {
				if (um.value.equals(mode)) {
					return um;
				}
			}
			return null;
		}
	}
	private CheckMode mCheckMode = CheckMode.UNCHECK;
	
	public CheckMode getCheckMode() {
		return mCheckMode;
	}
	
	public void setCheckMode(CheckMode mode) {
		this.mCheckMode = mode;
		Log.v("TypecTest", "setCheckMode "+mTitleView.getText()+" ,mCheckMode:"+mCheckMode.value);
	}

	public boolean isChecked()
	{
		return mCheckMode != CheckMode.UNCHECK;
	}
	public boolean isCheckSuccess()
	{
		return mCheckMode== CheckMode.SUCCESS;
	}
	public LayoutTypecCheck(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		// TODO Auto-generated constructor stub
	}

	public LayoutTypecCheck(Context context) {
		this(context, null);
		// TODO Auto-generated constructor stub
	}

	public LayoutTypecCheck(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub

		LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.layout_typec_check, this);
		mTitleView = (TextView)findViewById(R.id.typec_check_title);
		mCheckBtn1 = (Button)findViewById(R.id.typec_check_btn_1);
		mCheckBtn2 = (Button)findViewById(R.id.typec_check_btn_2);
		mBackground = mCheckBtn2.getBackground();
		mCheckBtn1.setOnClickListener(ocl);
		mCheckBtn2.setOnClickListener(ocl);

		TypedArray a = context.obtainStyledAttributes(attrs,  
				R.styleable.LayoutTypecCheck);  

		String title = a.getString(R.styleable.LayoutTypecCheck_title);
		String bnt1_title = a.getString(R.styleable.LayoutTypecCheck_button1_title);
		String bnt2_title = a.getString(R.styleable.LayoutTypecCheck_button2_title);
		a.recycle();  

		mTitleView.setText(title);
		if(bnt1_title != null && bnt1_title.length() >0)
		{
			mCheckBtn1.setText(bnt1_title);
		}else{
			mCheckBtn1.setText(R.string.typec_btn_success);
		}
		
		if(bnt2_title != null && bnt2_title.length() >0)
		{
			mCheckBtn2.setText(bnt2_title);
		}else{
			mCheckBtn2.setVisibility(View.GONE);
		}
	}
	OnClickListener ocl = new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			switch (arg0.getId()) {
			case R.id.typec_check_btn_1:
				if(mOnMyClickListener != null)mOnMyClickListener.onBtnPositiveClick(mCheckBtn1);
				//setButtonBackgroundDown(mCheckBtn1);
				break;
			case R.id.typec_check_btn_2:
				if(mOnMyClickListener != null)mOnMyClickListener.onBtnNegativeClick(mCheckBtn2);
				//setButtonBackgroundDown(mCheckBtn2);
				break;
			default:
				break;
			}
		}
	};
	
	public void setButtonBackgroundDown(Button btn) {
		if(mBackground != null){
			mCheckBtn1.setBackground(mBackground);
			mCheckBtn2.setBackground(mBackground);
		}else{
			mCheckBtn1.setBackgroundColor(Color.BLUE);
			mCheckBtn2.setBackgroundColor(Color.BLUE);
		}
		btn.setBackgroundColor(Color.GREEN);
		
	}
	
	
	public void setButtonBackgroundDown(Button btn,int color) {
		if(mBackground != null){
			mCheckBtn1.setBackground(mBackground);
			mCheckBtn2.setBackground(mBackground);
		}else{
			mCheckBtn1.setBackgroundColor(Color.BLUE);
			mCheckBtn2.setBackgroundColor(Color.BLUE);
		}
		btn.setBackgroundColor(color);
		
	}
}
