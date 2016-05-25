/**   
 * Copyright © 2015 公司名. All rights reserved.
 * 
 * @Title: DHListener.java 
 * @Prject: APICloudSDK
 * @Package: com.apicloud.impl 
 * @Description: TODO
 * @author: user20   
 * @date: 2015年8月26日 下午1:57:07 
 * @version: V1.0   
 */
package com.apicloud.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.dspread.xpos.QPOSService.Display;
import com.dspread.xpos.QPOSService.DoTradeResult;
import com.dspread.xpos.QPOSService.Error;
import com.dspread.xpos.QPOSService.QPOSServiceListener;
import com.dspread.xpos.QPOSService.TransactionResult;
import com.dspread.xpos.QPOSService.UpdateInformationResult;

/** 
 * @ClassName: DHListener 
 * @Description: TODO
 * @author: user20
 * @date: 2015年8月26日 下午1:57:07  
 */
public class DHListener implements QPOSServiceListener{
	public static final int GET_POSID=200;
	public static final int CONNECT_SUCCESS=201;
	public static final int CONNECT_FAIL=199;//连接失败
	public static final int SET_AMOUNT=202;
	public static final int IC_DO_EMV=203;//IC卡刷卡流程
	public static final int SEND_TIME=204;//IC卡刷卡流程
	public static final int GET_IC_INFO=205;//获取IC卡信息
	public static final int IS_SERVER_CONNECTED=206;
	public static final int GET_CT_SUCCESS=207;//获取磁条卡信息成功
	public static final int UPDATE_WOEK_KEY_SUCCESS=208;//更新密钥成功
	public static final int UPDATE_WOEK_KEY_FAIL=209;//更新密钥失败
	public static final int CANCLE_TRANSACTIOPN=210;//点击取消按钮
	public static final int GET_CARD_NO_SUCCESS=211;//获取卡号成功
	Handler controllerHandler=null;
	
	
	/**
	 * 设置消息处理
	 * @Title: setControllerHandeler 
	 * @Description: TODO
	 * @param hander
	 * @return: void
	 */
    public void setControllerHandeler(Handler hander){
    	this.controllerHandler=hander;
    	
    }
	/* (non Javadoc) 
	 * @Title: onBluetoothBondFailed
	 * @Description: TODO 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onBluetoothBondFailed() 
	 */
	@Override
	public void onBluetoothBondFailed() {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onBluetoothBondTimeout
	 * @Description: TODO 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onBluetoothBondTimeout() 
	 */
	@Override
	public void onBluetoothBondTimeout() {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onBluetoothBonded
	 * @Description: TODO 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onBluetoothBonded() 
	 */
	@Override
	public void onBluetoothBonded() {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onBluetoothBonding
	 * @Description: TODO 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onBluetoothBonding() 
	 */
	@Override
	public void onBluetoothBonding() {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onCbcMacResult
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onCbcMacResult(java.lang.String) 
	 */
	@Override
	public void onCbcMacResult(String arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onConfirmAmountResult
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onConfirmAmountResult(boolean) 
	 */
	@Override
	public void onConfirmAmountResult(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onDoTradeResult
	 * @Description: TODO
	 * @param arg0
	 * @param arg1 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onDoTradeResult(com.dspread.xpos.QPOSService.DoTradeResult, java.util.Hashtable) 
	 */
	@Override
	public void onDoTradeResult(DoTradeResult result, Hashtable<String, String> decodeData) {
		if (result == DoTradeResult.MCR) {//LH: 刷磁条卡成功返回数据
			/*String formatID = decodeData.get("formatID");
			String maskedPAN = decodeData.get("maskedPAN");//卡号
			String expiryDate = decodeData.get("expiryDate");
			String cardHolderName = decodeData.get("cardholderName");
			String ksn = decodeData.get("ksn");
			String serviceCode = decodeData.get("serviceCode");
			String track1Length = decodeData.get("track1Length");
			String track2Length = decodeData.get("track2Length");
			String track3Length = decodeData.get("track3Length");
			String encTracks = decodeData.get("encTracks");
			String encTrack1 = decodeData.get("encTrack1");
			String encTrack2 = decodeData.get("encTrack2");
			String encTrack3 = decodeData.get("encTrack3");
			String partialTrack = decodeData.get("partialTrack");
			// TODO
			String pinKsn = decodeData.get("pinKsn");
			String trackksn = decodeData.get("trackksn");
			String pinBlock = decodeData.get("pinBlock");
			String encPAN = decodeData.get("encPAN");
			String trackRandomNumber = decodeData
					.get("trackRandomNumber");
			String pinRandomNumber = decodeData.get("pinRandomNumber");*/
			
			Enumeration<String> e=decodeData.keys();
			while(e.hasMoreElements()){
				String key=(String) e.nextElement();
				String value=decodeData.get(key);
				Log.v("dh1", key+"-->"+value);
			}
			Message msg=controllerHandler.obtainMessage();
			msg.what=GET_CT_SUCCESS;
			msg.obj=decodeData;
		    controllerHandler.sendMessage(msg);
			
			
		}else if(result == DoTradeResult.ICC){//IC卡
			
			Message msg=controllerHandler.obtainMessage();
			msg.what=IC_DO_EMV;
		    controllerHandler.sendMessage(msg);
		}
		
	}

	/* (non Javadoc) 
	 * @Title: onEmvICCExceptionData
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onEmvICCExceptionData(java.lang.String) 
	 */
	@Override
	public void onEmvICCExceptionData(String arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onError
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onError(com.dspread.xpos.QPOSService.Error) 
	 */
	@Override
	public void onError(Error arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onGetCardNoResult
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onGetCardNoResult(java.lang.String) 
	 */
	@Override
	public void onGetCardNoResult(String cardNo) {
		Message msg=controllerHandler.obtainMessage();
		msg.what=GET_CARD_NO_SUCCESS;
		msg.obj=cardNo;
		controllerHandler.sendMessage(msg);
		
	}

	/* (non Javadoc) 
	 * @Title: onGetInputAmountResult
	 * @Description: TODO
	 * @param arg0
	 * @param arg1 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onGetInputAmountResult(boolean, java.lang.String) 
	 */
	@Override
	public void onGetInputAmountResult(boolean arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onGetPosComm
	 * @Description: TODO
	 * @param arg0
	 * @param arg1
	 * @param arg2 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onGetPosComm(int, java.lang.String, java.lang.String) 
	 */
	@Override
	public void onGetPosComm(int arg0, String arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onLcdShowCustomDisplay
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onLcdShowCustomDisplay(boolean) 
	 */
	@Override
	public void onLcdShowCustomDisplay(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onPinKey_TDES_Result
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onPinKey_TDES_Result(java.lang.String) 
	 */
	@Override
	public void onPinKey_TDES_Result(String arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onQposIdResult
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onQposIdResult(java.util.Hashtable) 
	 */
	
	

	@Override
	public void onQposIdResult(Hashtable<String, String> posIdTable) {
		String posId = posIdTable.get("posId") == null ? "" : posIdTable.get("posId");
		
		Message msg=controllerHandler.obtainMessage();
		msg.what=GET_POSID;
		msg.obj=posId;
		controllerHandler.sendMessage(msg);
		
		
		 
		
	}

	/* (non Javadoc) 
	 * @Title: onQposInfoResult
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onQposInfoResult(java.util.Hashtable) 
	 */
	@Override
	public void onQposInfoResult(Hashtable<String, String> arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onReadBusinessCardResult
	 * @Description: TODO
	 * @param arg0
	 * @param arg1 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onReadBusinessCardResult(boolean, java.lang.String) 
	 */
	@Override
	public void onReadBusinessCardResult(boolean arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestBatchData
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestBatchData(java.lang.String) 
	 */
	@Override
	public void onRequestBatchData(String arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestCalculateMac
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestCalculateMac(java.lang.String) 
	 */
	@Override
	public void onRequestCalculateMac(String arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestDisplay
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestDisplay(com.dspread.xpos.QPOSService.Display) 
	 */
	@Override
	public void onRequestDisplay(Display arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestFinalConfirm
	 * @Description: TODO 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestFinalConfirm() 
	 */
	@Override
	public void onRequestFinalConfirm() {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestIsServerConnected
	 * @Description: TODO 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestIsServerConnected() 
	 */
	@Override
	public void onRequestIsServerConnected() {
		Message msg=controllerHandler.obtainMessage();
		msg.what=IS_SERVER_CONNECTED;
		controllerHandler.sendMessage(msg);
		
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestNoQposDetected
	 * @Description: TODO 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestNoQposDetected() 
	 */
	@Override
	public void onRequestNoQposDetected() {
		//Log.v("dis1", "dis");
		Message msg=controllerHandler.obtainMessage();
		msg.what=CONNECT_FAIL;
		controllerHandler.sendMessage(msg);
		/*Message msg=controllerHandler.obtainMessage();
		msg.what=IS_SERVER_CONNECTED;
		controllerHandler.sendMessage(msg);*/
	}

	/* (non Javadoc) 
	 * @Title: onRequestOnlineProcess
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestOnlineProcess(java.lang.String) 
	 */
	@Override
	public void onRequestOnlineProcess(String str55) {
		Log.v("str55", str55);
		Message msg=controllerHandler.obtainMessage();
		msg.what=GET_IC_INFO;
		msg.obj=str55;//55域数据
		controllerHandler.sendMessage(msg);
		
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestQposConnected
	 * @Description: TODO 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestQposConnected() 
	 */
	
	@Override
	public void onRequestQposConnected() {//连接成功
		
		Message msg=controllerHandler.obtainMessage();
		msg.what=CONNECT_SUCCESS;
		controllerHandler.sendMessage(msg);
		 
		
		
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestQposDisconnected
	 * @Description: TODO 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestQposDisconnected() 
	 */
	@Override
	public void onRequestQposDisconnected() {

		
		//Log.v("dis1","dis");
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestSelectEmvApp
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestSelectEmvApp(java.util.ArrayList) 
	 */
	@Override
	public void onRequestSelectEmvApp(ArrayList<String> arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestSetAmount
	 * @Description: TODO 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestSetAmount() 
	 */
	@Override
	public void onRequestSetAmount() {
		Message msg=controllerHandler.obtainMessage();
		msg.what=SET_AMOUNT;
		controllerHandler.sendMessage(msg);
		 
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestSetPin
	 * @Description: TODO 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestSetPin() 
	 */
	@Override
	public void onRequestSetPin() {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestSignatureResult
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestSignatureResult(byte[]) 
	 */
	@Override
	public void onRequestSignatureResult(byte[] arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestTime
	 * @Description: TODO 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestTime() 
	 */
	@Override
	public void onRequestTime() {
		String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss")
		.format(Calendar.getInstance().getTime());
		Message msg=controllerHandler.obtainMessage();
		msg.what=SEND_TIME;
		msg.obj=terminalTime;
		controllerHandler.sendMessage(msg);
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestTransactionLog
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestTransactionLog(java.lang.String) 
	 */
	@Override
	public void onRequestTransactionLog(String arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestTransactionResult
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestTransactionResult(com.dspread.xpos.QPOSService.TransactionResult) 
	 */
	@Override
	public void onRequestTransactionResult(TransactionResult transactionResult) {
		if (transactionResult == TransactionResult.CANCEL) {//表示取消交易
			Message msg=controllerHandler.obtainMessage();
			msg.what=CANCLE_TRANSACTIOPN;
			controllerHandler.sendMessage(msg);
		 }
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestUpdateWorkKeyResult
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestUpdateWorkKeyResult(com.dspread.xpos.QPOSService.UpdateInformationResult) 
	 */
	@Override
	public void onRequestUpdateWorkKeyResult(UpdateInformationResult result) {
		
		if(result==UpdateInformationResult.UPDATE_SUCCESS){
			Message msg=controllerHandler.obtainMessage();
			msg.what=UPDATE_WOEK_KEY_SUCCESS;
		    controllerHandler.sendMessage(msg);
			Log.v("up1", "success");
		}else if(result==UpdateInformationResult.UPDATE_FAIL){
			Log.v("up1", "fail");
		}else if(result==UpdateInformationResult.UPDATE_PACKET_VEFIRY_ERROR){
			Log.v("up1", "v");
		}else if(result==UpdateInformationResult.UPDATE_PACKET_LEN_ERROR){
			Log.v("up1", "l");
		}
		
	}

	/* (non Javadoc) 
	 * @Title: onRequestWaitingUser
	 * @Description: TODO 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestWaitingUser() 
	 */
	@Override
	public void onRequestWaitingUser() {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onReturnApduResult
	 * @Description: TODO
	 * @param arg0
	 * @param arg1
	 * @param arg2 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onReturnApduResult(boolean, java.lang.String, int) 
	 */
	@Override
	public void onReturnApduResult(boolean arg0, String arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onReturnBatchSendAPDUResult
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onReturnBatchSendAPDUResult(java.util.LinkedHashMap) 
	 */
	@Override
	public void onReturnBatchSendAPDUResult(LinkedHashMap<Integer, String> arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onReturnCustomConfigResult
	 * @Description: TODO
	 * @param arg0
	 * @param arg1 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onReturnCustomConfigResult(boolean, java.lang.String) 
	 */
	@Override
	public void onReturnCustomConfigResult(boolean arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onReturnDownloadRsaPublicKey
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onReturnDownloadRsaPublicKey(java.util.HashMap) 
	 */
	@Override
	public void onReturnDownloadRsaPublicKey(HashMap<String, String> arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onReturnGetPinResult
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onReturnGetPinResult(java.util.Hashtable) 
	 */
	@Override
	public void onReturnGetPinResult(Hashtable<String, String> arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onReturnNFCApduResult
	 * @Description: TODO
	 * @param arg0
	 * @param arg1
	 * @param arg2 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onReturnNFCApduResult(boolean, java.lang.String, int) 
	 */
	@Override
	public void onReturnNFCApduResult(boolean arg0, String arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onReturnPowerOffIccResult
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onReturnPowerOffIccResult(boolean) 
	 */
	@Override
	public void onReturnPowerOffIccResult(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onReturnPowerOffNFCResult
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onReturnPowerOffNFCResult(boolean) 
	 */
	@Override
	public void onReturnPowerOffNFCResult(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onReturnPowerOnIccResult
	 * @Description: TODO
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onReturnPowerOnIccResult(boolean, java.lang.String, java.lang.String, int) 
	 */
	@Override
	public void onReturnPowerOnIccResult(boolean arg0, String arg1, String arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onReturnPowerOnNFCResult
	 * @Description: TODO
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onReturnPowerOnNFCResult(boolean, java.lang.String, java.lang.String, int) 
	 */
	@Override
	public void onReturnPowerOnNFCResult(boolean arg0, String arg1, String arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onReturnReversalData
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onReturnReversalData(java.lang.String) 
	 */
	@Override
	public void onReturnReversalData(String arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onReturnSetMasterKeyResult
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onReturnSetMasterKeyResult(boolean) 
	 */
	@Override
	public void onReturnSetMasterKeyResult(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onReturnSetSleepTimeResult
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onReturnSetSleepTimeResult(boolean) 
	 */
	@Override
	public void onReturnSetSleepTimeResult(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onReturniccCashBack
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onReturniccCashBack(java.util.Hashtable) 
	 */
	@Override
	public void onReturniccCashBack(Hashtable<String, String> arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onSetParamsResult
	 * @Description: TODO
	 * @param arg0
	 * @param arg1 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onSetParamsResult(boolean, java.util.Hashtable) 
	 */
	@Override
	public void onSetParamsResult(boolean arg0, Hashtable<String, Object> arg1) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onUpdateMasterKeyResult
	 * @Description: TODO
	 * @param arg0
	 * @param arg1 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onUpdateMasterKeyResult(boolean, java.util.Hashtable) 
	 */
	@Override
	public void onUpdateMasterKeyResult(boolean arg0, Hashtable<String, String> arg1) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onUpdatePosFirmwareResult
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onUpdatePosFirmwareResult(com.dspread.xpos.QPOSService.UpdateInformationResult) 
	 */
	@Override
	public void onUpdatePosFirmwareResult(UpdateInformationResult arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non Javadoc) 
	 * @Title: onWriteBusinessCardResult
	 * @Description: TODO
	 * @param arg0 
	 * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onWriteBusinessCardResult(boolean) 
	 */
	@Override
	public void onWriteBusinessCardResult(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

}
