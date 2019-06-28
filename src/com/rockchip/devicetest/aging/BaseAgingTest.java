/*******************************************************************
* Company:     Fuzhou Rockchip Electronics Co., Ltd
* Description:   
* @author:     fxw@rock-chips.com
* Create at:   2014年5月16日 上午11:43:40  
* 
* Modification History:  
* Date         Author      Version     Description  
* ------------------------------------------------------------------  
* 2014年5月16日      fxw         1.0         create
*******************************************************************/   

package com.rockchip.devicetest.aging;

import android.app.Activity;
import android.content.Context;
import android.view.View;

public abstract class BaseAgingTest implements IAgingTest {
	
	protected AgingConfig mAgingConfig;
	protected AgingCallback mAgingCallback;
	
	public BaseAgingTest(AgingConfig agingConfig, AgingCallback agingCallback){
		mAgingConfig = agingConfig;
		mAgingCallback = agingCallback;
	}

	@Override
	public void onCreate(Context context, View view) {

	}

	public void onStart() {

	}

	public void onStop() {

	}

	public void onDestroy() {

	}

}
