package com.rockchip.devicetest;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import twinone.lib.androidtools.shell.Command;
import twinone.lib.androidtools.shell.Shell;

import com.rockchip.devicetest.testcase.impl.CameraTest;
import com.rockchip.devicetest.utils.AppUtils;
import com.rockchip.devicetest.utils.LogUtil;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class CameraTestActivity extends Activity implements SurfaceHolder.Callback {
	private Button back;//返回和切换前后置摄像头
	private SurfaceView surface;
	private Button shutter;//快门
	private SurfaceHolder holder;
	private Camera camera;//声明相机
	private String filepath =  Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/DeviceTestDir";//Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();//照片保存路径


	RadioGroup mCameraGroup = null;
	ArrayList<RadioButton> mCameraList = new ArrayList<RadioButton>();
	String[] cameraIds = null;
	int curIndex = 0;
	@SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//拍照过程屏幕一直处于高亮
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		setContentView(R.layout.layout_camera_test);

		back = (Button) findViewById(R.id.camera_back);
		surface = (SurfaceView) findViewById(R.id.camera_surface);
		shutter = (Button) findViewById(R.id.camera_shutter);
		mCameraGroup = (RadioGroup)findViewById(R.id.camera_group);
		Button goGallery = (Button)findViewById(R.id.camera_go_gallery);

		clearCameraProfile();
		
		holder = surface.getHolder();//获得句柄
		holder.addCallback(this);//添加回调
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);//surfaceview不维护自己的缓冲区，等待屏幕渲染引擎将内容推送到用户面前

		//设置监听
		back.setOnClickListener(listener);
		shutter.setOnClickListener(listener);
		goGallery.setOnClickListener(listener);
		Log.v("sjfcamera","Camera.getNumberOfCameras():"+Camera.getNumberOfCameras());
		initCameraGroup();
	}
	
	private void clearCameraProfile()
	{
		Log.d("CameraTest","clearCameraProfile");
		Shell mShell = new Shell();
		Command ls_cmd = mShell.execute("vm -c 'ls /data/camera/media_profiles.xml'");
		if(ls_cmd.exitStatus ==0)
		{
			Log.d("CameraTest"," camera profile exists,delete now");
			Command rm_cmd = mShell.execute("vm -c 'rm /data/camera/media_profiles.xml'");
			if(rm_cmd.exitStatus == 0)
			{
				Log.d("CameraTest","delete camera profile successed");
			}else{
				Log.d("CameraTest","delete camera profile failed");
			}
		}else{
			Log.d("CameraTest"," camera profile not exists");
		}
		
	}
	@SuppressLint("NewApi")
	private void initCameraGroup()
	{
		CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
		try { 
			cameraIds = manager.getCameraIdList();
			for (String cameraid : cameraIds) {
				Log.v("sjfcamera","cameraid:"+cameraid);
			}
		} catch (CameraAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(cameraIds != null && cameraIds .length >0)
		{
			mCameraList.clear();
			for (String cameraId : cameraIds) {
				RadioButton tempButton = new RadioButton(this);    
				tempButton.setText(cameraId);  
				mCameraGroup.addView(tempButton, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);  
				mCameraList.add(tempButton);
			}
			mCameraGroup.check(mCameraList.get(curIndex).getId());
			//switchCamera(curIndex);
		}

		mCameraGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() { 

			@Override 
			public void onCheckedChanged(RadioGroup group, int checkedId) { 
				Log.v("sjfcamera","checkedId:"+checkedId);
				RadioButton tempButton = (RadioButton)findViewById(checkedId); 
				try {
					int index = Integer.parseInt(tempButton.getText().toString());
					switchCamera(index);
				} catch (Exception e) {
					// TODO: handle exception
					switchCamera(checkedId-1);
				}
			}
		});


	}
	private void switchCamera(int index)
	{
		if(index == curIndex && camera != null)return;
		if(index < 0 || index >= cameraIds.length) return;

		if(camera != null)
		{
			camera.stopPreview();//停掉原来摄像头的预览
			camera.release();//释放资源
			camera = null;//取消原来摄像头
		}
		camera = Camera.open(Integer.parseInt(cameraIds[index]));//打开当前选中的摄像头
		try {
			camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.v("sjf","start switch camera IOException:"+e.getMessage());
		}
		camera.startPreview();//开始预览
		curIndex = index;
	}

	//响应点击事件
	OnClickListener listener = new OnClickListener() {
		@SuppressWarnings("deprecation")
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.camera_go_gallery:
				if(AppUtils.isActivityExist(CameraTestActivity.this, "com.android.gallery3d", "com.android.gallery3d.app.GalleryActivity"))
				{
					Intent intent = new Intent(Intent.ACTION_MAIN)
		            .addCategory(Intent.CATEGORY_LAUNCHER)
		            .setClassName("com.android.gallery3d", "com.android.gallery3d.app.GalleryActivity")
		            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					CameraTestActivity.this.startActivity(intent);
				}
				break;
			case R.id.camera_back:
				//返回
				CameraTestActivity.this.finish();
				break; 

				//			case R.id.camera_position:
				//				int curIndex = 1-index;
				//
				//				camera.stopPreview();//停掉原来摄像头的预览
				//				camera.release();//释放资源
				//				camera = null;//取消原来摄像头
				//				camera = Camera.open(curIndex);//打开当前选中的摄像头
				//				try {
				//					camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
				//				} catch (IOException e) {
				//					// TODO Auto-generated catch block
				//					e.printStackTrace();
				//					Log.v("sjf","start switch camera IOException:"+e.getMessage());
				//				}
				//				camera.startPreview();//开始预览
				//				index = curIndex;
				//				break;

			case R.id.camera_shutter:
				//快门
				showShutterDialog();
				camera.autoFocus(new AutoFocusCallback() {//自动对焦
					@Override
					public void onAutoFocus(boolean success, Camera camera) {
						// TODO Auto-generated method stub
						if(success) {
							//设置参数，并拍照
							Parameters params = camera.getParameters();
							params.setPictureFormat(PixelFormat.JPEG);//图片格式
							camera.setParameters(params);//将参数设置到我的camera
							camera.takePicture(null, null, jpeg);//将拍摄到的照片给自定义的对象
						}else{
							hideShutterDialog();
							Toast.makeText(CameraTestActivity.this, "拍摄失败", Toast.LENGTH_SHORT).show();
						}
					}
				});
				break;
			}
		}
	};

	/*surfaceHolder他是系统提供的一个用来设置surfaceView的一个对象，而它通过surfaceView.getHolder()这个方法来获得。
     Camera提供一个setPreviewDisplay(SurfaceHolder)的方法来连接*/

	//SurfaceHolder.Callback,这是个holder用来显示surfaceView 数据的接口,他必须实现以下3个方法
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		//当surfaceview创建时开启相机
		if(camera == null) {
			camera = Camera.open(curIndex); 
			try {
				camera.setPreviewDisplay(holder);//通过surfaceview显示取景画面
				camera.startPreview();//开始预览
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		//当surfaceview关闭时，关闭预览并释放资源
		camera.stopPreview();
		camera.release();
		camera = null;
		holder = null;
		surface = null;
	}

	//创建jpeg图片回调数据对象
	PictureCallback jpeg = new PictureCallback() {
		@SuppressLint("SimpleDateFormat")
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			try {
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
				//自定义文件保存路径  以拍摄时间区分命名
				String filaname = "Camera" +cameraIds[curIndex]+"-"+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+".jpg";

				File dirFile = new File(filepath);
				if(!dirFile.exists())dirFile.mkdirs();
				
				File file = new File(filepath+File.separator+filaname);
				Log.v("sjfcamera","save pic:"+file.getAbsolutePath());
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
				bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将图片压缩的流里面
				bos.flush();// 刷新此缓冲区的输出流
				bos.close();// 关闭此输出流并释放与此流有关的所有系统资源
				camera.stopPreview();//关闭预览 处理数据
				camera.startPreview();//数据处理完后继续开始预览
				bitmap.recycle();//回收bitmap空间
				hideShutterDialog();
				//scanDirAsync(CameraTestActivity.this, filepath);

				MediaScannerConnection.scanFile(CameraTestActivity.this, new String[] {file.getAbsolutePath()}, null,null); 
				Toast.makeText(CameraTestActivity.this, "拍摄成功:"+filaname, Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				hideShutterDialog();
				Toast.makeText(CameraTestActivity.this, "拍摄失败", Toast.LENGTH_SHORT).show();
			}
		}
	};


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		hideShutterDialog();
	}

	ProgressDialog mShutterDialog;
	private void showShutterDialog()
	{
		if(mShutterDialog != null)
		{
			mShutterDialog.dismiss();
			mShutterDialog = null;
		}

		mShutterDialog = new ProgressDialog(this);  
		mShutterDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);  
		mShutterDialog.setCancelable(true);  
		mShutterDialog.setCanceledOnTouchOutside(false);
		mShutterDialog.setTitle("提示"); 
		mShutterDialog.setMessage("拍摄中");
		mShutterDialog.show();
	}

	private void hideShutterDialog()
	{
		if(mShutterDialog != null)
		{
			mShutterDialog.dismiss();
			mShutterDialog = null; 
		}
	}

	public static final String ACTION_MEDIA_SCANNER_SCAN_DIR = "android.intent.action.MEDIA_SCANNER_SCAN_DIR";  
	public void scanDirAsync(Context ctx, String dir) {  
		//           Intent scanIntent = new Intent(ACTION_MEDIA_SCANNER_SCAN_DIR);  
		//           scanIntent.setData(Uri.fromFile(new File(dir)));  
		//           ctx.sendBroadcast(scanIntent);  
		Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);  
		intent.setData(Uri.parse("file://" + dir));  
		sendBroadcast(intent);
	} 

	public void scanFileAsync(Context ctx, File file) {  
		Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);  
		intent.setData(Uri.fromFile(file));  
		ctx.sendBroadcast(intent);  
	} 


}