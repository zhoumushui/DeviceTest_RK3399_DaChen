package com.rockchip.devicetest.smb;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;

import com.rockchip.devicetest.utils.FileUtils;

import jcifs.Config;
import jcifs.smb.SmbFile;
import jcifs.util.transport.TransportException;
import android.os.Handler;
import android.os.Message;
import android.util.Log;  
import android.os.SystemProperties;

public class updateUIThread extends Thread{
	private static final String TAG = "SingelThreadDownload";
	/*** 文件大小 */
	private int fileSize;
	/** * 已经下载多少 */
	private int downloadSize;
	/**SMB共享文件路径,线程编号，文件名称*/
	private String smbPath,ThreadNo,fileName;
	/***保存的路径*/
	private String savePath;
	/**下载的百分比*/
	private int downloadPercent = 0;
	/**下载的 平均速度*/
	private int downloadSpeed = 0;
	/**下载用的时间*/
	private int usedTime = 0;
	/**开始时间*/
	private long startTime;
	/**当前时间*/
	private long curTime;
	/**是否已经下载完成*/
	private boolean completed = false;
	private Handler handler ;
	private FileDownloadThread fdt = null;
	private boolean mFlag = true;
	/**
	 * 下载的构造函数  
	 * @param url  请求下载的URL
	 * @param smbPath  网上邻居的路径
	 * @param fileName  保存的名字
	 */
	public updateUIThread(Handler handler,String smbPath,String savePath,String fileName)
	{
		this.handler = handler;
		this.smbPath = smbPath;
		this.savePath = savePath;
		this.fileName = fileName;
		Log.e(TAG, toString());
	}
	@Override
	public void run() {
		try {
			SmbFile smbFile = null;  
			boolean isExists = false;
			 
			  for (int retry = 0; retry < 5; retry++) { 
			      try { 
			    	  Log.v("sjfsmb", "start get smbFile "+retry);
			    	  smbFile = new SmbFile(smbPath); 
			    	  isExists = smbFile.exists();
			    	  Log.v("sjfsmb", "smbFile:"+smbFile.getPath());
			          break; 
			      } catch (Exception te) { 
			    	  Log.v("sjfsmb", "smbFile get error"+retry);
			      } 
			  } 
			if(isExists)
			{
				fileSize = smbFile.getContentLength();
				sendMsg(FileUtils.startDownloadMeg);
				Log.e(TAG, "smbFile filesize="+fileSize);
				File file = new File(savePath+fileName);
				fdt = new FileDownloadThread(smbFile, file);
				fdt.setName("downloadThread");
				fdt.start();
				startTime = System.currentTimeMillis();
				boolean finished = false;
				while(!finished && mFlag)
				{
					downloadSize = 0;
					downloadSize+= fdt.getDownloadSize();

					int d_size = downloadSize/1024;
					int f_size = fileSize/1024;
					
					downloadPercent = (int) (d_size*100/f_size);
					curTime = System.currentTimeMillis();
					//Log.v("sjf","tmp="+tmp+"  ,fileSize="+fileSize+"  ,downloadPercent="+downloadPercent);
					System.out.println("curTime = "+curTime+" downloadSize = "+downloadSize+"  fileSize="+fileSize +" usedTime "+(int) ((curTime-startTime)/1000));
					usedTime = (int) ((curTime-startTime)/1000);

					if(usedTime==0){
						usedTime = 1;  
					}
					downloadSpeed = (downloadSize/usedTime);
					finished = fdt.isFinished();
					sleep(1000);/*1秒钟刷新一次界面*/
					sendMsg(FileUtils.updateDownloadMeg);
				}
				
				if(file.exists()) {
					file.delete();
				}		
				
				completed = true;
				Log.e(TAG, "ok");
				sendMsg(FileUtils.endDownloadMeg);
			}else{
				sendMsg(FileUtils.noFoundFile);
			}
			smbFile = null;
			
		} catch (Exception e) {
			sendMsg(FileUtils.noFoundFile);
			Log.e(TAG, "multi file error Exception :"+e.getMessage());
			e.printStackTrace();
		}
		super.run();
	}
	/**
	 * 得到文件的大小
	 * @return
	 */
	public int getFileSize()
	{
		return this.fileSize;
	}
	/**
	 * 得到已经下载的数量
	 * @return
	 */
	public int getDownloadSize()
	{
		return this.downloadSize;
	}
	/**
	 * 获取下载百分比
	 * @return
	 */
	public int getDownloadPercent(){
		return this.downloadPercent;
	}

	/**
	 * 获取下载速度
	 * @return
	 */
	public int getDownloadSpeed(){
		return this.downloadSpeed;
	}
	
	public String getDownloadSpeedStr()
	{
		return FileUtils.FormatFileSize(downloadSpeed)+"/s";
	}
	/**
	 * 分块下载完成的标志
	 * @return
	 */
	public boolean isCompleted(){
		return this.completed;
	}
	@Override
	public String toString() {
		return "MultiThreadDownload [threadNum=" + ", fileSize="
				+ fileSize + ", smbPath=" + smbPath + ", ThreadNo=" + ThreadNo
				+ ", fileName=" + fileName + ", savePath=" + savePath + "]";
	}


	private void sendMsg(int what)
	{
		Message msg = new Message();
		msg.what = what;
		handler.sendMessage(msg);
	}

	public void stopUpdateUI() {
		mFlag = false;
		if(fdt != null) {
			fdt.stopDownload();
		}
	}


}
