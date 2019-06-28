package com.rockchip.devicetest.smb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jcifs.Config;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

import com.rockchip.devicetest.utils.FileUtils;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SmbDownLoadThread extends Thread{
	private static final String TAG = "SmbDownLoadThread";
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
	private boolean mFlag = true;
	/**
	 * 下载的构造函数  
	 * @param url  请求下载的URL
	 * @param smbPath  网上邻居的路径
	 * @param fileName  保存的名字
	 */
	public SmbDownLoadThread(Handler handler,String smbPath,String savePath,String fileName)
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

//	        jcifs.Config.setProperty("jcifs.resolveOrder", "DNS");
//	        jcifs.Config.setProperty("jcifs.smb.client.responseTimeout ", "10000");
//	        jcifs.Config.setProperty("jcifs.smb.client.dfs.disabled", "true");
			SmbFile smbFile = null;  
			boolean isExists = false;
			 
			  for (int retry = 0; retry < 3; retry++) { 
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
			  Log.v("sjfsmb", "jcifs.smb.client.responseTimeout:"+ jcifs.Config.getProperty("jcifs.smb.client.responseTimeout"));
		// Config.setProperty( "jcifs.resolveOrder", "DNS,LMHOSTS,WINS,BCAST");
//	     jcifs.Config.setProperty("jcifs.resolveOrder", "DNS");
//	      jcifs.Config.setProperty("jcifs.smb.client.dfs.disabled", "true");
//		SmbFile smbFile = new SmbFile(smbPath);
//		boolean isExists = smbFile.exists();
			if(isExists)
			{
				fileSize = smbFile.getContentLength();
				sendMsg(FileUtils.startDownloadMeg);
				Log.e(TAG, "smbFile filesize="+fileSize);
				File localFile = new File(savePath+fileName);
				InputStream in = null;  
		        OutputStream out = null;  
		        
		        try {  
		            in = new BufferedInputStream(new SmbFileInputStream(smbFile));  
		            out = new BufferedOutputStream(new FileOutputStream(localFile));  
		            byte []buffer = new byte[5096];  
		            int length;
					startTime = System.currentTimeMillis();
					boolean finished = false;
					while((length =in.read(buffer)) != -1 && mFlag){  
		                out.write(buffer);  
		                buffer = new byte[1024]; 
		                downloadSize +=  length;
		                
		                
		                
		          
		            }  
					usedTime =  (int) ((System.currentTimeMillis()-startTime)/1000);
					completed = true;
		        } catch (Exception e) {  
		            e.printStackTrace();  
		        }finally{  
		            try {  
		                out.close();  
		                in.close();  
		                localFile.delete();
		            } catch (IOException e) {  
		                e.printStackTrace();  
		            }  
		        }  
		        
		        
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
	public void updateDownloadDatial()
	{
		int d_size = downloadSize/1024;
		int f_size = fileSize/1024;

		downloadPercent = (int) (d_size*100/f_size);
		
		if(!isCompleted())
		{
			usedTime = (int) ((System.currentTimeMillis()-startTime)/1000);
		}
		

		if(usedTime==0){
			usedTime = 1;  
		}
		downloadSpeed = (downloadSize/usedTime);
		
		//Log.v("sjfsmb","curTime = "+curTime+" downloadSize = "+downloadSize+"  fileSize="+fileSize +" usedTime "+usedTime);
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
	private void sendMsg(int what)
	{
		Message msg = new Message();
		msg.what = what;
		handler.sendMessage(msg);
	}
	public void stopUpdateUI() {
		mFlag = false;
	}
	public boolean isCompleted(){
		return this.completed;
	}

}
