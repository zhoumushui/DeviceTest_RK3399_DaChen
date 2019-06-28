/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月12日 下午6:17:42  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月12日      fxw         1.0         create
 *******************************************************************/

package com.rockchip.devicetest.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;

import com.rockchip.devicetest.ConfigFinder;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class FileUtils {

	private static final String TAG = "DeviceTest-FileUitls";
	/**
	 * 开始消息提示常量
	 * */
	public static final int startDownloadMeg = 1;

	/**
	 * 更新消息提示常量
	 * */
	public static final int updateDownloadMeg = 2;

	/**
	 * 完成消息提示常量
	 * */
	public static final int endDownloadMeg = 3;
	/**
	 * smb文件不存在提示
	 * */
	public static final int noFoundFile = 4;
	/**
	 * 检验SDcard状态  
	 * @return boolean
	 */
	public static boolean checkSDCard()
	{
		if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
		{
			return true;
		}else{
			return false;
		}
	}
	/**
	 * 保存文件文件到目录
	 * @param context
	 * @return  文件保存的目录
	 */
	public static String setMkdir(Context context)
	{
		String filePath;
		if(checkSDCard())
		{
			filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		}else{
			filePath = context.getCacheDir().getAbsolutePath();
		}
		File file = new File(filePath);
		if(!file.exists())
		{
			boolean b = file.mkdirs();
			Log.e("file", "文件不存在  创建文件    "+b);
		}else{
			Log.e("file", "文件存在");
		}
		return filePath;
	}

	/** 
	 * 得到文件的名称
	 * @return
	 * @throws IOException
	 */
	public static  String getFileName(String url)
	{
		String name= null;
		try {
			name = url.substring(url.lastIndexOf("/")+1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	/**
	 * 
	 * @param size 文件大小
	 * @return
	 */
	public static String FormatFileSize(long size){  
		DecimalFormat formater = new DecimalFormat("####.00");  
		if(size<1024){  
			return size+"bytes";  
		}else if(size<1024*1024){  
			float kbsize = size/1024f;    
			return formater.format(kbsize)+"KB";  
		}else if(size<1024*1024*1024){  
			float mbsize = size/1024f/1024f;    
			return formater.format(mbsize)+"MB";  
		}else if(size<1024*1024*1024*1024){  
			float gbsize = size/1024f/1024f/1024f;    
			return formater.format(gbsize)+"GB";  
		}else{  
			return "size: error";  
		}  
	}

	/**
	 * 读取文件
	 * 
	 * @param file
	 * @return 字符串
	 */
	public static String readFromFile(File file) {
		if ((file != null) && file.exists()) {
			try {
				FileInputStream fin = new FileInputStream(file);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(fin));
				String value = reader.readLine();
				fin.close();
				return value;
			} catch (IOException e) {
				return null;
			}
		}
		return null;
	}

	/**
	 * 文件中写入字符串
	 * 
	 * @param file
	 * @param enabled
	 */
	public static boolean write2File(File file, String value) {
		if ((file == null) || (!file.exists()))
			return false;
		try {
			FileOutputStream fout = new FileOutputStream(file);
			PrintWriter pWriter = new PrintWriter(fout);
			pWriter.println(value);
			pWriter.flush();
			pWriter.close();
			fout.close();
			return true;
		} catch (IOException re) {
			return false;
		}
	}

	/**
	 * 将Asset下的文件复制到/data/data/.../files/目录下
	 * 
	 * @param context
	 * @param fileName
	 */
	public static boolean copyFromAsset(Context context, String fileName,
			boolean recreate) {
		byte[] buf = new byte[20480];
		try {
			File fileDir = context.getFilesDir();
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}
			String destFilePath = fileDir.getAbsolutePath() + File.separator
					+ fileName;
			File destFile = new File(destFilePath);
			if (!destFile.exists() || recreate) {
				destFile.createNewFile();
			} else {
				return true;
			}
			FileOutputStream os = new FileOutputStream(destFilePath);// 得到数据库文件的写入流
			InputStream is = context.getAssets().open(fileName);// 得到数据库文件的数据流
			int cnt = -1;
			while ((cnt = is.read(buf)) != -1) {
				os.write(buf, 0, cnt);
			}
			os.flush();
			is.close();
			os.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			buf = null;
		}
	}



	public static boolean copyFromAsset(Context context, String fileName,String toDirPath,
			boolean recreate) {
		byte[] buf = new byte[20480];
		try {
			File fileDir = new File(toDirPath);
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}
			String toFilePath = fileDir.getAbsolutePath() + File.separator
					+ fileName;
			File toFile = new File(toFilePath);
			if (!toFile.exists() || recreate) {
				toFile.createNewFile();
			} else {
				return true;
			}
			FileOutputStream os = new FileOutputStream(toFilePath);// 得到数据库文件的写入流
			InputStream is = context.getAssets().open(fileName);// 得到数据库文件的数据流
			int cnt = -1;
			while ((cnt = is.read(buf)) != -1) {
				os.write(buf, 0, cnt);
			}
			os.flush();
			is.close();
			os.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			buf = null;
			String toFilePath = toDirPath + File.separator
					+ fileName;
			if(new File(toFilePath).exists())
			{
				SystemBinUtils.chmod(toFilePath);
			}
		}
	}

	public static boolean copyFile(String fromPath, String toPath) {
		byte[] buf = new byte[20480];
		try {
			FileOutputStream os = new FileOutputStream(toPath);
			InputStream is =  new FileInputStream(fromPath);
			int cnt = -1;
			while ((cnt = is.read(buf)) != -1) {
				os.write(buf, 0, cnt);
			}
			os.flush();
			is.close();
			os.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			buf = null;
			if(new File(toPath).exists())
			{
				SystemBinUtils.chmod(toPath);
			}
		}
		/*try {
			int byteread = 0;
			File oldfile = new File(fromPath);

			File newfile = new File(toPath);
			String cmd = "chmod 644 " + newfile.getAbsolutePath();
			Runtime.getRuntime().exec(cmd);

			if (oldfile.exists()) {
				InputStream inStream = new FileInputStream(fromPath);
				FileOutputStream fout = new FileOutputStream(toPath);// ???异常
				byte[] buffer = new byte[1024 * 5];
				while ((byteread = inStream.read(buffer)) != -1) {
					fout.write(buffer, 0, byteread);
				}
				fout.flush();
				inStream.close();
				fout.close();
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}*/
	}

	/**
	 * 修改文件权限
	 */
	public static void chmodDataFile(Context context, String fileName) {
		File fileDir = context.getFilesDir();
		String destFilePath = fileDir.getAbsolutePath() + File.separator
				+ fileName;
		SystemBinUtils.chmod(destFilePath);
	}

	public static String getDataFileFullPath(Context context, String fileName) {
		File fileDir = context.getFilesDir();
		String destFilePath = fileDir.getAbsolutePath() + File.separator
				+ fileName;
		return destFilePath;
	}

	/**
	 * 读取文件内容
	 * 
	 * @param file
	 * @return
	 */
	public static byte[] readFileContent(File file) {
		InputStream fin = null;
		try {
			fin = new FileInputStream(file);
			byte[] readBuffer = new byte[20480];
			int readLen = 0;
			ByteArrayOutputStream contentBuf = new ByteArrayOutputStream();
			while ((readLen = fin.read(readBuffer)) > 0) {
				contentBuf.write(readBuffer, 0, readLen);
			}
			return contentBuf.toByteArray();
		} catch (Exception e) {
		} finally {
			if (fin != null) {
				try {
					fin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}


	/*寻找如***_Factory_Test.bin之类的文件*/
	public static String findFileByPartialName(String fileName, Context mContext) {	
		/*兼容某些/data/data/com.rockchip.devicetest.** path */
		if (fileName.startsWith("/") || fileName.startsWith("\\")) {
			return fileName;
		}

		StorageList mStorageList = new StorageList(mContext);
		String paths [] = mStorageList.getVolumnPaths();
		String externalStorageFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		String absolutePath = null;
		for(String path : paths) {
			Log.d(TAG, "find path:"+path);				
			if(!Environment.MEDIA_MOUNTED.equals(mStorageList.getVolumeState(path)))
				continue;
			if(externalStorageFilePath.equalsIgnoreCase(path))
				continue;
			//Usb path need getSubUsbPath!!!
			if(path.indexOf("usb_storage") != -1) {
				path = ConfigFinder.getSubUsbPath(path);
			}
			absolutePath = findFileByPartialName(fileName, path);
			if(absolutePath != null && absolutePath != "") {
				return absolutePath;
			}			
		}
		


		//Flash存储
		absolutePath = findFileByPartialName(fileName, externalStorageFilePath);
		if(absolutePath != null && absolutePath != "") {
		       return absolutePath;
		}	

		//system/media/devicetest
		absolutePath = findFileByPartialName(fileName, "/system/media/devicetest");
		
		return absolutePath;
	}

	public static String findFileByPartialName(String fileName, String filePath) {
		File file = new File(filePath);
		if (!file.exists() || !file.isDirectory()) {
			return "";
		}
		File[] listFiles = file.listFiles();
		if (listFiles == null)
			return "";

		for (File child : listFiles) {
			String absolutePath = child.getAbsolutePath();
			Log.d(TAG, "find file:"+absolutePath);		
			if(absolutePath != null && absolutePath.indexOf(fileName) != -1) {
				return absolutePath;
			}
		}
		return "";
	}
	
	/**
	 * 写入测试数据
	 * 
	 * @param filename
	 * @return
	 */
	public static boolean doWriteFile(String filename,String msg) {
		try {
			OutputStreamWriter osw = new OutputStreamWriter(
					new FileOutputStream(filename));
			osw.write(msg, 0, msg.length());
			osw.flush();
			osw.close();
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * 读取测试数据
	 * 
	 * @param filename
	 * @return
	 */
	public static String doReadFile(String filename) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename)));
			String data = null;
			StringBuilder temp = new StringBuilder();
			while ((data = br.readLine()) != null) {
				temp.append(data);
			}
			br.close();
			return temp.toString();
		} catch (Exception e) {
			return null;
		}
	}
	
}
