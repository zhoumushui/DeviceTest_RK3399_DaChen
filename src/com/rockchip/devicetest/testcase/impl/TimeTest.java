/*******************************************************************
 * Company:     Fuzhou Rockchip Electronics Co., Ltd
 * Description:   
 * @author:     fxw@rock-chips.com
 * Create at:   2014年5月12日 下午5:41:32  
 * 
 * Modification History:  
 * Date         Author      Version     Description  
 * ------------------------------------------------------------------  
 * 2014年5月12日      fxw         1.0         create
 *******************************************************************/

package com.rockchip.devicetest.testcase.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.lang.Integer; 
import java.lang.Thread; 

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.rockchip.devicetest.ConfigFinder;
import com.rockchip.devicetest.R;
import com.rockchip.devicetest.constants.ParamConstants;
import com.rockchip.devicetest.constants.ResourceConstants;
import com.rockchip.devicetest.enumerate.AgingType;
import com.rockchip.devicetest.model.TestCaseInfo;
import com.rockchip.devicetest.testcase.BaseTestCase;
import com.rockchip.devicetest.utils.FileUtils;


public class TimeTest extends BaseTestCase {

    private static final String TimeFilePath = "/sys/class/rtc/rtc0/time";
    private File TimeFile = null;
    

	public TimeTest(Context context, Handler handler, TestCaseInfo testcase) {
		super(context, handler, testcase);
	}
	@Override
	public boolean onTesting() {
		// TODO Auto-generated method stub
		
		TimeFile = new File(TimeFilePath);
		if(!TimeFile.exists())
		{
		    onTestFail("RTC time init fail");
		}
        
        String read1;
        String read2;
        
        read1 = FileUtils.readFromFile(TimeFile);
        
        
        try
        {
            Thread.currentThread().sleep(2000);//毫秒 
        }
        catch(Exception e){} 
        
        read2 = FileUtils.readFromFile(TimeFile);
        
        if(read1.equals(read2))
        {
            onTestFail("RTC time equal fail");
        }
        else
        {
            onTestSuccess(read1+"  "+read2);
        }
        
        
			

		return super.onTesting();
	}

	
}
