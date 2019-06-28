package com.rockchip.devicetest.smb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;

import android.util.Log;

public class FileDownloadThread extends Thread{
	private static final String TAG = "FileDownloadThread";
	/**缓冲区 */
	private static final int BUFF_SIZE = 1024;
	/**需要下载的smbFile*/
	private SmbFile smbFile;
	/**缓存的FIle*/
	private File localFile;
	/**完成*/
	private boolean finished = false;
	/**已经下载多少*/
	private int downloadSize = 0;
	
	private boolean mFlag = true;
	
	/***
	 * @param smbFile   局域网smb文件
	 * @param localFile  下载的文件
	 * @param startPosition 开始位置
	 * @param endPosition   结束位置
	 */
	public FileDownloadThread(SmbFile smbFile, File localFile) {
		this.smbFile = smbFile;
		this.localFile = localFile;
		Log.e(TAG, toString());
	}
	
	@Override
	public void run() {
		InputStream in = null;  
        OutputStream out = null;  
        
        try {  
            in = new BufferedInputStream(new SmbFileInputStream(smbFile));  
            out = new BufferedOutputStream(new FileOutputStream(localFile));  
            byte []buffer = new byte[1024];  
            int length;
			while((length =in.read(buffer)) != -1 && mFlag){  
                out.write(buffer);  
                buffer = new byte[1024]; 
                downloadSize +=  length;
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }finally{  
            try {  
            	this.finished = true; 
                out.close();  
                in.close();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
        }  
		super.run();
	}
	
	/**
	 * 是否完成当前下载
	 * @return
	 */
	public boolean isFinished() {
		return finished;
	}
	/**
	 * 已经下载多少
	 * @return
	 */
	public int getDownloadSize() {
		return downloadSize;
	}
	@Override
	public String toString() {
		return "FileDownloadThread [SmbFile=" + smbFile.getPath() + ", localFile=" + localFile.getAbsolutePath()
				 + ", finished="+ finished + ", downloadSize=" + downloadSize + "]";
	}
	
	public void stopDownload() {
		mFlag = false;
	}
}
