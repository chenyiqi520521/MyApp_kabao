/**   
 * Copyright © 2015 公司名. All rights reserved.
 * 
 * @Title: DHDeviceController.java 
 * @Prject: APICloudSDK
 * @Package: com.apicloud.controller 
 * @Description: TODO
 * @author: user20   
 * @date: 2015年8月26日 下午1:37:18 
 * @version: V1.0   
 */
package com.apicloud.controller;


import java.util.Hashtable;

import android.content.Context;

import com.apicloud.impl.DHListener;
import com.dspread.xpos.QPOSService.CommunicationMode;
import com.dspread.xpos.QPOSService.EmvOption;
import com.dspread.xpos.QPOSService.QPOSServiceListener;
import com.dspread.xpos.QPOSService.TransactionType;

/** 
 * @ClassName: DHDeviceController 
 * @Description: TODO
 * @author: user20
 * @date: 2015年8月26日 下午1:37:18  
 */

/**
 * 鼎合设备管理
 * @ClassName: DHDeviceController 
 * @Description: TODO
 * @author: user20
 * @date: 2015年8月26日 下午1:38:32
 */
public interface DHDeviceController {
	public void init(Context context,CommunicationMode mode,QPOSServiceListener listener);
	public void connect(String blueToothAddress);
	public void destory();
    public void checkConnected();
    public void doTrade(int timeout);
    public void setAmount(String amount,String cashbackAmount,String defStr,TransactionType transactionType);
    public void doEmv(EmvOption option);
    public void setTime(String formatetime);
    public void isServerConnected(boolean set);
    public Hashtable<String, String> getICInfo(String str55);
    public void disConnected();
    public void UpdateWorkKey();
    
    public String getCardNo();
	/** 
	 * @Title: init 
	 * @Description: TODO
	 * @param listener
	 * @return: void
	 */
	

}
