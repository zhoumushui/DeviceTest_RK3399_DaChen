/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月13日 上午9:04:27  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月13日      fxw         1.0         create
 *******************************************************************/   

package com.rockchip.devicetest.testcase.impl;

import java.io.IOException;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.model.TestResult;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.SystemUtils;

public class AudioChannelTest extends BaseTestCase {

	public static final String AUDIO_FILE = "test_music.mp3";
	private MediaPlayer mPlayer;
	private AudioManager mAudioManager;
	private boolean leftEnable = true;//左声道
	private boolean rightEnable = false;//右声道
	private int mOldVolume;
	private boolean mSpeakerOn;

	public AudioChannelTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
	}

	public boolean onTesting() {
		//View view = mLayoutInflater.inflate(R.layout.test_key, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.audio_title);
		//builder.setView(view);
		builder.setSingleChoiceItems(R.array.test_audio, 0, new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(which==0){
					leftEnable = true;
					rightEnable = false;
				}else if(which==1){
					leftEnable = false;
					rightEnable = true;
				}else if(which==2){
					leftEnable = true;
					rightEnable = true;
				}
				if(mPlayer!=null)
					mPlayer.setVolume(leftEnable ? 1 : 0, rightEnable ? 1 : 0);
			}
		});
		builder.setPositiveButton(mContext.getString(R.string.pub_success), new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				onTestSuccess();
			}
		});
		builder.setNegativeButton(mContext.getString(R.string.pub_fail), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				onTestFail(0);
			}
		});
		builder.setCancelable(false);
		AlertDialog dialog = builder.create();
		dialog.show();
		startPlay();
		return super.onTesting();
	}

	public boolean onTestHandled(TestResult result) {
		stopPlay();
		mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, false);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mOldVolume, 0);
		if(!mSpeakerOn){
			this.mAudioManager.setSpeakerphoneOn(false);
		}
		return super.onTestHandled(result);
	}

	/**
	 * 开始播放
	 */
	private void startPlay(){
		stopPlay();
		initMusic();
		this.mAudioManager.setStreamSolo(AudioManager.STREAM_MUSIC, true);
		int i = this.mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mOldVolume = i;
		//根据配置文件参数设置音量
		Map<String, String> attachParams = mTestCaseInfo.getAttachParams();
		String mVolume = attachParams.get(ParamConstants.AUDIO_CHANNEL_TEST_VOLUME_PERCENT);
		SystemUtils.setVolume(mContext, mVolume);
		this.mSpeakerOn = this.mAudioManager.isSpeakerphoneOn();
		if (!this.mSpeakerOn) {
			this.mAudioManager.setSpeakerphoneOn(true);
		}
		mPlayer.setVolume(leftEnable ? 1 : 0, rightEnable ? 1 : 0);
		this.mPlayer.start();
	}

	//停止播放
	private void stopPlay(){
		if(mPlayer == null) {
			return;
		}
		mPlayer.stop();
		this.mPlayer.release();
		this.mPlayer = null;
	}

	private void initMusic(){
		mPlayer = new MediaPlayer();
		try {
			AssetFileDescriptor fd = mContext.getAssets().openFd(AUDIO_FILE);
			mPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(),
					fd.getDeclaredLength());
			mPlayer.prepare();
			mPlayer.setLooping(true);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
