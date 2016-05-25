/**   
 * Copyright © 2015 公司名. All rights reserved.
 * 
 * @Title: DeviceListener.java 
 * @Prject: APICloudSDK
 * @Package: com.apicloud.controller 
 * @Description: TODO
 * @author: user20   
 * @date: 2015年8月14日 下午1:45:06 
 * @version: V1.0   
 */
package com.apicloud.controller;

import android.os.Handler;
import android.util.Log;

import com.apicloud.activity.BaseActivity;
import com.apicloud.module.BankCrad;
import com.newland.mtype.event.DeviceEventListener;
import com.newland.mtype.module.common.keyboard.KeyBoardReadingEvent;

/** 
 * @ClassName: DeviceListener 
 * @Description: TODO
 * @author: user20
 * @date: 2015年8月14日 下午1:45:06  
 */
public class DeviceListener implements DeviceEventListener<KeyBoardReadingEvent<String>> {
    
	 public boolean pwdInputFinish=false;
	 
     public DeviceListener() {
		// TODO Auto-generated constructor stub
	 }
	/* (non Javadoc) 
	 * @Title: getUIHandler
	 * @Description: TODO
	 * @return
	 * @deprecated 
	 * @see com.newland.mtype.event.DeviceEventListener#getUIHandler() 
	 */
	@Override
	public Handler getUIHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	
	/* (non Javadoc) 
	 * @Title: onEvent
	 * @Description: TODO
	 * @param arg0
	 * @param arg1 
	 * @see com.newland.mtype.event.DeviceEventListener#onEvent(com.newland.mtype.event.DeviceEvent, android.os.Handler) 
	 */
	@Override
	public void onEvent(KeyBoardReadingEvent<String> event, Handler arg1) {
		String password=event.getRslt()+"";
		BaseActivity.BlueToothPsd=password;
		
		pwdInputFinish=true;
		
	}

}
