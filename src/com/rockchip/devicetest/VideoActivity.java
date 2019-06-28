package com.rockchip.devicetest;

import com.rockchip.devicetest.testcase.impl.HDMITest;
import com.rockchip.devicetest.view.VideoView;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;

public class VideoActivity extends FullscreenActivity 
		 {

	private VideoView mVideoView;
	private Button btnSuccess;
	private Button btnFail;
	private Button btnScale;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.test_video);
		mVideoView = (VideoView) findViewById(R.id.test_vv);
		btnSuccess = (Button) findViewById(R.id.success);
		btnFail = (Button) findViewById(R.id.fail);
		btnScale = (Button) findViewById(R.id.scale);
		btnSuccess.setFocusable(true);
		btnSuccess.setFocusableInTouchMode(true);
		btnSuccess.requestFocus();

		mVideoView.setVideoPath(HDMITest.videoPath);
		Log.v("VideoActivity", "videoPath--->" + HDMITest.videoPath);
		mVideoView.setFocusable(false);
		int winHeight = this.getResources().getDisplayMetrics().heightPixels;
		int winWidth = this.getResources().getDisplayMetrics().widthPixels;
		mVideoView.setMinimumHeight((int) (winHeight * 0.7));
		mVideoView.setMinimumWidth((int) (winHeight * 0.7 * 1.5));
		mVideoView.setOnPreparedListener(new OnPreparedListener() {

			@Override
			public void onPrepared(MediaPlayer mp) {
				// TODO Auto-generated method stub
				mp.start();
			}
		});
		btnSuccess.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mVideoView.stopPlayback();
				setResult(111);
				finish();
			}
		});
		btnFail.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mVideoView.stopPlayback();
				setResult(222);
				finish();
			}
		});
		btnScale.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (getString(R.string.screen_full).equals(
						btnScale.getText().toString())) {
					btnScale.setText(R.string.scale);
					RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
							RelativeLayout.LayoutParams.MATCH_PARENT,
							RelativeLayout.LayoutParams.MATCH_PARENT);
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					mVideoView.setLayoutParams(layoutParams);
				} else {
					btnScale.setText(R.string.screen_full);
					RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
							mVideoView.getMinimumWidth(), mVideoView
									.getMinimumHeight());
					lp.addRule(RelativeLayout.CENTER_IN_PARENT);
					mVideoView.setLayoutParams(lp);
				}
			}
		});
	}
}
