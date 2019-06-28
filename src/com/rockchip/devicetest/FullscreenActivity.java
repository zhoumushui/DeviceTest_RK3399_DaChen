package com.rockchip.devicetest;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.os.Build;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.content.pm.ActivityInfo;
import android.util.Log;

public class FullscreenActivity extends Activity implements OnSystemUiVisibilityChangeListener {
	
	private static final int SYSTEM_UI_FLAG_SHOW_FULLSCREEN = 0x00000008;  //View.SYSTEM_UI_FLAG_SHOW_FULLSCREEN
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
		getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_SHOW_FULLSCREEN);
		getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);
		
		hideSystemUI();
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		 if (hasFocus) {
		 	hideSystemUI();
		 }
	}

	@Override
	public void onSystemUiVisibilityChange(int visibility) {
		hideSystemUI();

	}
	private void hideSystemUI()
	{
		getWindow().getDecorView().setSystemUiVisibility(
			View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                		| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                		| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                		| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                		| View.SYSTEM_UI_FLAG_FULLSCREEN
                		| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int km = KeyEvent.KEYCODE_MENU;
		if(keyCode==KeyEvent.KEYCODE_MENU){
			return true ;
		}
		return super.onKeyDown(keyCode, event);
	}
	

}
