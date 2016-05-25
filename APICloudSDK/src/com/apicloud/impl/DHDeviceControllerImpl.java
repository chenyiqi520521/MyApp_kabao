/**   
 * Copyright © 2015 公司名. All rights reserved.
 * 
 * @Title: DHDeviceControllerImpl.java 
 * @Prject: APICloudSDK
 * @Package: com.apicloud.impl 
 * @Description: TODO
 * @author: user20   
 * @date: 2015年8月26日 下午1:39:28 
 * @version: V1.0   
 */
package com.apicloud.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;

import android.content.Context;

import com.apicloud.controller.DHDeviceController;
import com.dspread.xpos.QPOSService;
import com.dspread.xpos.QPOSService.CommunicationMode;
import com.dspread.xpos.QPOSService.EmvOption;
import com.dspread.xpos.QPOSService.QPOSServiceListener;
import com.dspread.xpos.QPOSService.TransactionType;

/** 
 * @ClassName: DHDeviceControllerImpl 
 * @Description: TODO
 * @author: user20
 * @date: 2015年8月26日 下午1:39:28  
 */
public class DHDeviceControllerImpl implements DHDeviceController{
	
	private  DHDeviceControllerImpl dhDC=null;
	private QPOSService pos;
	
	public DHDeviceControllerImpl(CommunicationMode mode){
		   pos=QPOSService.getInstance(mode);
	}
	
	public   DHDeviceControllerImpl  getInstance(CommunicationMode mode){
		
	        dhDC=new DHDeviceControllerImpl(mode);
		
		    return dhDC;
	}
	

	/* (non Javadoc) 
	 * @Title: destory
	 * @Description: 销毁设备 
	 * @see com.apicloud.controller.DHDeviceController#destory() 
	 */
	@Override
	public void destory() {
		if (pos != null) {
			pos.onDestroy();
		}
		
		
	}

	/* (non Javadoc) 
	 * @Title: init
	 * @Description: TODO
	 * @param context
	 * @param mode
	 * @param listener 
	 * @see com.apicloud.controller.DHDeviceController#init(android.content.Context, com.dspread.xpos.QPOSService.CommunicationMode, com.dspread.xpos.QPOSService.QPOSServiceListener) 
	 */
	@Override
	public void init(Context context, CommunicationMode mode, QPOSServiceListener listener) {
		
		/*if(pos==null){
			pos=QPOSService.getInstance(mode);
		}*/
		pos.setContext(context);
		pos.initListener(listener);
	}

	/* (non Javadoc) 
	 * @Title: connect
	 * @Description: TODO
	 * @param blueToothAddress 
	 * @see com.apicloud.controller.DHDeviceController#connect(java.lang.String) 
	 */
	@Override
	public void connect(String blueToothAddress) {
		pos.connectBluetoothDevice(true, 25, blueToothAddress);
		
	}

	/* (non Javadoc) 
	 * @Title: checkConnected
	 * @Description: TODO 
	 * @see com.apicloud.controller.DHDeviceController#checkConnected() 
	 */
	@Override
	public void checkConnected() {
		pos.getQposId();
		
	}

	/* (non Javadoc) 
	 * @Title: doTrade
	 * @Description: TODO
	 * @param timeout 
	 * @see com.apicloud.controller.DHDeviceController#doTrade(int) 
	 */
	@Override
	public void doTrade(int timeout) {
		if(pos!=null){
			pos.doTrade(timeout);
		}
		
	}

	/* (non Javadoc) 
	 * @Title: setAmount
	 * @Description: TODO
	 * @param amount
	 * @param cashbackAmount
	 * @param defStr
	 * @param transactionType 
	 * @see com.apicloud.controller.DHDeviceController#setAmount(java.lang.String, java.lang.String, java.lang.String, com.dspread.xpos.QPOSService.TransactionType) 
	 */
	@Override
	public void setAmount(String amount, String cashbackAmount, String defStr, TransactionType transactionType) {
		if(pos!=null){
			pos.setAmount(amount, cashbackAmount, defStr, transactionType);
		}
		
	}

	/* (non Javadoc) 
	 * @Title: doEmv
	 * @Description: TODO
	 * @param option 
	 * @see com.apicloud.controller.DHDeviceController#doEmv(com.dspread.xpos.QPOSService.EmvOption) 
	 */
	@Override
	public void doEmv(EmvOption option) {
	    if(pos!=null){
	    	pos.doEmvApp(option);
	    }
		
	}

	/* (non Javadoc) 
	 * @Title: setTime
	 * @Description: TODO
	 * @param formatetime 
	 * @see com.apicloud.controller.DHDeviceController#setTime(java.lang.String) 
	 */
	@Override
	public void setTime(String formatetime) {
		if(pos!=null){
			pos.sendTime(formatetime);
		}
		
	}

	/* (non Javadoc) 
	 * @Title: isServerConnected
	 * @Description: TODO
	 * @param set 
	 * @see com.apicloud.controller.DHDeviceController#isServerConnected(boolean) 
	 */
	@Override
	public void isServerConnected(boolean set) {
		if(pos!=null){
			pos.isServerConnected(set);
		}
		
	}

	/* (non Javadoc) 
	 * @Title: getICInfo
	 * @Description: TODO
	 * @param str55
	 * @return 
	 * @see com.apicloud.controller.DHDeviceController#getICInfo(java.lang.String) 
	 */
	@Override
	public Hashtable<String, String> getICInfo(String str55) {
	    if(pos!=null){
	    	return pos.anlysEmvIccData(str55);
	    }else{
	    	return null;
	    }
		
	}

	/* (non Javadoc) 
	 * @Title: disConnected
	 * @Description: TODO 
	 * @see com.apicloud.controller.DHDeviceController#disConnected() 
	 */
	@Override
	public void disConnected() {
		if(pos!=null){
			pos.resetQPOS();
			pos.disconnectBT();
			
		}
		
	}

	/* (non Javadoc) 
	 * @Title: UpdateWorkKey
	 * @Description: TODO 
	 * @see com.apicloud.controller.DHDeviceController#UpdateWorkKey() 
	 */
	@Override
	
	public void UpdateWorkKey() {
		/*pos.udpateWorkKey("89EEF94D28AA2DC189EEF94D28AA2DC1", "82E13665B4624DF5", 
				"ADC67D8473BF2F06ADC67D8473BF2F06", "8CA64DE9C1B123A7",
				"40BAEF32B505F86F40BAEF32B505F86F", " 00962B60AA556E65");*/
	/*	pos.udpateWorkKey("89EEF94D28AA2DC189EEF94D28AA2DC1", "82E13665B4624DF5", 
				"ADC67D8473BF2F06ADC67D8473BF2F06", "8CA64DE9C1B123A7",
				"40BAEF32B505F86F40BAEF32B505F86F", " 00962B60AA556E65",0,5);*/
		pos.udpateWorkKey(
				"89EEF94D28AA2DC189EEF94D28AA2DC1",	"82E13665B4624DF5",//PIN KEY
				"ADC67D8473BF2F06ADC67D8473BF2F06","8CA64DE9C1B123A7",  //TRACK KEY
				"40BAEF32B505F86F40BAEF32B505F86F",	"00962B60AA556E65", //MAC KEY
				0,5);
		
	}

	@Override
	public String getCardNo() {
		String cardNo="";
		String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
        pos.getIccCardNo(terminalTime);
		return cardNo;
	}

	/* (non Javadoc) 
	 * @Title: isConnected
	 * @Description: TODO
	 * @return 
	 * @see com.apicloud.controller.DHDeviceController#isConnected() 
	 */
	

	
	

	

}
